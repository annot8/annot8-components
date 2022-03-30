/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.easyocr.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@ComponentName("Local EasyOCR")
@ComponentDescription("Perform OCR using the locally installed EasyOCR library")
public class LocalEasyOCR
    extends AbstractProcessorDescriptor<LocalEasyOCR.Processor, LocalEasyOCR.Settings> {

  // @formatter:off
  private static final String OCR_PY =
      String.join(
          System.getProperty("line.separator"),
          "from fastapi import FastAPI, Response, File",
          "from typing import Optional",
          "from pydantic import BaseModel",
          "import easyocr",
          "",
          "class Config(BaseModel):",
          "    langs: Optional[str] = \"en\"",
          "    download: Optional[bool] = False",
          "    gpu: Optional[bool] = False",
          "",
          "app = FastAPI()",
          "",
          "",
          "reader = easyocr.Reader([\"en\"], download_enabled=False, gpu=False)",
          "",
          "",
          "@app.post(\"/init\")",
          "def init(config: Config):",
          "    langs = config.langs.split(\",\")",
          "    global reader",
          "    reader = easyocr.Reader(langs, download_enabled=config.download, gpu=config.gpu)",
          "    ",
          "@app.post(\"/ocr\")",
          "def index(file: bytes = File(...)):",
          "    results = reader.readtext(file, detail=0, paragraph=True, y_ths = -0.01, x_ths = 10.0)",
          "    return Response(content=\"\\n\\n\".join(results), media_type=\"text/plain\")");
  // @formatter:on

  // Copy the python code from the jar so it can be called more easily
  static {
    try {
      Files.write(Path.of("./ocr.py"), OCR_PY.getBytes(), StandardOpenOption.CREATE);
    } catch (Exception e) {
      throw new BadConfigurationException("Unable to write ocr code", e);
    }
  }

  @Override
  public Capabilities capabilities() {
    return AbstractEasyOCRProcessor.capabilities();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractEasyOCRProcessor {

    private final ExecutorService pool;
    private final Settings settings;
    private Process process;

    public Processor(Settings settings) {
      this.settings = settings;
      pool = Executors.newSingleThreadExecutor();
      startEasyOCR();
    }

    @SuppressWarnings({"java:S1141", "java:S4036"})
    private void startEasyOCR() {
      try {
        log().info("Creating EasyOCR server on port {}", settings.getPort());

        // uvicorn ocr:app --host=127.0.0.1 --port=XXXX
        process =
            new ProcessBuilder(
                    "uvicorn",
                    "ocr:app",
                    "--host",
                    "127.0.0.1",
                    "--port",
                    Integer.toString(settings.getPort()))
                .redirectErrorStream(true)
                .start();

        pool.submit(
            new ProcessReadTask(
                process.getInputStream(), l -> log().debug("EasyOCR-Server: {}", l)));

        tryInitializeEasyOCR(
            new InitSettings(settings.getLang(), settings.isDownload(), settings.isGpu()));
      } catch (Exception e) {
        log().error("Easy-OCR start error", e);
        close();
      }
    }

    protected String getUrl(String string) {
      try {
        return new URL("http", "127.0.0.1", settings.getPort(), string).toString();
      } catch (MalformedURLException e) {
        throw new BadConfigurationException("Supplied port invalid", e);
      }
    }

    @Override
    public void close() {
      pool.shutdown();
      if (process != null) {
        process.destroy();
      }
    }
  }

  private static class ProcessReadTask implements Runnable {

    private final InputStream inputStream;
    private final Consumer<String> consumer;

    public ProcessReadTask(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
        output.lines().forEach(consumer);
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private static final boolean DEFAULT_DOWNLOAD = true;
    private static final boolean DEFAULT_GPU = false;
    private static final String DEFAULT_LANGS = "en";
    private static final int DEFAULT_PORT = 8000;

    private boolean download = DEFAULT_DOWNLOAD;
    private boolean gpu = DEFAULT_GPU;
    private String langs = DEFAULT_LANGS;
    private int port = DEFAULT_PORT;

    @Override
    public boolean validate() {
      return langs != null;
    }

    @Description("Set true to allow language models to be downloaded")
    public boolean isDownload() {
      return download;
    }

    public void setDownload(boolean download) {
      this.download = download;
    }

    @Description("Set true to use allow language models to be downloaded")
    public boolean isGpu() {
      return gpu;
    }

    public void setGpu(boolean gpu) {
      this.gpu = gpu;
    }

    public void setLangs(String langs) {
      this.langs = langs;
    }

    @Description(
        "Comma separated list of languages expected in the text, see https://www.jaided.ai/easyocr/ for supported languages")
    public String getLang() {
      return langs;
    }

    public void setPort(int port) {
      this.port = port;
    }

    @Description("Port to run the EasyOCR server on, defaults to 8000")
    public int getPort() {
      return port;
    }

    public static class Builder {
      private List<String> langs = new ArrayList<>();
      private boolean download = DEFAULT_DOWNLOAD;
      private boolean gpu = DEFAULT_GPU;
      private int port = DEFAULT_PORT;

      public Builder withDownload(boolean download) {
        this.download = download;
        return this;
      }

      public Builder useGpu() {
        this.gpu = true;
        return this;
      }

      public Builder withLang(String lang) {
        this.langs.add(lang);
        return this;
      }

      public Builder withPort(int port) {
        this.port = port;
        return this;
      }

      public Settings build() {
        Settings settings = new Settings();
        settings.setDownload(download);
        settings.setGpu(gpu);
        if (!langs.isEmpty()) {
          settings.setLangs(String.join(",", langs));
        }
        settings.setPort(port);
        return settings;
      }
    }

    public static Builder builder() {
      return new Builder();
    }
  }
}
