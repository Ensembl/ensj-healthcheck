#!/bin/sh

java -classpath "lib/junit.jar:lib/mysql-connector-java-2.0.14-bin.jar:build/" org.ensembl.healthcheck.ListAllTests $*
