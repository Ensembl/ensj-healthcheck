#!/bin/sh

. /software/lsf/conf/profile.lsf

#cd into the dir but this file is already there. Use standard import pattern
dir=$HOME/ensj-healthcheck
cd $dir

home=`dirname $0`
. $home/setup.sh
jar
classpath

$JAVA_HOME/bin/java -server -Xmx1024m org.ensembl.healthcheck.ParallelDatabaseTestRunner -config database.release.properties

