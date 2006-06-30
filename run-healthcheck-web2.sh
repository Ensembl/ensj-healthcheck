#!/bin/sh

home=/nfs/acari/gp1/work/ensj-healthcheck
web=/nfs/acari/gp1/WWW
java=/usr/opt/java141/bin/java

cp=$home
cp=$cp:$home/lib/ensj-healthcheck.jar
cp=$cp:$home/lib/mysql-connector-java-3.0.15-ga-bin.jar

cd $home

$java -server -classpath $cp -Xmx512m org.ensembl.healthcheck.DatabaseTestRunner

$java -server -classpath $cp org.ensembl.healthcheck.DatabaseToHTML -output /nfs/WWWUsers/Users/gp1/WWW/new


