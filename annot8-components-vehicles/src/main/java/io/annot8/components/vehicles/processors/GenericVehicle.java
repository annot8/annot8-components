/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.vehicles.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.Processor;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.DescribedWordToken;
import io.annot8.components.base.processors.MultiProcessor;
import io.annot8.components.stopwords.resources.NoOpStopwords;
import io.annot8.components.stopwords.resources.Stopwords;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.utils.text.PluralUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ComponentName("Generic Vehicle")
@ComponentDescription("Extracts vehicles (with descriptions) from text")
public class GenericVehicle extends AbstractProcessorDescriptor<MultiProcessor, NoSettings> {

  @Override
  protected MultiProcessor createComponent(Context context, NoSettings noSettings) {
    Stopwords sw;
    if (context == null || context.getResource(Stopwords.class).isEmpty()) {
      sw = new NoOpStopwords();
    } else {
      sw = context.getResource(Stopwords.class).get();
    }

    Set<String> roadVehicles =
        Set.of(
            "car",
            "motorcar",
            "hatchback",
            "bike",
            "bicycle",
            "motorbike",
            "motorcycle",
            "motorbicycle",
            "tricycle",
            "trike",
            "quadbike",
            "van",
            "truck",
            "lorry",
            "tanker",
            "bus",
            "taxi",
            "jeep",
            "4x4",
            "wagon",
            "automobile",
            "cab",
            "taxicab",
            "minibus",
            "coach",
            "digger",
            "bulldozer",
            "engine",
            "ambulance");
    Set<String> railVehicles =
        Set.of(
            "carriage",
            "locomotive",
            "tram",
            "tramcar",
            "streetcar"); // train not included as it is more often used to refer to training
    Set<String> maritimeVehicles =
        Set.of(
            "ship",
            "boat",
            "vessel",
            "ferry",
            "canoe",
            "kayak",
            "dinghy",
            "yacht",
            "lifeboat",
            "barge",
            "trawler",
            "tug",
            "pedalo",
            "raft",
            "motorboat",
            "motorship");
    Set<String> airVehicles =
        Set.of(
            "aircraft",
            "airliner",
            "plane",
            "aeroplane",
            "airplane",
            "biplane",
            "monoplane",
            "helicopter",
            "glider",
            "parachute",
            "jet",
            "jetpack",
            "balloon",
            "airship",
            "blimp",
            "dirigible",
            "ultralight",
            "hangglider",
            "floatplane",
            "autogyro");
    Set<String> spaceVehicles = Set.of("spacecraft", "spaceship", "satellite", "rover");
    Set<String> otherVehicles = Set.of("vehicle", "hovercraft", "skidoo");

    Set<String> descriptors =
        Set.of(
            // Vehicle type qualifiers
            "pickup",
            "articulated",
            "jumbo",
            "hire",
            "pool",
            "wheeled",
            "tracked",
            "passenger",
            "cargo",
            "research",
            "cruise",
            "container",
            "commercial",
            "civilian",
            "escort",
            "camper",
            "postal",
            "space",
            "interstellar",
            "planetary",
            "robotic",
            "semi",
            "safety",
            "dumper",
            "police",
            "fire",
            // Propulsion
            "motor",
            "motorised",
            "sail",
            "sailing",
            "nuclear",
            "diesel",
            "petrol",
            "gas",
            "hybrid",
            "steam",
            "electric",
            "solar",
            "powered",
            "rowing",
            // Descriptive words
            "old",
            "new",
            "big",
            "small",
            "rusty",
            "dusty",
            "dirty",
            "clean",
            "fast",
            "slow",
            "metal",
            "plastic",
            "wooden",
            "branded",
            "unbranded",
            "painted",
            "sports",
            "hot",
            "cold",
            "float",
            "floating",
            "submersible",
            "narrow",
            "thin",
            "wide",
            "long",
            "short",
            "inflatable",
            "dangerous",
            "unsafe",
            "safe",
            "decommissioned",
            // Colors
            "aqua",
            "aquamarine",
            "azure",
            "beige",
            "bisque",
            "black",
            "blue",
            "bronze",
            "brown",
            "chocolate",
            "copper",
            "coral",
            "crimson",
            "cyan",
            "forest",
            "fuchsia",
            "gold",
            "golden",
            "gray",
            "green",
            "grey",
            "indigo",
            "ivory",
            "khaki",
            "lavender",
            "lemon",
            "lime",
            "magenta",
            "maroon",
            "mint",
            "navy",
            "olive",
            "orange",
            "orchid",
            "peach",
            "pink",
            "plum",
            "purple",
            "red",
            "salmon",
            "silver",
            "tan",
            "teal",
            "turquoise",
            "violet",
            "white",
            "yellow",
            "dark",
            "dim",
            "light",
            "pale",
            "metallic");

    Processor road =
        new DescribedWordToken.Processor(
            sw,
            AnnotationTypes.ANNOTATION_TYPE_VEHICLE,
            PluralUtils.pluraliseSet(roadVehicles),
            descriptors,
            false,
            Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "road"));

    Processor rail =
        new DescribedWordToken.Processor(
            sw,
            AnnotationTypes.ANNOTATION_TYPE_VEHICLE,
            PluralUtils.pluraliseSet(railVehicles),
            descriptors,
            false,
            Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "rail"));

    Processor maritime =
        new DescribedWordToken.Processor(
            sw,
            AnnotationTypes.ANNOTATION_TYPE_VEHICLE,
            PluralUtils.pluraliseSet(maritimeVehicles),
            descriptors,
            false,
            Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "maritime"));

    Processor air =
        new DescribedWordToken.Processor(
            sw,
            AnnotationTypes.ANNOTATION_TYPE_VEHICLE,
            PluralUtils.pluraliseSet(airVehicles),
            descriptors,
            false,
            Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "air"));

    Processor space =
        new DescribedWordToken.Processor(
            sw,
            AnnotationTypes.ANNOTATION_TYPE_VEHICLE,
            PluralUtils.pluraliseSet(spaceVehicles),
            descriptors,
            false,
            Map.of(PropertyKeys.PROPERTY_KEY_SUBTYPE, "space"));

    Processor other =
        new DescribedWordToken.Processor(
            sw,
            AnnotationTypes.ANNOTATION_TYPE_VEHICLE,
            PluralUtils.pluraliseSet(otherVehicles),
            descriptors,
            false,
            Collections.emptyMap());

    return new MultiProcessor(road, rail, maritime, air, space, other);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_VEHICLE, SpanBounds.class)
        .build();
  }
}
