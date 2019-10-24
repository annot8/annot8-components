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

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UnqualifiedDateTest {

    @Test
    public void testCapabilities() {
        UnqualifiedDate n = new UnqualifiedDate();

        // Get the capabilities and check that we have the expected number
        Capabilities c = n.capabilities();
        assertEquals(1, c.creates().count());
        assertEquals(1, c.processes().count());
        assertEquals(0, c.deletes().count());

        // Check that we're creating an Annotation and that it has the correct definitions
        AnnotationCapability annotCap = c.creates(AnnotationCapability.class).findFirst().get();
        assertEquals(SpanBounds.class, ((AnnotationCapability) annotCap).getBounds());
        assertEquals(
                AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, ((AnnotationCapability) annotCap).getType());

        // Check that we're processing a Content and that it has the correct definitions
        ContentCapability contentCap = c.processes(ContentCapability.class).findFirst().get();
        assertEquals(Text.class, ((ContentCapability) contentCap).getType());
    }

    @Test
    public void testCreateComponent() {
        UnqualifiedDate n = new UnqualifiedDate();

        // Test that we actually get a component when we create it
        UnqualifiedDate.Processor np = n.createComponent(null, new UnqualifiedDate.Settings());
        assertNotNull(np);
    }

    List<Annotation> getSortedAnnotations(Text content)
    {

        AnnotationStore store = content.getAnnotations();

        List<Annotation> annotations =
            store
                .getAll()
                .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
                .collect(Collectors.toList());

        return annotations;

    }

    void testAnnotation(Text content, String expected, Annotation a){

        Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, a.getType());
        Assertions.assertEquals(content.getId(), a.getContentId());
        Assertions.assertEquals(expected, a.getBounds().getData(content).get());
        Assertions.assertEquals(0, a.getProperties().getAll().size());

    }

    void testAnnotationCount(Integer count, List<Annotation> annotations){

        Assertions.assertEquals(count, annotations.size());

    }

    @Test
    public void testMonths() throws Exception {

        try (Processor p = new UnqualifiedDate.Processor(false)) {
            Item item = new TestItem();

            Text content = item
                .createContent(TestStringContent.class)
                .withData("It happened on the 15th October, Tuesday 11 Oct, and in September, but not on Wednesday 5 October 2016 or 3 Oct '16 or in January 2014. 15th of October was the last day it happened.")
                .save();

            p.process(item);

            List<Annotation> annotations =
                getSortedAnnotations(content);

            testAnnotationCount(4, annotations);

            int i=0;
            testAnnotation(content,"15th October", annotations.get(i++));

            testAnnotation(content,"Tuesday 11 Oct", annotations.get(i++));

            testAnnotation(content,"September", annotations.get(i++));

            testAnnotation(content,"15th of October", annotations.get(i++));
        }
    }

    @Test
    public void testDays() throws Exception {

        try (Processor p = new UnqualifiedDate.Processor(false)) {
            Item item = new TestItem();

            Text content = item
                .createContent(TestStringContent.class)
                .withData("Monday, Tuesday 11th, Wednesday 12th October, Thursday 13th October 2016, Fri 14 Oct, Sat 15 Oct 16, Thu 12th Oct")
                .save();

            p.process(item);

            List<Annotation> annotations =
                getSortedAnnotations(content);

            testAnnotationCount(5, annotations);

            int i=0;
            testAnnotation(content,"Monday", annotations.get(i++));

            testAnnotation(content,"Tuesday 11th", annotations.get(i++));

            testAnnotation(content,"Wednesday 12th October", annotations.get(i++));

            testAnnotation(content,"Fri 14 Oct", annotations.get(i++));

            testAnnotation(content,"Thu 12th Oct", annotations.get(i++));
        }
    }

    @Test
    public void testMonthsLowerCase() throws Exception {



        try (Processor p = new UnqualifiedDate.Processor(false)) {
            Item item = new TestItem();
            Text content = item
                .createContent(TestStringContent.class)
                .withData("It happened on the 15th october, tuesday 11 oct, and in september, but not on wednesday 5 october 2016 or 3 Oct '16 or in january 2014. 15th of october was the last day it happened.")
                .save();
            p.process(item);

            List<Annotation> annotations =
                getSortedAnnotations(content);

            testAnnotationCount(0, annotations);

        }

        try (Processor p = new UnqualifiedDate.Processor(true)) {
            Item item = new TestItem();
            Text content = item
                .createContent(TestStringContent.class)
                .withData("It happened on the 15th october, tuesday 11 oct, and in september, but not on wednesday 5 october 2016 or 3 Oct '16 or in january 2014. 15th of october was the last day it happened.")
                .save();
            p.process(item);

            List<Annotation> annotations =
                getSortedAnnotations(content);

            testAnnotationCount(4, annotations);

            int i=0;

            testAnnotation(content,"15th october", annotations.get(i++));

            testAnnotation(content,"tuesday 11 oct", annotations.get(i++));

            testAnnotation(content,"september", annotations.get(i++));

            testAnnotation(content,"15th of october", annotations.get(i++));
        }
    }

    @Test
    public void testDaysLowerCase() throws Exception {



        try (Processor p = new UnqualifiedDate.Processor(false)) {

            Item item = new TestItem();
            Text content = item
                .createContent(TestStringContent.class)
                .withData("monday, tuesday 11th, wednesday 12th october, thursday 13th october 2016, fri 14 Oct, sat 15 oct 16")
                .save();
            p.process(item);

            List<Annotation> annotations =
                getSortedAnnotations(content);

            testAnnotationCount(0, annotations);

        }

        try (Processor p = new UnqualifiedDate.Processor(true)) {
            Item item = new TestItem();
            Text content = item
                .createContent(TestStringContent.class)
                .withData("monday, tuesday 11th, wednesday 12th october, thursday 13th october 2016, fri 14 oct, sat 15 oct 16")
                .save();

            p.process(item);

            List<Annotation> annotations =
                getSortedAnnotations(content);

            testAnnotationCount(4, annotations);

            int i=0;

            testAnnotation(content,"monday", annotations.get(i++));

            testAnnotation(content,"tuesday 11th", annotations.get(i++));

            testAnnotation(content,"wednesday 12th october", annotations.get(i++));

            testAnnotation(content,"fri 14 oct", annotations.get(i++));
        }

    }
}
