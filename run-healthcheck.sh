#!/bin/sh

java -classpath "lib/ensj.jar:lib:/ecp1_0beta.jar:lib/log4j-1.2.6.jar:lib/mysql-connector-java-2.0.14-bin.jar:build/" org.ensembl.healthcheck.TextTestRunner $*
