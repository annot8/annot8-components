/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import java.util.List;

import org.bson.Document;
import org.mockito.ArgumentMatcher;

public class DocumentListArgMatcher implements ArgumentMatcher<List<Document>> {

  private List<Document> documents;

  public DocumentListArgMatcher(List<Document> documents) {
    this.documents = documents;
  }

  @Override
  public boolean matches(List<Document> argument) {
    if (argument == null) {
      return false;
    }
    if (documents.size() != argument.size()) {
      return false;
    }
    for (Document doc : argument) {
      if (!documents.contains(doc)) {
        return false;
      }
    }

    return true;
  }
}
