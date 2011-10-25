#!/bin/sh

. /software/lsf/conf/profile.lsf

#JAVA_HOME=/usr/opt/java
dir=$HOME/ensj-healthcheck

cp=$dir
for jar in $dir/lib/*.jar; do
    cp=$jar:$cp
done

cd $dir

$JAVA_HOME/bin/java -server -classpath $cp -Xmx1024m org.ensembl.healthcheck.ParallelDatabaseTestRunner

