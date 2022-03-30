/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.easyocr.processors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.annot8.components.easyocr.processors.TestUtil.*;
import static io.annot8.components.easyocr.processors.TestUtil.checkCanProcessImage;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import org.junit.jupiter.api.Test;

@WireMockTest
public class AbstractEasyOCRTest {

  @Test
  public void testFile(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

    stubFor(post("/init").willReturn(ok()));
    stubFor(
        post("/ocr")
            .willReturn(
                ok().withHeader("Content-Type", "text/plain").withBody("Annot8 Test Image")));

    String url = wmRuntimeInfo.getHttpBaseUrl();

    RemoteEasyOCR desc = new RemoteEasyOCR();
    try (Processor ocr =
        desc.createComponent(
            null, RemoteEasyOCR.Settings.builder().withUrl(url).initialize().build())) {
      checkCanProcessFile(ocr);
    }

    verify(
        postRequestedFor(urlEqualTo("/init"))
            .withHeader("Content-Type", containing("application/json"))
            .withRequestBody(
                equalToJson("{\"download\" : false, \"gpu\" : false, \"lang\" : \"en\" }")));

    verify(
        postRequestedFor(urlEqualTo("/ocr"))
            .withHeader("Content-Type", containing("multipart/form-data;boundary="))
            .withRequestBody(containing("Content-Disposition: form-data;"))
            .withRequestBody(containing("name=\"file\";"))
            .withRequestBody(containing("Content-Type: image/tiff"))
            .withRequestBody(containing("filename=\"test-image.tif\"")));
  }

  @Test
  public void testImage(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

    stubFor(
        post("/ocr")
            .willReturn(
                ok().withHeader("Content-Type", "text/plain").withBody("Annot8 Test Image")));

    String url = wmRuntimeInfo.getHttpBaseUrl();

    RemoteEasyOCR desc = new RemoteEasyOCR();
    try (Processor ocr =
        desc.createComponent(null, RemoteEasyOCR.Settings.builder().withUrl(url).build())) {
      checkCanProcessImage(ocr);
    }

    verify(exactly(0), postRequestedFor(urlEqualTo("/init")));

    verify(
        postRequestedFor(urlEqualTo("/ocr"))
            .withHeader("Content-Type", containing("multipart/form-data;boundary="))
            .withRequestBody(containing("Content-Disposition: form-data;"))
            .withRequestBody(containing("name=\"file\";"))
            .withRequestBody(containing("Content-Type: image/png"))
            .withRequestBody(matching(".*filename=\".*-ocr\\.png\".*")));
  }

  @Test
  public void testIntializtionError(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

    stubFor(
        post("/init")
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withBody("{\"status\":\"Error\",\"message\":\"Endpoint not found\"}")));

    String url = wmRuntimeInfo.getHttpBaseUrl();

    try (Processor ocr =
        new RemoteEasyOCR.Processor(
            RemoteEasyOCR.Settings.builder().withUrl(url).initialize().build(), 10, 2)) {
      fail("create should throw");
    } catch (BadConfigurationException e) {
      // expected
    }

    verify(
        exactly(2),
        postRequestedFor(urlEqualTo("/init"))
            .withHeader("Content-Type", containing("application/json"))
            .withRequestBody(
                equalToJson("{\"download\" : false, \"gpu\" : false, \"lang\" : \"en\" }")));
  }

  @Test
  public void testProcessing500Error(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
    stubFor(post("/ocr").willReturn(aResponse().withStatus(500)));
    String url = wmRuntimeInfo.getHttpBaseUrl();

    try (Processor ocr =
        new RemoteEasyOCR.Processor(RemoteEasyOCR.Settings.builder().withUrl(url).build())) {
      Item image = image();
      assertTrue(ocr.process(image).hasExceptions());
    }
  }

  @Test
  public void testProcessing404Error(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
    stubFor(post("/ocr").willReturn(aResponse().withStatus(404)));
    String url = wmRuntimeInfo.getHttpBaseUrl();

    try (Processor ocr =
        new RemoteEasyOCR.Processor(RemoteEasyOCR.Settings.builder().withUrl(url).build())) {
      Item file = file();
      assertTrue(ocr.process(file).hasExceptions());
    }
  }
}
