/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class MoneyTest {

  @Test
  public void testUK() {
    assertFoundMoney("The price is £3", 3.0, "GBP");
    assertFoundMoney("The price is £3.50", 3.5, "GBP");
    assertFoundMoney("The price is £3000", 3000.0, "GBP");
    assertFoundMoney("The price is £3,000", 3000.0, "GBP");
    assertFoundMoney("The price is £2,999.99", 2999.99, "GBP");
    assertFoundMoney("The price is £2999.99", 2999.99, "GBP");
    assertFoundMoney("The price is £2 999.99", 2999.99, "GBP");
    assertFoundMoney("The price is £2,999,999", 2999999.0, "GBP");
    assertFoundMoney("The price is £2,999,999.99", 2999999.99, "GBP");
    assertFoundMoney("The price is \u00A3100", 100.0, "GBP");
    assertFoundMoney("£47", 47.0, "GBP");
  }

  @Test
  public void testMultipliers() {
    assertFoundMoney("The price is £37 million", 37000000.0, "GBP");
    assertFoundMoney("The price is £37m", 37000000.0, "GBP");
    assertFoundMoney("The price is $20k", 20000.0, "USD");
    assertFoundMoney("The price is 47 thousand €", 47000.0, "EUR");
    assertFoundMoney("The price is 47 € thousand", 47000.0, "EUR");
  }

  @Test
  public void testUS() {
    assertFoundMoney("The price is $1,234,567.89", 1234567.89, "USD");
    assertFoundMoney("The price is $1 234 567.89", 1234567.89, "USD");
    assertFoundMoney("The price is \u0024100", 100.0, "USD");
  }

  @Test
  public void testEur() {
    assertFoundMoney("The price is 1€", 1.0, "EUR");
    assertFoundMoney("The price is 1 €", 1.0, "EUR");
    assertFoundMoney("The price is 1,23€", 1.23, "EUR");
    assertFoundMoney("The price is 1.23 €", 1.23, "EUR");
    assertFoundMoney("The price is 123 456,78€", 123456.78, "EUR");
    assertFoundMoney("The price is 123,456.78 €", 123456.78, "EUR");
    assertFoundMoney("The price is 123.456,78 €", 123456.78, "EUR");
    assertFoundMoney("The price is 123.456.789,99€", 123456789.99, "EUR");
    assertFoundMoney("The price is 123.456.789 €", 123456789.0, "EUR");
    assertFoundMoney("The price is 1.234 €", 1234.0, "EUR");
    assertFoundMoney("The price is \u20AC100", 100.0, "EUR");
  }

  @Test
  public void testOtherCountries() {
    assertFoundMoney("The price is ¥3000", 3000.0, "JPY");
    assertFoundMoney("The price is \u00A5100", 100.0, "JPY");
    assertFoundMoney("The price is SEK 47,000", 47000.0, "SEK");
    assertFoundMoney("The price is 47,000 IQD", 47000.0, "IQD");
    assertFoundMoney("The price is 47 Fr", 47.0, "CHF");
    assertFoundMoney("The price is Fr 47", 47.0, "CHF");
  }

  @Test
  public void testFractions() {
    assertFoundMoney("The price is 47¢", 0.47, "USD");
    assertFoundMoney("The price is $1.99¢", 1.99, "USD");
    assertFoundMoney("The price is 47c", 0.47, "EUR");
    assertFoundMoney("The price is 50p", 0.5, "GBP");
    assertFoundMoney("The price is £1.99p", 1.99, "GBP");
  }

  @Test
  public void testNotMoney() {
    assertNotFoundMoney("There were 4 of them");
    assertNotFoundMoney("There were 4.0 of them");
    assertNotFoundMoney("There were 4,000 of them");
    assertNotFoundMoney("There were 4 million of them");
  }

  private void assertFoundMoney(String inputString, double value, String currencyCode) {
    try (Processor moneyProcessor = new Money.Processor()) {
      TestItem testItem = new TestItem();
      TestStringContent content =
          testItem.createContent(TestStringContent.class).withData(inputString).save();
      moneyProcessor.process(testItem);
      ImmutableProperties properties =
          content.getAnnotations().getAll().findAny().orElseThrow().getProperties();
      assertEquals(value, properties.getOrDefault("value", 0.0));
      assertEquals(currencyCode, properties.getOrDefault("currencyCode", ""));
    }
  }

  private void assertNotFoundMoney(String inputString) {
    try (Processor moneyProcessor = new Money.Processor()) {
      TestItem testItem = new TestItem();
      TestStringContent content =
          testItem.createContent(TestStringContent.class).withData(inputString).save();
      moneyProcessor.process(testItem);

      assertEquals(0L, content.getAnnotations().getAll().count());
    }
  }
}
