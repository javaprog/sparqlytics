# SPARQLytics

Multidimensional Analytics for RDF Data

## Building SPARQLytics

SPARQLytics uses [Apache Maven](http://maven.apache.org/) as a build system.
It will take care of downloading the dependencies, so make sure you have an
internet connection when building for the first time. Start the build process
by issuing the following command in the project directory:
```
mvn install
```

## Running SPARQLytics

The build process produces the standalone artifact
`sparqlytics-<verion>-dist.jar`, which contains the SPARQLytics code together
with all dependencies. You can run it with the following command:
```
java -jar sparqlytics-<version>-dist.jar [<options>]
```

SPARQLytics supports to operation modes:

1. Interactive: waits for the user to input commands in the console and
2. Batch: processes commands from a file.

The following optional command line arguments can be specified to steer the
behavior of SPARQLytics:
* `-debug`: prints the generated SPARQL queries to the console,
* `-input`: reads commands from the specified file,
* `-output`: writes results to the specified file or directory, and
* `-outputFormat`: designates the result format.

If an output directory is specified, each measure computation will cause a
separate result file to be created in that directory. The date and time of the
request will be used as file name.
SPARQLytics supports RDF/XML, Turtle, and N3 as output formats.
