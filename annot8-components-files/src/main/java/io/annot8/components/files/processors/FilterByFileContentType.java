/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.j256.simplemagic.ContentType;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterByFileContentType
    extends AbstractProcessorDescriptor<
        FilterByFileContentType.Processor, FilterByFileContentType.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        // TODO: Make configurable which types it applies to?
        .withDeletesContent(FileContent.class)
        .withDeletesContent(InputStreamContent.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;
    private final ContentInfoUtil contentInfoUtil = new ContentInfoUtil();

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Content<?>> contentToRemove = new ArrayList<>();

      contentToRemove.addAll(
          item.getContents(FileContent.class)
              .filter(
                  fc -> {
                    try {
                      ContentInfo ci = contentInfoUtil.findMatch(fc.getData());
                      if (ci == null) {
                        return settings.isFilterNullContentTypes();
                      } else {
                        return settings.getContentTypes().contains(ci.getContentType())
                            == settings.isFilterMatchingContentTypes();
                      }
                    } catch (IOException e) {
                      log()
                          .error(
                              "Unable to read File {} to determine content type",
                              fc.getData().getName(),
                              e);
                      return false;
                    }
                  })
              .collect(Collectors.toList()));

      contentToRemove.addAll(
          item.getContents(InputStreamContent.class)
              .filter(
                  isc -> {
                    try {
                      ContentInfo ci = contentInfoUtil.findMatch(isc.getData());

                      if (ci == null) {
                        return settings.isFilterNullContentTypes();
                      } else {
                        return settings.getContentTypes().contains(ci.getContentType())
                            == settings.isFilterMatchingContentTypes();
                      }
                    } catch (IOException e) {
                      log()
                          .error(
                              "Unable to read InputStream {} to determine content type",
                              isc.getId(),
                              e);
                      return false;
                    }
                  })
              .collect(Collectors.toList()));

      if (!contentToRemove.isEmpty()) {
        log().debug("Removing {} Content", contentToRemove.size());
        contentToRemove.forEach(item::removeContent);
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<ContentType> contentTypes = List.of();
    private boolean filterMatchingContentTypes = true;
    private boolean filterNullContentTypes = true;

    @Override
    public boolean validate() {
      return contentTypes != null && !contentTypes.isEmpty();
    }

    @Description("The list of content types")
    public List<ContentType> getContentTypes() {
      return contentTypes;
    }

    public void setContentTypes(List<ContentType> contentTypes) {
      this.contentTypes = contentTypes;
    }

    @Description(
        "If true, then matching content types will be removed. If false, matching content types will be kept.")
    public boolean isFilterMatchingContentTypes() {
      return filterMatchingContentTypes;
    }

    public void setFilterMatchingContentTypes(boolean filterMatchingContentTypes) {
      this.filterMatchingContentTypes = filterMatchingContentTypes;
    }

    @Description("If true, then remove content where we can't detect the content type.")
    public boolean isFilterNullContentTypes() {
      return filterNullContentTypes;
    }

    public void setFilterNullContentTypes(boolean filterNullContentTypes) {
      this.filterNullContentTypes = filterNullContentTypes;
    }
  }
}
