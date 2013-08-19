#!/bin/sh

home=`dirname $0`
. $home/setup.sh
jar
classpath

java -Xmx1000m -Djava.util.logging.config.file=config/logger/logging.properties org.ensembl.healthcheck.TestCaseInfo $*

