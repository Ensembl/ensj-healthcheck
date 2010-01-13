#!/bin/sh

. /software/lsf/conf/profile.lsf

#JAVA_HOME=/usr/opt/java
dir=$HOME/ensj-healthcheck

cp=$dir
cp=$cp:$dir/lib/ensj-healthcheck.jar
cp=$cp:$dir/lib/mysql-connector-java-3.0.15-ga-bin.jar

cd $dir

$JAVA_HOME/bin/java -server -classpath $cp org.ensembl.healthcheck.ParallelDatabaseTestRunner

