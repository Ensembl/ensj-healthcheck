#!/bin/sh

java -classpath "lib/ensj-healthcheck.jar:lib/ensj.jar:lib/junit.jar:lib/mysql-connector-java-3.0.8-stable-bin.jar" org.ensembl.healthcheck.ListAllTests $*
