#!/bin/sh

$JAVA_HOME/bin/javadoc -d doc/external_web_pages/javadoc src/org/ensembl/healthcheck/util/*.java src/org/ensembl/healthcheck/testcase/*.java src/org/ensembl/healthcheck/*.java
