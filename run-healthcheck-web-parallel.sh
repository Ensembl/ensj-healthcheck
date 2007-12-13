#!/bin/sh

JAVA_HOME=/usr/opt/java
dir=/nfs/acari/ensembl/ensj-healthcheck

cp=$dir
cp=$cp:$dir/lib/ensj-healthcheck.jar
cp=$cp:$dir/lib/mysql-connector-java-3.0.15-ga-bin.jar

cd $dir

$JAVA_HOME/bin/java -server -classpath $cp -Xmx1700m org.ensembl.healthcheck.ParallelDatabaseTestRunner


