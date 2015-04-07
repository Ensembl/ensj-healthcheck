#!/bin/bash

home=`dirname $0`
. $home/setup.sh
jar
classpath

$JAVA_HOME/bin/java -Xmx1500m org.ensembl.healthcheck.ListAllTests -groups
