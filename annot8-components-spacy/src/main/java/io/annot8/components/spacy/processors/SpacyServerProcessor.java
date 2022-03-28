/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import org.openapi.spacy.ApiClient;
import org.openapi.spacy.api.NlpApi;

public abstract class SpacyServerProcessor extends AbstractTextProcessor {
  protected final NlpApi client;

  protected static final Map<String, String> nerLabelMapping = new HashMap<>();

  static {
    // Commented out labels don't have a conventional mapping, and will be dynamically generated

    // labelMapping.put("CARDINAL", "");
    nerLabelMapping.put("DATE", AnnotationTypes.ANNOTATION_TYPE_TEMPORAL);
    // labelMapping.put("EVENT", "");
    // labelMapping.put("FAC", "");
    nerLabelMapping.put("GPE", AnnotationTypes.ANNOTATION_TYPE_GEOPOLITICALENTITY);
    nerLabelMapping.put("LANGUAGE", AnnotationTypes.ANNOTATION_TYPE_LANGUAGE);
    // labelMapping.put("LAW", "");
    nerLabelMapping.put("LOC", AnnotationTypes.ANNOTATION_TYPE_LOCATION);
    nerLabelMapping.put("MONEY", AnnotationTypes.ANNOTATION_TYPE_MONEY);
    // labelMapping.put("NORP", "");
    // labelMapping.put("ORDINAL", "");
    nerLabelMapping.put("ORG", AnnotationTypes.ANNOTATION_TYPE_ORGANISATION);
    nerLabelMapping.put("PERCENT", AnnotationTypes.ANNOTATION_TYPE_PERCENT);
    nerLabelMapping.put("PERSON", AnnotationTypes.ANNOTATION_TYPE_PERSON);
    // labelMapping.put("PRODUCT", "");
    nerLabelMapping.put("QUANTITY", AnnotationTypes.ANNOTATION_TYPE_QUANTITY);
    nerLabelMapping.put("TIME", AnnotationTypes.ANNOTATION_TYPE_TEMPORAL);
    // labelMapping.put("WORK_OF_ART", "");
  }

  protected SpacyServerProcessor(SpacyServerSettings settings) {
    HttpClient.Builder builder =
        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1); // Version
    // 2
    // not
    // supported
    // by
    // server

    ApiClient apiClient = new ApiClient();
    apiClient.updateBaseUri(settings.getBaseUri());
    apiClient.setHttpClientBuilder(builder);

    client = new NlpApi(apiClient);
  }

  protected static org.openapi.spacy.model.Text fromTextContent(Text content) {
    org.openapi.spacy.model.Text t = new org.openapi.spacy.model.Text();
    t.setText(content.getData());

    return t;
  }

  public static String toNerLabel(String spacyLabel) {
    if (spacyLabel == null || spacyLabel.isEmpty()) {
      return AnnotationTypes.ANNOTATION_TYPE_ENTITY;
    }

    return nerLabelMapping.getOrDefault(
        spacyLabel, AnnotationTypes.ENTITY_PREFIX + spacyLabel.toLowerCase());
  }
}
