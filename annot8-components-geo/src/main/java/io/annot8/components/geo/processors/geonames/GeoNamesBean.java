/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors.geonames;

import com.opencsv.bean.CsvBindAndSplitByPosition;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import java.time.LocalDate;
import java.util.List;

public class GeoNamesBean {
  /*
  GeoNames fields and their order:

  geonameid         : integer id of record in geonames database
  name              : name of geographical point (utf8) varchar(200)
  asciiname         : name of geographical point in plain ascii characters, varchar(200)
  alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
  latitude          : latitude in decimal degrees (wgs84)
  longitude         : longitude in decimal degrees (wgs84)
  feature class     : see http://www.geonames.org/export/codes.html, char(1)
  feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
  country code      : ISO-3166 2-letter country code, 2 characters
  cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
  admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
  admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
  admin3 code       : code for third level administrative division, varchar(20)
  admin4 code       : code for fourth level administrative division, varchar(20)
  population        : bigint (8 byte int)
  elevation         : in meters, integer
  dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
  timezone          : the iana timezone id (see file timeZone.txt) varchar(40)
  modification date : date of last modification in yyyy-MM-dd format
   */

  @CsvBindByPosition(position = 0)
  private Long geonameId;

  @CsvBindByPosition(position = 1)
  private String name;

  @CsvBindByPosition(position = 2)
  private String asciiName;

  @CsvBindAndSplitByPosition(position = 3, splitOn = ",", elementType = String.class)
  private List<String> alternateNames;

  @CsvBindByPosition(position = 4)
  private Double latitude;

  @CsvBindByPosition(position = 5)
  private Double longitude;

  @CsvBindByPosition(position = 6)
  private String featureClass;

  @CsvBindByPosition(position = 7)
  private String featureCode;

  @CsvBindByPosition(position = 8)
  private String countryCode;

  @CsvBindAndSplitByPosition(position = 9, splitOn = ",", elementType = String.class)
  private List<String> cc2;

  @CsvBindByPosition(position = 10)
  private String admin1Code;

  @CsvBindByPosition(position = 11)
  private String admin2Code;

  @CsvBindByPosition(position = 12)
  private String admin3Code;

  @CsvBindByPosition(position = 13)
  private String admin4Code;

  @CsvBindByPosition(position = 14)
  private Integer population;

  @CsvBindByPosition(position = 15)
  private Integer elevation;

  @CsvBindByPosition(position = 16)
  private Integer dem;

  @CsvBindByPosition(position = 17)
  private String timezone;

  @CsvBindByPosition(position = 18)
  @CsvDate("yyyy-MM-dd")
  private LocalDate modificationDate;

  public Long getGeonameId() {
    return geonameId;
  }

  public void setGeonameId(Long geonameId) {
    this.geonameId = geonameId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAsciiName() {
    return asciiName;
  }

  public void setAsciiName(String asciiName) {
    this.asciiName = asciiName;
  }

  public List<String> getAlternateNames() {
    return alternateNames;
  }

  public void setAlternateNames(List<String> alternateNames) {
    this.alternateNames = alternateNames;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public String getFeatureClass() {
    return featureClass;
  }

  public void setFeatureClass(String featureClass) {
    this.featureClass = featureClass;
  }

  public String getFeatureCode() {
    return featureCode;
  }

  public void setFeatureCode(String featureCode) {
    this.featureCode = featureCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public List<String> getCc2() {
    return cc2;
  }

  public void setCc2(List<String> cc2) {
    this.cc2 = cc2;
  }

  public String getAdmin1Code() {
    return admin1Code;
  }

  public void setAdmin1Code(String admin1Code) {
    this.admin1Code = admin1Code;
  }

  public String getAdmin2Code() {
    return admin2Code;
  }

  public void setAdmin2Code(String admin2Code) {
    this.admin2Code = admin2Code;
  }

  public String getAdmin3Code() {
    return admin3Code;
  }

  public void setAdmin3Code(String admin3Code) {
    this.admin3Code = admin3Code;
  }

  public String getAdmin4Code() {
    return admin4Code;
  }

  public void setAdmin4Code(String admin4Code) {
    this.admin4Code = admin4Code;
  }

  public Integer getPopulation() {
    return population;
  }

  public void setPopulation(Integer population) {
    this.population = population;
  }

  public Integer getElevation() {
    return elevation;
  }

  public void setElevation(Integer elevation) {
    this.elevation = elevation;
  }

  public Integer getDem() {
    return dem;
  }

  public void setDem(Integer dem) {
    this.dem = dem;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public LocalDate getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(LocalDate modificationDate) {
    this.modificationDate = modificationDate;
  }
}
