/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import com.google.common.base.Strings;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.*;
import io.annot8.common.data.content.*;
import io.annot8.conventions.PropertyKeys;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import javax.json.*;
import org.apache.commons.text.StringEscapeUtils;

@ComponentName("File Sink")
@ComponentDescription(
    "Outputs extracted content and metadata as files within a nested folder structure")
@SettingsClass(FileSink.Settings.class)
public class FileSink extends AbstractProcessorDescriptor<FileSink.Processor, FileSink.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .withProcessesGroups("*")
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      // Create folder for item
      File itemFolder;
      try {
        itemFolder =
            Files.createDirectories(
                    getItemPath(item, settings.getRootOutputFolder(), settings.getBasePaths()))
                .toFile();
      } catch (IOException e) {
        log().error("Unable to create directory for item {}", item.getId(), e);
        return ProcessorResponse.itemError(e);
      }

      if (settings.isCopyOriginalFile()) {
        Optional<Object> source = item.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE);
        if (source.isPresent()) {
          try {
            copyOriginalFile(source.get(), itemFolder.toPath());
          } catch (IOException e) {
            log().error("Unable to copy original file for item {}", item.getId(), e);
          }
        }
      }

      // Output properties
      if (!Strings.isNullOrEmpty(settings.getPropertiesFilename())) {
        try {
          writeJson(
              objectToJson(item.getProperties().getAll()),
              new File(itemFolder, settings.getPropertiesFilename()));
        } catch (IOException e) {
          log().error("Unable to write properties file for item {}", item.getId(), e);
        }
      }

      // Output groups
      if (!Strings.isNullOrEmpty(settings.getGroupsFilename())) {
        try {
          writeJson(
              groupsToJson(item.getGroups().getAll()),
              new File(itemFolder, settings.getGroupsFilename()));
        } catch (IOException e) {
          log().error("Unable to write groups file for item {}", item.getId(), e);
        }
      }

      item.getContents()
          .forEach(
              content -> {
                File contentFolder;
                try {
                  contentFolder =
                      Files.createDirectories(itemFolder.toPath().resolve(content.getId()))
                          .toFile();
                } catch (IOException e) {
                  log().error("Unable to create directory for content {}", content.getId(), e);
                  return;
                }

                // Output each content
                if (!Strings.isNullOrEmpty(settings.getContentFilename())) {
                  try {
                    writeContent(content, contentFolder, settings);
                  } catch (IOException e) {
                    log().error("Unable to write content to disk", e);
                  } catch (IllegalArgumentException e) {
                    log().warn("Unsupported content", e);
                  }
                }

                // Output properties for each content
                if (!Strings.isNullOrEmpty(settings.getPropertiesFilename())) {
                  try {
                    writeJson(
                        objectToJson(content.getProperties().getAll()),
                        new File(contentFolder, settings.getPropertiesFilename()));
                  } catch (IOException e) {
                    log()
                        .error(
                            "Unable to write properties file for content {}", content.getId(), e);
                  }
                }

                // Output annotations for each content
                if (!Strings.isNullOrEmpty(settings.getAnnotationsFilename())) {
                  try {
                    writeJson(
                        annotationsToJson(content.getAnnotations().getAll()),
                        new File(contentFolder, settings.getAnnotationsFilename()));
                  } catch (IOException e) {
                    log()
                        .error(
                            "Unable to write annotations file for content {}", content.getId(), e);
                  }
                }
              });

      return ProcessorResponse.ok();
    }

    protected static Path copyOriginalFile(Object source, Path targetDirectory) throws IOException {
      Path p;
      if (source instanceof Path) {
        p = (Path) source;
      } else if (source instanceof File) {
        p = ((File) source).toPath();
      } else if (source instanceof String) {
        p = Path.of((String) source);
      } else if (source instanceof URI) {
        p = Path.of((URI) source);
      } else {
        return null;
      }

      return Files.copy(
          p, targetDirectory.resolve(p.getFileName()), StandardCopyOption.COPY_ATTRIBUTES);
    }

    protected static Path getItemPath(
        Item item, Path rootOutputFolder, List<Path> baseSourceFolders) {
      Optional<Object> o = item.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE);

      if (o.isEmpty()) {
        return rootOutputFolder.resolve(item.getId());
      }

      Path source = Path.of(o.get().toString());
      Path cleanSource =
          baseSourceFolders.stream()
              .filter(source::startsWith)
              .findFirst()
              .map(path -> path.relativize(source))
              .orElse(source);

      return Path.of(rootOutputFolder.toString(), cleanSource.toString());
    }

    protected static void writeJson(JsonValue json, File outputFile) throws IOException {
      if (JsonValue.NULL.equals(json)
          || JsonValue.EMPTY_JSON_ARRAY.equals(json)
          || JsonValue.EMPTY_JSON_OBJECT.equals(json)) return;

      try (FileOutputStream fos = new FileOutputStream(outputFile);
          JsonWriter writer = Json.createWriter(fos)) {
        writer.write(json);
      }
    }

    protected static File writeContent(Content<?> content, File contentFolder, Settings settings)
        throws IOException, IllegalArgumentException {
      File f;

      if (content instanceof Text) {
        f = new File(contentFolder, settings.getContentFilename() + ".txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
          bw.write(((Text) content).getData());
        }
      } else if (content instanceof FileContent) {
        Path source = ((FileContent) content).getData().toPath();
        f =
            new File(
                contentFolder,
                settings.getContentFilename()
                    + "."
                    + com.google.common.io.Files.getFileExtension(source.toString()));

        Files.copy(source, f.toPath());
      } else if (content instanceof InputStreamContent) {
        f = new File(contentFolder, settings.getContentFilename());

        byte[] bytes = new byte[8192];
        int read;

        try (FileOutputStream fos = new FileOutputStream(f)) {
          while ((read = ((InputStreamContent) content).getData().read(bytes)) != -1) {
            fos.write(bytes, 0, read);
          }
        }
      } else if (content instanceof Image) {
        String extension;
        switch (settings.getImageType()) {
          case JPG:
            extension = ".jpg";
            break;
          case PNG:
            extension = ".png";
            break;
          default:
            extension = "";
        }

        f = new File(contentFolder, settings.getContentFilename() + extension);

        try (FileOutputStream fos = new FileOutputStream(f)) {
          switch (settings.getImageType()) {
            case JPG:
              ((Image) content).saveAsJpg(fos);
              break;
            case PNG:
              ((Image) content).saveAsPng(fos);
              break;
            default:
              throw new IllegalArgumentException(
                  "Image type " + settings.getImageType() + " is not supported");
          }
        }
      } else if (content instanceof UriContent) {
        f = new File(contentFolder, settings.getContentFilename() + ".url");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
          bw.write("[InternetShortcut]\nURL=" + ((UriContent) content).getData().toString());
        }
      } else if (content instanceof TableContent) {
        f = new File(contentFolder, settings.getContentFilename() + ".csv");

        Table tbl = ((TableContent) content).getData();

        StringJoiner sj = new StringJoiner("\n");

        // Column Names
        tbl.getColumnNames()
            .ifPresent(
                names -> {
                  StringJoiner rowSj = new StringJoiner(",");
                  names.forEach(s -> rowSj.add(StringEscapeUtils.escapeCsv(s)));

                  sj.add(rowSj.toString());
                });

        // Data
        tbl.getRows()
            .forEach(
                row -> {
                  StringJoiner rowSj = new StringJoiner(",");

                  for (int i = 0; i < row.getColumnCount(); i++) {
                    rowSj.add(StringEscapeUtils.escapeCsv(row.getValueAt(i).orElse("").toString()));
                  }

                  sj.add(rowSj.toString());
                });

        // Write data
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
          bw.write(sj.toString());
        }

      } else {
        throw new IllegalArgumentException(
            "Content type " + content.getContentClass().getName() + " is not supported");
      }

      return f;
    }

    protected static JsonValue objectToJson(Object object) {
      if (object == null) {
        return JsonValue.NULL;
      } else if (object instanceof JsonValue) {
        return (JsonValue) object;
      } else if (object instanceof Boolean) {
        return ((Boolean) object) ? JsonValue.TRUE : JsonValue.FALSE;
      } else if (object instanceof Integer) {
        return Json.createValue((Integer) object);
      } else if (object instanceof Long) {
        return Json.createValue((Long) object);
      } else if (object instanceof Double) {
        return Json.createValue((Double) object);
      } else if (object instanceof BigInteger) {
        return Json.createValue((BigInteger) object);
      } else if (object instanceof BigDecimal) {
        return Json.createValue((BigDecimal) object);
      } else if (object instanceof Collection) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        ((Collection<?>) object).forEach(o -> arrayBuilder.add(objectToJson(o)));
        return arrayBuilder.build();
      } else if (object instanceof Map) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        ((Map<?, ?>) object).forEach((k, v) -> objectBuilder.add(k.toString(), objectToJson(v)));
        return objectBuilder.build();
      } else {
        return Json.createValue(object.toString());
      }
    }

    protected static JsonArray annotationsToJson(Stream<Annotation> annotations) {
      JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
      annotations.forEach(a -> arrayBuilder.add(annotationToJson(a)));

      return arrayBuilder.build();
    }

    protected static JsonObject annotationToJson(Annotation annotation) {
      JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

      objectBuilder.add("id", annotation.getId());
      objectBuilder.add("contentId", annotation.getContentId());
      objectBuilder.add("type", annotation.getType());
      objectBuilder.add("properties", objectToJson(annotation.getProperties().getAll()));
      try {
        objectBuilder.add("bounds", boundsToJson(annotation.getBounds()));
      } catch (IllegalArgumentException iae) {
        // Do nothing - no support for bounds
      }

      return objectBuilder.build();
    }

    protected static JsonValue boundsToJson(Bounds bounds) {
      JsonObjectBuilder builder = Json.createObjectBuilder();

      if (bounds instanceof SpanBounds) {
        builder.add("begin", ((SpanBounds) bounds).getBegin());
        builder.add("end", ((SpanBounds) bounds).getEnd());
      } else if (bounds instanceof PositionBounds) {
        builder.add("position", ((PositionBounds) bounds).getPosition());
      } else if (bounds instanceof RectangleBounds) {
        builder.add("top", ((RectangleBounds) bounds).getTop());
        builder.add("bottom", ((RectangleBounds) bounds).getBottom());
        builder.add("left", ((RectangleBounds) bounds).getLeft());
        builder.add("right", ((RectangleBounds) bounds).getRight());
      } else if (bounds instanceof CellBounds) {
        builder.add("row", ((CellBounds) bounds).getRow());
        builder.add("column", ((CellBounds) bounds).getColumn());
      } else if (bounds instanceof MultiCellBounds) {
        builder.add("row", ((MultiCellBounds) bounds).getRow());

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (int col : ((MultiCellBounds) bounds).getCells()) arrayBuilder.add(col);

        builder.add("columns", arrayBuilder);
      } else if (bounds instanceof NoBounds || bounds instanceof ContentBounds) {
        // Do nothing, these bounds have no fields
        return JsonValue.EMPTY_JSON_OBJECT;
      } else {
        throw new IllegalArgumentException(
            "Bounds type " + bounds.getClass() + " is not supported");
      }

      return builder.build();
    }

    protected static JsonArray groupsToJson(Stream<Group> groups) {
      JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
      groups.forEach(g -> arrayBuilder.add(groupToJson(g)));

      return arrayBuilder.build();
    }

    protected static JsonObject groupToJson(Group group) {
      JsonObjectBuilder objectBuilder = Json.createObjectBuilder();

      objectBuilder.add("id", group.getId());
      objectBuilder.add("type", group.getType());
      objectBuilder.add("properties", objectToJson(group.getProperties().getAll()));

      JsonObjectBuilder annotationsObjectBuilder = Json.createObjectBuilder();

      group
          .getAnnotations()
          .forEach(
              (role, annotations) ->
                  annotationsObjectBuilder.add(role, annotationsToJson(annotations)));

      objectBuilder.add("annotations", annotationsObjectBuilder);

      return objectBuilder.build();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Path rootOutputFolder = Path.of(".");
    private String propertiesFilename = "properties.json";
    private String contentFilename = "content";
    private ImageType imageType = ImageType.JPG;
    private String annotationsFilename = "annotations.json";
    private String groupsFilename = "groups.json";
    private List<Path> basePaths = Collections.emptyList();
    private boolean copyOriginalFile = false;

    @Override
    public boolean validate() {
      return rootOutputFolder != null && imageType != null && basePaths != null;
    }

    @Description(value = "The root folder in which to save files", defaultValue = ".")
    public Path getRootOutputFolder() {
      return rootOutputFolder;
    }

    public void setRootOutputFolder(Path rootOutputFolder) {
      this.rootOutputFolder = rootOutputFolder;
    }

    @Description(
        value = "The file name for files containing Item and Content properties",
        defaultValue = "properties.json")
    public String getPropertiesFilename() {
      return propertiesFilename;
    }

    public void setPropertiesFilename(String propertiesFilename) {
      this.propertiesFilename = propertiesFilename;
    }

    @Description(
        value = "The file name for extracted Content - a suitable extension will be added",
        defaultValue = "content")
    public String getContentFilename() {
      return contentFilename;
    }

    public void setContentFilename(String contentFilename) {
      this.contentFilename = contentFilename;
    }

    @Description(value = "The format that image Content should be saved in", defaultValue = "JPG")
    public ImageType getImageType() {
      return imageType;
    }

    public void setImageType(ImageType imageType) {
      this.imageType = imageType;
    }

    @Description(
        value = "The file name for files containing Content annotations",
        defaultValue = "annotations.json")
    public String getAnnotationsFilename() {
      return annotationsFilename;
    }

    public void setAnnotationsFilename(String annotationsFilename) {
      this.annotationsFilename = annotationsFilename;
    }

    @Description(
        value = "The file name for files containing Item groups",
        defaultValue = "groups.json")
    public String getGroupsFilename() {
      return groupsFilename;
    }

    public void setGroupsFilename(String groupsFilename) {
      this.groupsFilename = groupsFilename;
    }

    @Description(
        "If the source path of any Item begins with any of these paths, then it will be truncated")
    public List<Path> getBasePaths() {
      return basePaths;
    }

    public void setBasePaths(List<Path> basePaths) {
      this.basePaths = basePaths;
    }

    @Description(
        value =
            "If true, and the source file for an item can be identified, then it is copied into the Item folder",
        defaultValue = "false")
    public boolean isCopyOriginalFile() {
      return copyOriginalFile;
    }

    public void setCopyOriginalFile(boolean copyOriginalFile) {
      this.copyOriginalFile = copyOriginalFile;
    }

    public enum ImageType {
      JPG,
      PNG
    }
  }
}
