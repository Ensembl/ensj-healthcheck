#!/bin/sh

javac -classpath "lib/ensj.jar:build/" -d build src/org/ensembl/healthcheck/util/*.java src/org/ensembl/healthcheck/testcase/*.java src/org/ensembl/healthcheck/*.java

