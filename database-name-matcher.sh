#!/bin/sh

$JAVA_HOME/bin/java -classpath "lib/ensj-healthcheck.jar:lib/mysql-connector-java-3.0.15-ga-bin.jar" org.ensembl.healthcheck.DatabaseNameMatcher "$*"
