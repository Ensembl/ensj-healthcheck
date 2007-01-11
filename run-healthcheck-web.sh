#!/bin/sh

dir=$HOME/ensj-healthcheck
java=/usr/bin/java

cp=$dir
cp=$cp:$dir/lib/ensj-healthcheck.jar
cp=$cp:$dir/lib/mysql-connector-java-3.0.15-ga-bin.jar

cd $dir

$java -server -classpath $cp -Xmx512m org.ensembl.healthcheck.DatabaseTestRunner

#$java -server -classpath $cp org.ensembl.healthcheck.DatabaseToHTML -output $dir/web_healthchecks

