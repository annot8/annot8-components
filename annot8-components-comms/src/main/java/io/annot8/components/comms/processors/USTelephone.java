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
package io.annot8.components.comms.processors;

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
import io.annot8.conventions.AnnotationTypes;

import java.util.regex.Pattern;

@ComponentName("US Telephone Number")
@ComponentDescription("Extract valid US-style Telephone numbers (e.g. 1-800-CALLME) from text")
public class USTelephone extends AbstractProcessorDescriptor<USTelephone.Processor, NoSettings>  {

    @Override
    protected Processor createComponent(Context context, NoSettings settings) {
        return new Processor();
    }

    @Override
    public Capabilities capabilities() {
        return new SimpleCapabilities.Builder()
                .withProcessesContent(Text.class)
                .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, SpanBounds.class)
                .build();
    }

    public static class Processor extends AbstractRegexProcessor {
        public Processor() {
            super(
                    Pattern.compile("((\\(?\\+?1\\)?[-. ])?\\(?([2-9]|two|three|four|five|six|seven|eight|nine)([0-9]|zero|one|two|three|four|five|six|seven|eight|nine){2}\\)?[-. ]([2-9]|two|three|four|five|six|seven|eight|nine)([0-9]|zero|one|two|three|four|five|six|seven|eight|nine){2}[-. ]([0-9]|zero|one|two|three|four|five|six|seven|eight|nine){4}|1-800-[A-Z]{7})\\b", Pattern.CASE_INSENSITIVE),
                    0,
                    AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER);
        }
    }
}
