#!/bin/sh

$JAVA_HOME/bin/java -classpath "lib/ensj.jar:lib/mysql-connector-java-3.0.8-stable-bin.jar:build/" org.ensembl.healthcheck.DatabaseNameMatcher "$*"
