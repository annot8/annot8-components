/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.data;

import java.util.function.Supplier;

import org.bson.Document;

import io.annot8.common.implementations.content.AbstractContentBuilder;
import io.annot8.common.implementations.content.AbstractContentBuilderFactory;
import io.annot8.common.implementations.stores.AnnotationStoreFactory;
import io.annot8.core.data.BaseItem;
import io.annot8.core.data.Content;
import io.annot8.core.properties.ImmutableProperties;
import io.annot8.core.stores.AnnotationStore;

public class MongoDocument implements Content<Document> {

  private final String id;
  private final String name;
  private final AnnotationStore annotations;
  private final ImmutableProperties properties;
  private final Document document;

  public MongoDocument(
      String id,
      String name,
      AnnotationStoreFactory annotationStoreFactory,
      ImmutableProperties properties,
      Document document) {
    this.id = id;
    this.name = name;
    this.annotations = annotationStoreFactory.create(this);
    this.properties = properties;
    this.document = document;
  }

  @Override
  public Document getData() {
    return document;
  }

  @Override
  public Class<Document> getDataClass() {
    return Document.class;
  }

  @Override
  public Class<? extends Content<Document>> getContentClass() {
    return MongoDocument.class;
  }

  @Override
  public AnnotationStore getAnnotations() {
    return annotations;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public ImmutableProperties getProperties() {
    return properties;
  }

  public static class Builder extends AbstractContentBuilder<Document, MongoDocument> {

    private AnnotationStoreFactory factory;

    public Builder(AnnotationStoreFactory annotationStoreFactory) {
      this.factory = annotationStoreFactory;
    }

    @Override
    protected MongoDocument create(
        String id, String name, ImmutableProperties properties, Supplier<Document> data) {
      return new MongoDocument(id, name, factory, properties, data.get());
    }
  }

  public static class BuilderFactory
      extends AbstractContentBuilderFactory<Document, MongoDocument> {

    private AnnotationStoreFactory annotationStoreFactory;

    protected BuilderFactory(AnnotationStoreFactory annotationStoreFactory) {
      super(Document.class, MongoDocument.class);
      this.annotationStoreFactory = annotationStoreFactory;
    }

    @Override
    public Content.Builder<MongoDocument, Document> create(BaseItem item) {
      return new Builder(annotationStoreFactory);
    }
  }
}
