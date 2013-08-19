#!/bin/sh

home=`dirname $0`
. $home/setup.sh
jar
classpath

echo ---------------------------
echo $CLASSPATH
echo ---------------------------

java -Xmx1500m -Djava.util.logging.config.file=config/logger/logging.properties org.ensembl.healthcheck.ConfigurableTestRunner $*

