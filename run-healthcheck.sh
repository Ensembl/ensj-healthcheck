#!/bin/bash

java -classpath "lib/junit.jar:lib/mysql-connector-java-2.0.14-bin.jar:src/" org.ensembl.healthcheck.TestRunner $*
