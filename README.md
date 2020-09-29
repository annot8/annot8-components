# Annot8 Components

This repository contains components and utilities for use with the [Annot8 Data Processing Framework](https://github.com/annot8).

## Building

Build and install locally with:

```shell script
mvn clean install
```

If you wish to build a set of JAR files (one per module) which contain all the dependencies of the project.

```
mvn -Pplugin clean package
```

Note, the above commands do not build the `annot8-conventions` project, which whilst also residing in this repository is currently a standalone project.
To build the `annot8-conventions` project, run:

```shell script
cd annot8-conventions
mvn clean install
```

## Deploying to Maven central

To deploy to Maven Central (assuming you have sufficient permissions):

```shell script
mvn -Prelease deploy
``` 

As above, this command should be run separately for the Annot8 Conventions (`annot8-conventions`) project.

## License

Code in this repository is licenced under the Apache Software Licence v2, a copy of which can be found in the LICENCE file.
See the NOTICE file for any additional restrictions.