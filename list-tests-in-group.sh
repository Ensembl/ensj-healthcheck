#!/bin/sh

java -classpath "lib/ensj.jar:lib/junit.jar:lib/mysql-connector-java-3.0.8-stable-bin.jar:build/" org.ensembl.healthcheck.ListAllTests $*
