#!/bin/sh

# create build directory for class files if it's not there already
if [ ! -d build ]; then
  mkdir build
fi

$JAVA_HOME/bin/javac -classpath "lib/ensj.jar:build/" -d build src/org/ensembl/healthcheck/util/*.java src/org/ensembl/healthcheck/testcase/*.java src/org/ensembl/healthcheck/*.java

