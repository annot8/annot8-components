# Annot8 EasyOCR

This module provides an [EasyOCR](https://www.jaided.ai/easyocr/ base annot8 processor.
It processes **Images** and produces new **Text** content.

# Requirements

Annot8 EasyOCR consists of two components, the Annot8 EasyOCR Processor and the EasyOCR Server.

As EasyOCR is a python library we run it in a separate EasyOCR server and communicate through http.
The EasyOCR server can be run on the same system as Annot8 or a different system with http connectivity.

## Same host

The Annot8 EasyOCR processor can start it's own EasyOCR server that will run on the same system as the processor provided the host system has the following requirements:

- [Python 3](https://www.python.org/)
- [EasyOCR](https://www.jaided.ai/easyocr/)
- [Fast API](https://fastapi.tiangolo.com/)
- [Uvicorn](https://www.uvicorn.org/)

See the relevent project documentation for installation on your system.

## Different host

You may want to run the EasyOCR server on a different system with, say a better GPU. We provide a minimal docker based setup to enable running the EasyOCR Server separately, any other setup is out of scope for this introduction but the Dockerfile provides a reference.

Build the docker images using the following command:

```
docker build -t annot8-easyocr-server:latest server
```

Run the docker image using the following command:

```
docker run -p 8000:8000 annot8-easyocr-server:latest
```

# Models

The EasyOCR Server requires models for each language used. If the server has internet access it will download the models from the internet, if not you must provide them youself in the `~/.EasyOCR/model` directory. Models can be downloaded from the [Jaided.ai](https://www.jaided.ai/easyocr/modelhub/) website.

To provide models to the Docker EasyOCR Server you can use the following command:

```
docker run -p 8000:8000 -v <path to models>:/root/.EasyOCR/model/ annot8-easyocr-server:latest
```

## Testing

Two integration test suites are provided for testing the EasyOCR server but disabled by default.
To run these, remove the `@Disabled` from the relevent test class.

For the `LocalEasyOCRTest` the requirements must be insalled locally in order to run the server.

> Note these are installed in the annot8 project [devcontainer](https://github.com/annot8/devcontainer).

For the `RemoteEasyOCRTest` you must be running a EasyOCR Server to test against and set the url in the test appropriately.

This can be done, for example by running the docker image as above or, if requirements are installed localled, running the following command:

```
uvicorn server.ocr:app --host=0.0.0.0 --port=8080
```
