#!/bin/sh

java -classpath "lib/ensj.jar:lib/junit.jar:lib/mysql-connector-java-2.0.14-bin.jar:build/" org.ensembl.healthcheck.BuildTestLibrary doc/external_web_pages/testlist_template.html 
