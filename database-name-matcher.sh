#!/bin/sh

java -classpath "lib/ensj.jar:lib/mysql-connector-java-2.0.14-bin.jar:build/" org.ensembl.healthcheck.DatabaseNameMatcher "$*"
