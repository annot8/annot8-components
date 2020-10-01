/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import javax.json.bind.annotation.JsonbTransient;
import org.apache.http.HttpHost;

public class ElasticsearchSettings implements Settings {
  private String hostname = "localhost";
  private int port = 9200;
  private String scheme = "http";
  private String index = "baleen";

  public ElasticsearchSettings() {
    // Do nothing - use default values
  }

  public ElasticsearchSettings(String hostname, int port, String scheme, String index) {
    this.hostname = hostname;
    this.port = port;
    this.scheme = scheme;
    this.index = index;
  }

  @Override
  public boolean validate() {
    return hostname != null
        && !hostname.isEmpty()
        && port >= 0
        && port <= 65535
        && scheme != null
        && !scheme.isEmpty()
        && index != null
        && !index.isEmpty();
  }

  @JsonbTransient
  public HttpHost getHost() {
    return new HttpHost(hostname, port, scheme);
  }

  @Description(value = "The hostname of the Elasticsearch server", defaultValue = "localhost")
  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Description(value = "The port of the Elasticsearch server", defaultValue = "9200")
  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Description(
      value =
          "The scheme over which to communicate with the Elasticsearch server (e.g. http or https)",
      defaultValue = "http")
  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  @Description(value = "The Elasticsearch index to use", defaultValue = "baleen")
  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }
}