/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Money")
@ComponentDescription("Extracts money amounts from text")
public class Money extends AbstractProcessorDescriptor<Money.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings noSettings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_MONEY, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  // Deep type hierarchy
  @SuppressWarnings("java:S110")
  public static class Processor extends AbstractRegexProcessor {
    private static final String NUMBER_REGEX =
        "(?<number>[0-9]{1,3}"
            + "((?<thousand>[ ,.])?[0-9]{3})?(\\k<thousand>?[0-9]{3})*"
            + "(?<decimal>[,.])?[0-9]*"
            + ")";
    private static final String CURRENCY_CODES =
        "AED|AFN|ALL|AMD|ANG|AOA|ARS|AUD|AWG|AZN|"
            + "BAM|BBD|BDT|BGN|BHD|BIF|BMD|BND|BOB|BOV|BRL|BSD|BTN|BWP|BYR|BZD|"
            + "CAD|CDF|CHE|CHF|CHW|CLF|CLP|CNY|COP|COU|CRC|CUC|CUP|CVE|CZK|"
            + "DJF|DKK|DOP|DZD|"
            + "EGP|ERN|ETB|EUR|"
            + "FJD|FKP|"
            + "GBP|GEL|GHS|GIP|GMD|GNF|GTQ|GYD|"
            + "HKD|HNL|HRK|HTG|HUF|"
            + "IDR|ILS|INR|IQD|IRR|ISK|"
            + "JMD|JOD|JPY|"
            + "KES|KGS|KHR|KMF|KPW|KRW|KWD|KYD|KZT|"
            + "LAK|LBP|LKR|LRD|LSL|LYD|"
            + "MAD|MDL|MGA|MKD|MMK|MNT|MOP|MRO|MUR|MVR|MWK|MXN|MXV|MYR|MZN|"
            + "NAD|NGN|NIO|NOK|NPR|NZD|"
            + "OMR|"
            + "PAB|PEN|PGK|PHP|PKR|PLN|PYG|"
            + "QAR|"
            + "RON|RSD|RUB|RWF|"
            + "SAR|SBD|SCR|SDG|SEK|SGD|SHP|SLL|SOS|SRD|SSP|STD|SYP|SZL|"
            + "THB|TJS|TMT|TND|TOP|TRY|TTD|TWD|TZS|"
            + "UAH|UGX|USD|USN|USS|UYI|UYU|UZS|"
            + "VEF|VND|VUV|"
            + "WST|"
            + "XAF|XAG|XAU|XBA|XBB|XBC|XBD|XCD|XDR|XFU|XOF|XPD|XPF|XPT|XSU|XTS|XUA|XXX|"
            + "YER|"
            + "ZAR|ZMW";
    private static final String CURRENCY_SYMBOLS = "[£$€¥]|Fr";
    private static final String CURRENCY_SYMBOLS_FRACTIONS = "[p¢c]";
    private static final Map<String, String> symbolCountries =
        Map.ofEntries(
            Map.entry("£", "GBP"),
            Map.entry("$", "USD"),
            Map.entry("€", "EUR"),
            Map.entry("¥", "JPY"),
            Map.entry("Fr", "CHF"),
            Map.entry("p", "GBP"),
            Map.entry("¢", "USD"),
            Map.entry("c", "EUR"));
    private static final String MULTIPLIERS = "k|thousand|million|m|billion|b|trillion|t";
    private static final Map<String, Double> multiplierValues =
        Map.ofEntries(
            Map.entry("k", 1000.0),
            Map.entry("thousand", 1000.0),
            Map.entry("million", 1000000.0),
            Map.entry("m", 1000000.0),
            Map.entry("billion", 1000000000.0),
            Map.entry("b", 1000000000.0),
            Map.entry("trillion", 1000000000000.0),
            Map.entry("t", 1000000000000.0));
    private static final String WHITESPACE = "\\h*";
    private static final String START = "(?<=^|\\(|\\s)";
    private static final String END = "(?=$|\\)|\\?|!|\\s|[.,](\\s|$))";

    private static final String MONEY_REGEX =
        START
            + "("
            + "(?<code1>"
            + CURRENCY_CODES
            + ")"
            + "|"
            + "(?<symbol1>"
            + CURRENCY_SYMBOLS
            + ")"
            + ")?("
            + WHITESPACE
            + NUMBER_REGEX
            + ")("
            + WHITESPACE
            + "(?<multiplier1>"
            + MULTIPLIERS
            + ")"
            + ")?("
            + WHITESPACE
            + "("
            + "(?<code2>"
            + CURRENCY_CODES
            + ")"
            + "|"
            + "(?<symbol2>"
            + CURRENCY_SYMBOLS
            + ")"
            + "|"
            + "(?<fraction>"
            + CURRENCY_SYMBOLS_FRACTIONS
            + ")"
            + ")"
            + ")?("
            + WHITESPACE
            + "("
            + "(?<multiplier2>"
            + MULTIPLIERS
            + ")"
            + "))?"
            + END;

    public Processor() {
      super(Pattern.compile(MONEY_REGEX), 0, AnnotationTypes.ANNOTATION_TYPE_MONEY);
    }

    @Override
    protected boolean acceptMatch(Matcher m) {
      return !getCurrencyCode(m).isEmpty();
    }

    @Override
    protected void addProperties(Annotation.Builder builder, Matcher m) {
      Double value = calculateValue(m);
      if (value != null) {
        builder.withProperty("value", calculateValue(m));
      }

      String currencyCode = getCurrencyCode(m);
      if (currencyCode != null && !currencyCode.isEmpty()) {
        builder.withProperty("currencyCode", currencyCode);
      }
    }

    private Double calculateValue(Matcher m) {
      double value;
      try {
        value = Double.parseDouble(normaliseMatchedNumber(m));
      } catch (NumberFormatException nfe) {
        log().warn("Unable to parse value", nfe);
        return null;
      }
      value = applyMultipliers(m, value);
      value = applyFractionModifier(m, value);
      return value;
    }

    private double applyMultipliers(Matcher m, double value) {
      String multiplier = getNamedGroup(m, "multiplier");
      if (multiplier == null) {
        return value;
      }
      return value * multiplierValues.getOrDefault(multiplier, 1.0);
    }

    private double applyFractionModifier(Matcher m, double value) {
      if (m.group("fraction") == null) {
        return value;
      } else if (getNamedGroup(m, "symbol") == null) {
        return value / 100.0;
      }
      return value;
    }

    private String getCurrencyCode(Matcher m) {
      String namedCode = getNamedGroup(m, "code");
      if (namedCode != null) {
        return namedCode;
      }
      String symbol = getSymbol(m);
      if (symbol == null) {
        return "";
      }
      return symbolCountries.getOrDefault(symbol, "");
    }

    private String getSymbol(Matcher m) {
      String symbol = getNamedGroup(m, "symbol");
      if (symbol == null) {
        return m.group("fraction");
      }
      return symbol;
    }

    private String getNamedGroup(Matcher m, String groupName) {
      if (m.group(groupName + "1") != null) {
        return m.group(groupName + "1");
      } else {
        return m.group(groupName + "2");
      }
    }

    private String normaliseMatchedNumber(Matcher m) {
      String number = m.group("number");
      if (m.group("thousand") != null) {
        number = number.replace(m.group("thousand"), "");
      }
      if (m.group("decimal") != null) {
        number = number.replace(m.group("decimal"), ".");
      }
      return number;
    }
  }
}
