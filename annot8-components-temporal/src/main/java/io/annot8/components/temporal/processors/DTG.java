/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.temporal.processors;

import io.annot8.api.annotations.Annotation.Builder;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.components.temporal.processors.utils.DateTimeUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotate DTG (Date Time Groups) within a document using regular expressions
 *
 * <p>The document content is run through a regular expression matcher looking for things that match
 * the following regular expression:
 *
 * <pre>
 * ([0-9]{2})\\s*([0-9]{2})([0-9]{2})([A-IK-Z]|D\\*)\\s*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\s*([0-9]{2})
 * </pre>
 *
 * <p>Matched DTGs are parsed as a date and annotated as Temporal entities.
 */
@ComponentName("Date Time Group")
@ComponentDescription("Extracts date time groups from text using regular expressions")
public class DTG extends AbstractProcessorDescriptor<DTG.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    private static final Map<String, ZoneOffset> zoneMap = createTimeCodeMap();

    public Processor() {
      super(
          Pattern.compile(
              "([0-9]{2})\\s*([0-9]{2})([0-9]{2})([A-IK-Z]|D\\*)\\s*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\s*([0-9]{2})",
              Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT);
    }

    @Override
    protected void addProperties(Builder builder, Matcher matcher) {
      ZonedDateTime zdt = parseMatch(matcher);
      builder.withProperty(PropertyKeys.PROPERTY_KEY_VALUE, zdt);
    }

    protected ZonedDateTime parseMatch(Matcher matcher) {
      ZonedDateTime zdt;
      try {
        zdt =
            ZonedDateTime.of(
                2000 + Integer.parseInt(matcher.group(6)),
                DateTimeUtils.asMonth(matcher.group(5)).getValue(),
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                0,
                0,
                militaryTimeCodeToOffset(matcher.group(4)));
      } catch (DateTimeException dte) {
        log().warn("Unable to parse DTG", dte);
        return null;
      }

      return zdt;
    }

    @Override
    protected boolean acceptMatch(Matcher matcher) {
      ZonedDateTime zdt = parseMatch(matcher);
      return null != zdt;
    }

    private static Map<String, ZoneOffset> createTimeCodeMap() {
      Map<String, ZoneOffset> map = new HashMap<>();
      map.put("A", ZoneOffset.ofHours(1));
      map.put("B", ZoneOffset.ofHours(2));
      map.put("C", ZoneOffset.ofHours(3));
      map.put("D", ZoneOffset.ofHours(4));
      map.put("D*", ZoneOffset.ofHoursMinutes(4, 30));
      map.put("E", ZoneOffset.ofHours(5));
      map.put("F", ZoneOffset.ofHours(6));
      map.put("G", ZoneOffset.ofHours(7));
      map.put("H", ZoneOffset.ofHours(8));
      map.put("I", ZoneOffset.ofHours(9));
      map.put("K", ZoneOffset.ofHours(10));
      map.put("L", ZoneOffset.ofHours(11));
      map.put("M", ZoneOffset.ofHours(12));
      map.put("N", ZoneOffset.ofHours(-1));
      map.put("O", ZoneOffset.ofHours(-2));
      map.put("P", ZoneOffset.ofHours(-3));
      map.put("Q", ZoneOffset.ofHours(-4));
      map.put("R", ZoneOffset.ofHours(-5));
      map.put("S", ZoneOffset.ofHours(-6));
      map.put("T", ZoneOffset.ofHours(-7));
      map.put("U", ZoneOffset.ofHours(-8));
      map.put("V", ZoneOffset.ofHours(-9));
      map.put("W", ZoneOffset.ofHours(-10));
      map.put("X", ZoneOffset.ofHours(-11));
      map.put("Y", ZoneOffset.ofHours(-12));

      return map;
    }

    private static ZoneOffset militaryTimeCodeToOffset(String timeCode) {
      return zoneMap.getOrDefault(timeCode.toUpperCase(), ZoneOffset.UTC);
    }
  }
}
