#!/bin/sh

JAVA_HOME=/usr/opt/j2sdk1.4.2_07
dir=/nfs/acari/ensembl/ensj-healthcheck

cp=$dir
for jar in $dir/lib/*.jar; do
    cp=$jar:$cp
done

cd $dir

$JAVA_HOME/bin/java -server -classpath $cp -Xmx2048m org.ensembl.healthcheck.DatabaseTestRunner


