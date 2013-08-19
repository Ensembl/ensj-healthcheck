#!/bin/sh

dir=$HOME/ensj-healthcheck

cd $dir

home=`dirname $0`
. $home/setup.sh
jar
classpath

$JAVA_HOME/bin/java -server -Xmx1700m org.ensembl.healthcheck.NodeDatabaseTestRunner $*


