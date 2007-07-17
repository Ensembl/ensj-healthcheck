#!/bin/sh

$JAVA_HOME/bin/java -classpath "lib/ensj-healthcheck.jar:lib/mysql-connector-java-3.0.8-stable-bin.jar" org.ensembl.healthcheck.ListAllTests -groups
