#!/bin/sh

home=`dirname $0`
. $home/setup.sh
classpath

$JAVA_HOME/bin/java -Xmx1500m -server org.ensembl.healthcheck.DatabaseTestRunner $* 
