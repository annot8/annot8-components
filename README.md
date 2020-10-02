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

## Deploying to Maven central

To deploy to Maven Central (assuming you have sufficient permissions):

```shell script
mvn -Prelease clean deploy
```

## License

Code in this repository is licenced under the Apache Software Licence v2, a copy of which can be found in the LICENCE file.
See the NOTICE file for any additional restrictions.
