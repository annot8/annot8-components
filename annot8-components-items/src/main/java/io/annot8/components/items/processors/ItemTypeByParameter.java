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
package io.annot8.components.items.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.PropertyKeys;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

@ComponentName("Item Type by Parameter")
@ComponentDescription("Explicitly sets the Item type")
@SettingsClass(ItemTypeByParameter.Settings.class)
public class ItemTypeByParameter extends AbstractProcessorDescriptor<ItemTypeByParameter.Processor, ItemTypeByParameter.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  public Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getType());
  }

  public static class Processor extends AbstractTextProcessor {

    private final String type;

    public Processor(String type) {
      this.type = type;
    }

    @Override
    protected void process(Text content) {
      if (type == null || type.isBlank())
        return;

      content.getItem().getProperties().set(PropertyKeys.PROPERTY_KEY_SUBTYPE, type);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private final String type;

    @JsonbCreator
    public Settings(@JsonbProperty("type") String type) {
      this.type = type;
    }

    @Description("Value to set type to")
    public String getType() {
      return type;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}

