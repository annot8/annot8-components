# Annot8 spaCy Components

This module is based on the [spaCy Server](https://github.com/neelkamath/spacy-server) project (tested with v2),
and uses the APIs exposed by that Docker image to integrate spaCy into Annot8.

The client library for interacting with spaCy Server has been generated using [OpenAPI Generator](https://openapi-generator.tech/).
For details on updating the client to newer versions of the API, see the [Updating OpenAPI Client](#updating-openapi-client) section below.

## Updating OpenAPI Client

First, build an updated version of the client using OpenAPI Generator and the following command,
where <SPEC_YAML> is the path to the spaCy Server OpenAPI definition and <OUTPUT> is an output directory.

```
openapi-generator-cli generate -i <SPEC_YAML> -g java -o <OUTPUT> \
    --additional-properties=invokerPackage=org.openapi.spacy \
    --additional-properties=apiPackage=org.openapi.spacy.api \
    --additional-properties=modelPackage=org.openapi.spacy.model \
    --additional-properties=dateLibrary=java8 \
    --additional-properties=library=native
```

Now, delete the existing `src/main/java/org/` folder from the project folder, and replace with the equivalent `src/main/java/org/` folder from your generated API.

Find the following file, `src/main/java/org/openapi/spacy/model/PartsOfSpeechTags.java`, and find the `WhitespaceEnum` definition.
The API defined in the OpenAPI uses a symbol (`_`) which isn't allowed in Java 9 or later, so replace it with `SPACE`. e.g.

```
...

public enum WhitespaceEnum {
    SPACE(" "),    
    EMPTY("");

...
```

Try building the project - if there have been no dependency changes in the template, then it should build fine in which case the update is done.
If it doesn't build, then compare the dependencies of the generated `pom.xml` with those in the project `pom.xml` and make any necessary changes.