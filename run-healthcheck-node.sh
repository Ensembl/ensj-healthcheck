#!/bin/sh

#JAVA_HOME=/usr/opt/java
dir=$HOME/ensj-healthcheck

cp=$dir
for jar in $dir/lib/*.jar; do
    cp=$jar:$cp
done

cd $dir

$JAVA_HOME/bin/java -server -classpath $cp -Xmx1700m org.ensembl.healthcheck.NodeDatabaseTestRunner $*


