#!/bin/sh

java -classpath "lib/ensj.jar:lib:/ecp1_0beta.jar:lib/log4j-1.2.6.jar:lib/mysql-connector-java-3.0.8-stable-bin.jar:build/" org.ensembl.healthcheck.GuiTestRunner $*
