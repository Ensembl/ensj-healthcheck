#!/bin/sh

dir=/nfs/acari/ensembl/ensj-healthcheck

cd $dir
home=`dirname $0`
. $home/setup.sh
classpath
cd $dir

$JAVA_HOME/bin/java -server -Xmx2048m org.ensembl.healthcheck.DatabaseTestRunner -config database.release.properties


