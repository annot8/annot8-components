/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.*;
import io.annot8.common.data.content.*;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.json.*;
import org.junit.jupiter.api.Test;

public class FileSinkTest {
  @Test
  public void testGetItemPathNoSource() {
    TestItem item = new TestItem();

    Path itemPath = FileSink.Processor.getItemPath(item, Path.of("."), Collections.emptyList());
    assertEquals(Path.of(".", item.getId()), itemPath);
  }

  @Test
  public void testGetItemPathWithSource() {
    TestItem item = new TestItem();
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, "/path/to/my/file.txt");

    Path itemPath =
        FileSink.Processor.getItemPath(item, Path.of("./output/"), Collections.emptyList());

    assertEquals(Path.of("./output", "/path/to/my/file.txt"), itemPath);
    assertEquals("./output/path/to/my/file.txt", itemPath.toString());
  }

  @Test
  public void testGetItemPathWithRelativeSource() {
    TestItem item = new TestItem();
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, "path/to/my/file.txt");

    Path itemPath =
        FileSink.Processor.getItemPath(item, Path.of("./output/"), Collections.emptyList());

    assertEquals(Path.of("./output/", "/path/to/my/file.txt"), itemPath);
    assertEquals("./output/path/to/my/file.txt", itemPath.toString());
  }

  @Test
  public void testGetItemPathWithPathSource() {
    TestItem item = new TestItem();
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, Path.of("/path/to/my/file.txt"));

    Path itemPath =
        FileSink.Processor.getItemPath(item, Path.of("./output/"), Collections.emptyList());

    assertEquals(Path.of("./output/", "/path/to/my/file.txt"), itemPath);
    assertEquals("./output/path/to/my/file.txt", itemPath.toString());
  }

  @Test
  public void testGetItemPathWithBasePaths() {
    TestItem item = new TestItem();
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, Path.of("/path/to/my/file.txt"));

    Path itemPath =
        FileSink.Processor.getItemPath(
            item, Path.of("./output/"), List.of(Path.of("/my/path/"), Path.of("/path/to")));

    assertEquals(Path.of("./output/", "my/file.txt"), itemPath);
    assertEquals("./output/my/file.txt", itemPath.toString());
  }

  @Test
  public void testWriteJson() throws IOException {
    JsonValue json = Json.createObjectBuilder().add("val", "Hello World!").build();

    File tempFile = Files.createTempFile("filesinktest-", ".json").toFile();
    tempFile.deleteOnExit();

    FileSink.Processor.writeJson(json, tempFile);

    String writtenContent = Files.readString(tempFile.toPath());
    assertEquals("{\"val\":\"Hello World!\"}", writtenContent);
  }

  @Test
  public void testWriteJsonNull() throws IOException {
    File tempFile = Files.createTempFile("filesinktest-", ".json").toFile();
    tempFile.deleteOnExit();

    FileSink.Processor.writeJson(JsonValue.NULL, tempFile);

    assertEquals(0, Files.size(tempFile.toPath()));
  }

  @Test
  public void testWriteJsonEmptyObject() throws IOException {
    File tempFile = Files.createTempFile("filesinktest-", ".json").toFile();
    tempFile.deleteOnExit();

    FileSink.Processor.writeJson(JsonValue.EMPTY_JSON_OBJECT, tempFile);

    assertEquals(0, Files.size(tempFile.toPath()));
  }

  @Test
  public void testWriteJsonEmptyArray() throws IOException {
    File tempFile = Files.createTempFile("filesinktest-", ".json").toFile();
    tempFile.deleteOnExit();

    FileSink.Processor.writeJson(JsonValue.EMPTY_JSON_ARRAY, tempFile);

    assertEquals(0, Files.size(tempFile.toPath()));
  }

  @Test
  public void testWriteContentString() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    Item item = new TestItem();
    Content<?> c = item.createContent(Text.class).withData("Hello World!").save();

    File f = FileSink.Processor.writeContent(c, tempDir, new FileSink.Settings());
    f.deleteOnExit();

    String writtenContent = Files.readString(f.toPath());
    assertEquals("Hello World!", writtenContent);
  }

  @Test
  public void testWriteContentInputStream() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    Item item = new TestItem();
    Content<?> c =
        item.createContent(InputStreamContent.class)
            .withData(new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8)))
            .save();

    File f = FileSink.Processor.writeContent(c, tempDir, new FileSink.Settings());
    f.deleteOnExit();

    String writtenContent = Files.readString(f.toPath());
    assertEquals("Hello World!", writtenContent);
  }

  @Test
  public void testWriteContentUri() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    Item item = new TestItem();
    Content<?> c =
        item.createContent(UriContent.class).withData(URI.create("http://www.example.com")).save();

    File f = FileSink.Processor.writeContent(c, tempDir, new FileSink.Settings());
    f.deleteOnExit();

    String writtenContent = Files.readString(f.toPath());
    assertEquals("[InternetShortcut]\nURL=http://www.example.com", writtenContent);
  }

  @Test
  public void testWriteContentFile() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    Item item = new TestItem();
    Content<?> c = item.createContent(FileContent.class).withData(tempFile).save();

    File f = FileSink.Processor.writeContent(c, tempDir, new FileSink.Settings());
    f.deleteOnExit();

    assertEquals("content.txt", f.getName());

    byte[] b1 = Files.readAllBytes(tempFile.toPath());
    byte[] b2 = Files.readAllBytes(f.toPath());

    assertTrue(Arrays.equals(b1, b2));
  }

  @Test
  public void testWriteContentImagePng() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    BufferedImage img = ImageIO.read(FileSinkTest.class.getResourceAsStream("test.png"));

    Item item = new TestItem();
    Content<?> c = item.createContent(Image.class).withData(img).save();

    FileSink.Settings s = new FileSink.Settings();
    s.setImageType(FileSink.Settings.ImageType.PNG);

    File f = FileSink.Processor.writeContent(c, tempDir, s);
    f.deleteOnExit();

    BufferedImage writtenImg = ImageIO.read(f);
    assertNotNull(writtenImg);
  }

  @Test
  public void testWriteContentImageJpg() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    BufferedImage img = ImageIO.read(FileSinkTest.class.getResourceAsStream("test.jpg"));

    Item item = new TestItem();
    Content<?> c = item.createContent(Image.class).withData(img).save();

    FileSink.Settings s = new FileSink.Settings();
    s.setImageType(FileSink.Settings.ImageType.JPG);

    File f = FileSink.Processor.writeContent(c, tempDir, s);
    f.deleteOnExit();

    BufferedImage writtenImg = ImageIO.read(f);
    assertNotNull(writtenImg);
  }

  @Test
  public void testWriteContentTable() throws IOException {
    File tempDir = Files.createTempDirectory("filesinktest-").toFile();
    tempDir.deleteOnExit();

    Item item = new TestItem();
    Content<?> c =
        item.createContent(TableContent.class)
            .withData(
                new Table() {
                  private List<String> columns = List.of("id", "name", "qualified");

                  @Override
                  public int getColumnCount() {
                    return 3;
                  }

                  @Override
                  public int getRowCount() {
                    return 2;
                  }

                  @Override
                  public Optional<List<String>> getColumnNames() {
                    return Optional.of(columns);
                  }

                  @Override
                  public Stream<Row> getRows() {
                    return Stream.of(
                        new Row() {
                          @Override
                          public List<String> getColumnNames() {
                            return columns;
                          }

                          @Override
                          public int getColumnCount() {
                            return 3;
                          }

                          @Override
                          public int getRowIndex() {
                            return 0;
                          }

                          @Override
                          public Optional<Object> getValueAt(int index) {
                            switch (index) {
                              case 0:
                                return Optional.of(1);
                              case 1:
                                return Optional.of("SMITH, Alice");
                              case 2:
                                return Optional.of(true);
                              default:
                                return Optional.empty();
                            }
                          }
                        },
                        new Row() {
                          @Override
                          public List<String> getColumnNames() {
                            return columns;
                          }

                          @Override
                          public int getColumnCount() {
                            return 3;
                          }

                          @Override
                          public int getRowIndex() {
                            return 1;
                          }

                          @Override
                          public Optional<Object> getValueAt(int index) {
                            switch (index) {
                              case 0:
                                return Optional.of(2);
                              case 1:
                                return Optional.of("Bob");
                              case 2:
                                return Optional.of(false);
                              default:
                                return Optional.empty();
                            }
                          }
                        });
                  }
                })
            .save();

    File f = FileSink.Processor.writeContent(c, tempDir, new FileSink.Settings());
    f.deleteOnExit();

    String writtenContent = Files.readString(f.toPath());
    assertEquals("id,name,qualified\n1,\"SMITH, Alice\",true\n2,Bob,false", writtenContent);
  }

  @Test
  public void testBoundsToJsonPosition() {
    Bounds b = new PositionBounds(5);
    JsonObject jo = FileSink.Processor.boundsToJson(b).asJsonObject();

    assertEquals(5, jo.asJsonObject().getInt("position"));
  }

  @Test
  public void testBoundsToJsonSpan() {
    Bounds b = new SpanBounds(5, 10);
    JsonObject jo = FileSink.Processor.boundsToJson(b).asJsonObject();

    assertEquals(5, jo.getInt("begin"));
    assertEquals(10, jo.getInt("end"));
  }

  @Test
  public void testBoundsToJsonRectangle() {
    Bounds b = new RectangleBounds(5, 10, 15, 20);
    JsonObject jo = FileSink.Processor.boundsToJson(b).asJsonObject();

    assertEquals(5, jo.getInt("left"));
    assertEquals(10, jo.getInt("top"));
    assertEquals(15, jo.getInt("right"));
    assertEquals(20, jo.getInt("bottom"));
  }

  @Test
  public void testBoundsToJsonCell() {
    Bounds b = new CellBounds(5, 10);
    JsonObject jo = FileSink.Processor.boundsToJson(b).asJsonObject();

    assertEquals(5, jo.getInt("row"));
    assertEquals(10, jo.getInt("column"));
  }

  @Test
  public void testBoundsToJsonMultiCell() {
    Bounds b = new MultiCellBounds(5, new int[] {10, 15});
    JsonObject jo = FileSink.Processor.boundsToJson(b).asJsonObject();

    assertEquals(5, jo.getInt("row"));
    assertEquals(Json.createArrayBuilder(List.of(10, 15)).build(), jo.getJsonArray("columns"));
  }

  @Test
  public void testBoundsToJsonContent() {
    Bounds b = ContentBounds.getInstance();
    JsonValue jv = FileSink.Processor.boundsToJson(b);

    assertEquals(JsonValue.EMPTY_JSON_OBJECT, jv);
  }

  @Test
  public void testBoundsToJsonNo() {
    Bounds b = NoBounds.getInstance();
    JsonValue jv = FileSink.Processor.boundsToJson(b);

    assertEquals(JsonValue.EMPTY_JSON_OBJECT, jv);
  }

  @Test
  public void testBoundsToJsonUnsupported() {
    Bounds b =
        new Bounds() {
          @Override
          public <D, C extends Content<D>, R> Optional<R> getData(
              C content, Class<R> requiredClass) {
            return Optional.empty();
          }

          @Override
          public <D, C extends Content<D>> boolean isValid(C content) {
            return false;
          }
        };

    assertThrows(IllegalArgumentException.class, () -> FileSink.Processor.boundsToJson(b));
  }

  @Test
  public void testAnnotationsToJson() {
    Item item = new TestItem();
    Content<String> content = new TestStringContent(item);

    content
        .getAnnotations()
        .getBuilder()
        .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
        .withBounds(new SpanBounds(0, 5))
        .withProperty(PropertyKeys.PROPERTY_KEY_DESCRIPTION, "Example location")
        .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.5)
        .save();

    content
        .getAnnotations()
        .getBuilder()
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .withBounds(new SpanBounds(10, 15))
        .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.75)
        .save();

    JsonArray ja = FileSink.Processor.annotationsToJson(content.getAnnotations().getAll());
    assertEquals(2, ja.size());
    assertNotEquals(ja.get(0), ja.get(1));
  }

  @Test
  public void testAnnotationToJson() {
    Item item = new TestItem();
    Content<String> content = new TestStringContent(item);

    Annotation a =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
            .withBounds(new SpanBounds(0, 5))
            .withProperty(PropertyKeys.PROPERTY_KEY_DESCRIPTION, "Example location")
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.5)
            .save();

    JsonObject jo = FileSink.Processor.annotationToJson(a);

    assertEquals(AnnotationTypes.ANNOTATION_TYPE_LOCATION, jo.getString("type"));
    assertEquals(a.getId(), jo.getString("id"));
    assertEquals(a.getContentId(), jo.getString("contentId"));

    JsonObject bounds = jo.getJsonObject("bounds");
    assertEquals(0, bounds.getInt("begin"));
    assertEquals(5, bounds.getInt("end"));

    JsonObject props = jo.getJsonObject("properties");
    assertEquals("Example location", props.getString(PropertyKeys.PROPERTY_KEY_DESCRIPTION));
    assertEquals(Json.createValue(0.5), props.getJsonNumber(PropertyKeys.PROPERTY_KEY_PROBABILITY));
  }

  @Test
  public void testAnnotationToJsonBadBounds() {
    Item item = new TestItem();
    Content<String> content = new TestStringContent(item);

    Annotation a =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ENTITY_PREFIX + "test")
            .withBounds(
                new Bounds() {
                  @Override
                  public <D, C extends Content<D>, R> Optional<R> getData(
                      C content, Class<R> requiredClass) {
                    return Optional.empty();
                  }

                  @Override
                  public <D, C extends Content<D>> boolean isValid(C content) {
                    return false;
                  }
                })
            .save();

    JsonObject jo = FileSink.Processor.annotationToJson(a);
    assertNull(jo.get("bounds"));
  }

  @Test
  public void testGroupsToJson() {
    Item item = new TestItem();
    Content<String> content = new TestStringContent(item);

    Annotation a1 =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
            .withBounds(new SpanBounds(0, 5))
            .withProperty(PropertyKeys.PROPERTY_KEY_DESCRIPTION, "Example location")
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.5)
            .save();

    Annotation a2 =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .withBounds(new SpanBounds(10, 15))
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.75)
            .save();

    Annotation a3 =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .withBounds(new SpanBounds(20, 25))
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.75)
            .save();

    item.getGroups()
        .getBuilder()
        .withType(GroupTypes.GROUP_TYPE_EVENT)
        .withAnnotation(GroupRoles.GROUP_ROLE_LOCATION, a1)
        .withAnnotation(GroupRoles.GROUP_ROLE_PARTICIPANT, a2)
        .withProperty(PropertyKeys.PROPERTY_KEY_DESCRIPTION, "Example event")
        .save();

    item.getGroups()
        .getBuilder()
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_COREFERENCE)
        .withAnnotation(GroupRoles.GROUP_ROLE_LOCATION, a2)
        .withAnnotation(GroupRoles.GROUP_ROLE_PARTICIPANT, a3)
        .save();

    JsonArray ja = FileSink.Processor.groupsToJson(item.getGroups().getAll());
    assertEquals(2, ja.size());
    assertNotEquals(ja.get(0), ja.get(1));
  }

  @Test
  public void testGroupToJson() {
    Item item = new TestItem();
    Content<String> content = new TestStringContent(item);

    Annotation a1 =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
            .withBounds(new SpanBounds(0, 5))
            .withProperty(PropertyKeys.PROPERTY_KEY_DESCRIPTION, "Example location")
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.5)
            .save();

    Annotation a2 =
        content
            .getAnnotations()
            .getBuilder()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .withBounds(new SpanBounds(10, 15))
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, 0.75)
            .save();

    Group g =
        item.getGroups()
            .getBuilder()
            .withType(GroupTypes.GROUP_TYPE_EVENT)
            .withAnnotation(GroupRoles.GROUP_ROLE_LOCATION, a1)
            .withAnnotation(GroupRoles.GROUP_ROLE_PARTICIPANT, a2)
            .withProperty(PropertyKeys.PROPERTY_KEY_DESCRIPTION, "Example event")
            .save();

    JsonObject jo = FileSink.Processor.groupToJson(g);

    assertEquals(GroupTypes.GROUP_TYPE_EVENT, jo.getString("type"));
    assertEquals(g.getId(), jo.getString("id"));

    JsonObject annotations = jo.getJsonObject("annotations");
    assertNotNull(annotations.get(GroupRoles.GROUP_ROLE_LOCATION));
    assertNotNull(annotations.get(GroupRoles.GROUP_ROLE_PARTICIPANT));

    JsonObject props = jo.getJsonObject("properties");
    assertEquals("Example event", props.getString(PropertyKeys.PROPERTY_KEY_DESCRIPTION));
  }

  @Test
  public void testObjectToJsonInt() {
    assertEquals(Json.createValue(123), FileSink.Processor.objectToJson(123));
  }

  @Test
  public void testObjectToJsonLong() {
    assertEquals(Json.createValue(123L), FileSink.Processor.objectToJson(123L));
  }

  @Test
  public void testObjectToJsonBoolean() {
    assertEquals(JsonValue.TRUE, FileSink.Processor.objectToJson(true));
    assertEquals(JsonValue.FALSE, FileSink.Processor.objectToJson(false));
  }

  @Test
  public void testObjectToJsonDouble() {
    assertEquals(Json.createValue(123.0), FileSink.Processor.objectToJson(123.0));
  }

  @Test
  public void testObjectToJsonBigInteger() {
    assertEquals(
        Json.createValue(new BigInteger("1234567")),
        FileSink.Processor.objectToJson(new BigInteger("1234567")));
  }

  @Test
  public void testObjectToJsonBigDecimal() {
    assertEquals(
        Json.createValue(new BigDecimal("1234567.8")),
        FileSink.Processor.objectToJson(new BigDecimal("1234567.8")));
  }

  @Test
  public void testObjectToJsonString() {
    assertEquals(Json.createValue("Hello World!"), FileSink.Processor.objectToJson("Hello World!"));
  }

  @Test
  public void testObjectToJsonJsonValue() {
    assertEquals(Json.createValue(123), FileSink.Processor.objectToJson(Json.createValue(123)));
  }

  @Test
  public void testObjectToJsonNull() {
    assertEquals(JsonValue.NULL, FileSink.Processor.objectToJson(null));
  }

  @Test
  public void testObjectToJsonMap() {
    Map<String, Object> m = new HashMap<>();
    m.put("a", 1);
    m.put("b", 2);

    JsonObjectBuilder job = Json.createObjectBuilder();
    job.add("a", 1);
    job.add("b", 2);

    assertEquals(job.build(), FileSink.Processor.objectToJson(m));
  }

  @Test
  public void testObjectToJsonMapNested() {
    Map<Integer, Boolean> m2 = new HashMap<>();
    m2.put(1, true);

    Map<String, Object> m1 = new HashMap<>();
    m1.put("map", m2);

    JsonObjectBuilder job2 = Json.createObjectBuilder();
    job2.add("1", true);

    JsonObjectBuilder job1 = Json.createObjectBuilder();
    job1.add("map", job2);

    assertEquals(job1.build(), FileSink.Processor.objectToJson(m1));
  }

  @Test
  public void testObjectToJsonList() {
    List<Object> l = new ArrayList<>();
    l.add(123);
    l.add("abc");
    l.add(List.of("X", "Y", "Z"));

    JsonArrayBuilder jab = Json.createArrayBuilder();
    jab.add(123);
    jab.add("abc");

    JsonArrayBuilder jab2 = Json.createArrayBuilder();
    jab2.add("X");
    jab2.add("Y");
    jab2.add("Z");

    jab.add(jab2);

    assertEquals(jab.build(), FileSink.Processor.objectToJson(l));
  }

  @Test
  public void testCopyOriginalFileFile() throws IOException {
    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    Path p = FileSink.Processor.copyOriginalFile(tempFile, tempOutputDir.toPath());
    p.toFile().deleteOnExit();

    assertNotNull(p);

    byte[] b1 = Files.readAllBytes(tempFile.toPath());
    byte[] b2 = Files.readAllBytes(p);

    assertTrue(Arrays.equals(b1, b2));
  }

  @Test
  public void testCopyOriginalFilePath() throws IOException {
    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    Path p = FileSink.Processor.copyOriginalFile(tempFile.toPath(), tempOutputDir.toPath());
    p.toFile().deleteOnExit();

    assertNotNull(p);

    byte[] b1 = Files.readAllBytes(tempFile.toPath());
    byte[] b2 = Files.readAllBytes(p);

    assertTrue(Arrays.equals(b1, b2));
  }

  @Test
  public void testCopyOriginalFileString() throws IOException {
    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    Path p = FileSink.Processor.copyOriginalFile(tempFile.toString(), tempOutputDir.toPath());
    p.toFile().deleteOnExit();

    assertNotNull(p);

    byte[] b1 = Files.readAllBytes(tempFile.toPath());
    byte[] b2 = Files.readAllBytes(p);

    assertTrue(Arrays.equals(b1, b2));
  }

  @Test
  public void testCopyOriginalFileUri() throws IOException {
    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    Path p = FileSink.Processor.copyOriginalFile(tempFile.toURI(), tempOutputDir.toPath());
    p.toFile().deleteOnExit();

    assertNotNull(p);

    byte[] b1 = Files.readAllBytes(tempFile.toPath());
    byte[] b2 = Files.readAllBytes(p);

    assertTrue(Arrays.equals(b1, b2));
  }

  @Test
  public void testCopyOriginalFileNull() throws IOException {
    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    assertNull(FileSink.Processor.copyOriginalFile(null, tempOutputDir.toPath()));
  }

  @Test
  public void testCopyOriginalFileBad() throws IOException {
    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    assertNull(FileSink.Processor.copyOriginalFile(1234, tempOutputDir.toPath()));
  }

  @Test
  public void testCopyOriginalFileNotFound() throws IOException {
    File tempOutputDir = Files.createTempDirectory("filesinktest-").toFile();
    tempOutputDir.deleteOnExit();

    assertThrows(
        IOException.class,
        () ->
            FileSink.Processor.copyOriginalFile(
                "/my-non-existent-file.txt", tempOutputDir.toPath()));
  }

  @Test
  public void testCapabilities() {
    FileSink fs = new FileSink();
    Capabilities cap = fs.capabilities();

    assertNotNull(cap);
    assertTrue(cap.processes().count() > 0);
  }

  @Test
  public void testCreateComponent() {
    FileSink fs = new FileSink();
    Processor p = fs.createComponent(new SimpleContext(), new FileSink.Settings());

    assertNotNull(p);
    p.close();
  }

  @Test
  public void testSettings() {
    FileSink.Settings s = new FileSink.Settings();
    assertTrue(s.validate());

    s.setRootOutputFolder(null);
    assertFalse(s.validate());
    s.setRootOutputFolder(Path.of("/test/path"));
    assertEquals(Path.of("/test/path"), s.getRootOutputFolder());

    s.setPropertiesFilename("my-properties.json");
    assertEquals("my-properties.json", s.getPropertiesFilename());

    s.setContentFilename("my-content");
    assertEquals("my-content", s.getContentFilename());

    s.setAnnotationsFilename("my-annotations.json");
    assertEquals("my-annotations.json", s.getAnnotationsFilename());

    s.setGroupsFilename("my-groups.json");
    assertEquals("my-groups.json", s.getGroupsFilename());

    s.setImageType(null);
    assertFalse(s.validate());

    s.setImageType(FileSink.Settings.ImageType.JPG);
    assertEquals(FileSink.Settings.ImageType.JPG, s.getImageType());

    s.setImageType(FileSink.Settings.ImageType.PNG);
    assertEquals(FileSink.Settings.ImageType.PNG, s.getImageType());

    s.setBasePaths(null);
    assertFalse(s.validate());
    s.setBasePaths(List.of(Path.of("/test/path1"), Path.of("/test/path2")));
    assertEquals(List.of(Path.of("/test/path1"), Path.of("/test/path2")), s.getBasePaths());

    s.setCopyOriginalFile(true);
    assertTrue(s.isCopyOriginalFile());
    s.setCopyOriginalFile(false);
    assertFalse(s.isCopyOriginalFile());
  }

  @Test
  public void test() throws Exception {
    File tempFile = Files.createTempFile("filesinktest", ".txt").toFile();
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello World!");

    TestItem item = new TestItem();
    item.getProperties().set("val", "Item property");
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, tempFile);

    Content<?> c1 =
        item.createContent(Text.class)
            .withData("Test Content")
            .withProperty("val", "Text content property")
            .save();

    Annotation a1 =
        c1.getAnnotations()
            .getBuilder()
            .withBounds(new SpanBounds(0, 4))
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withProperty("wordIndex", 0)
            .save();

    Annotation a2 =
        c1.getAnnotations()
            .getBuilder()
            .withBounds(new SpanBounds(5, 12))
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withProperty("wordIndex", 1)
            .save();

    Annotation a3 =
        c1.getAnnotations()
            .getBuilder()
            .withBounds(new SpanBounds(13, 21))
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withProperty("wordIndex", 2)
            .save();

    BufferedImage img = ImageIO.read(FileSinkTest.class.getResourceAsStream("test.jpg"));

    Content<?> c2 = item.createContent(Image.class).withData(img).save();

    Annotation a4 =
        c2.getAnnotations()
            .getBuilder()
            .withBounds(ContentBounds.getInstance())
            .withType(AnnotationTypes.ENTITY_PREFIX + "squares")
            .withProperty("colours", List.of("red", "blue"))
            .save();

    item.getGroups()
        .getBuilder()
        .withType(GroupTypes.GROUP_PREFIX + "example")
        .withAnnotation("words", a1)
        .withAnnotation("words", a2)
        .withAnnotation("words", a3)
        .withAnnotation("shapes", a4)
        .save();

    Path tempRootDir = Files.createTempDirectory("file-sink-test");

    FileSink.Settings settings = new FileSink.Settings();
    settings.setRootOutputFolder(tempRootDir);
    settings.setBasePaths(List.of(Path.of("/test/example/")));
    settings.setCopyOriginalFile(true);

    FileSink.Processor processor = new FileSink.Processor(settings);
    processor.process(item);

    Path outputFolder = Path.of(tempRootDir.toString(), tempFile.toString());

    // Original file
    assertTrue(outputFolder.resolve(tempFile.getName()).toFile().exists());

    // Properties
    assertTrue(outputFolder.resolve(settings.getPropertiesFilename()).toFile().exists());

    // Groups
    assertTrue(outputFolder.resolve(settings.getPropertiesFilename()).toFile().exists());

    // Content
    assertTrue(
        outputFolder
            .resolve(c1.getId())
            .resolve(settings.getContentFilename() + ".txt")
            .toFile()
            .exists());
    assertTrue(
        outputFolder
            .resolve(c2.getId())
            .resolve(settings.getContentFilename() + ".jpg")
            .toFile()
            .exists());

    // Content Properties
    assertTrue(
        outputFolder
            .resolve(c1.getId())
            .resolve(settings.getPropertiesFilename())
            .toFile()
            .exists());
    assertFalse(
        outputFolder
            .resolve(c2.getId())
            .resolve(settings.getPropertiesFilename())
            .toFile()
            .exists());

    // Annotations
    assertTrue(
        outputFolder
            .resolve(c1.getId())
            .resolve(settings.getAnnotationsFilename())
            .toFile()
            .exists());
    assertTrue(
        outputFolder
            .resolve(c2.getId())
            .resolve(settings.getAnnotationsFilename())
            .toFile()
            .exists());

    // Delete temp directory
    /*Files.walk(tempRootDir)
    .sorted(Comparator.reverseOrder())
    .map(Path::toFile)
    .forEach(File::delete);*/
  }
}
