/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.data;

import java.util.function.Supplier;

import org.bson.Document;

import io.annot8.common.implementations.content.AbstractContentBuilder;
import io.annot8.common.implementations.content.AbstractContentBuilderFactory;
import io.annot8.common.implementations.stores.AnnotationStoreFactory;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.api.stores.AnnotationStore;

public class MongoDocument implements Content<Document> {

  private Item item;
  private final String id;
  private final String description;
  private final AnnotationStore annotations;
  private final ImmutableProperties properties;
  private final Document document;

  public MongoDocument(
      Item item,
      String id,
      String description,
      AnnotationStoreFactory annotationStoreFactory,
      ImmutableProperties properties,
      Document document) {
    this.item = item;
    this.id = id;
    this.description = description;
    this.annotations = annotationStoreFactory.create(this);
    this.properties = properties;
    this.document = document;
  }

  @Override
  public Item getItem() {
    return item;
  }

  @Override
  public String getDescription() {
    return description;
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
  public String getId() {
    return id;
  }

  @Override
  public ImmutableProperties getProperties() {
    return properties;
  }

  public static class Builder extends AbstractContentBuilder<Document, MongoDocument> {

    private AnnotationStoreFactory factory;

    public Builder(Item item, AnnotationStoreFactory annotationStoreFactory) {
      super(item);
      this.factory = annotationStoreFactory;
    }

    @Override
    protected MongoDocument create(
        String id, String description, ImmutableProperties properties, Supplier<Document> data) {
      return new MongoDocument(getItem(), id, description, factory, properties, data.get());
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
    public Content.Builder<MongoDocument, Document> create(Item item) {
      return new Builder(item, annotationStoreFactory);
    }
  }
}
