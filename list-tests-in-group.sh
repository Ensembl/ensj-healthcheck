#!/bin/sh

home=`dirname $0`
cp=$home:$home/build/classes
for jar in $home/lib/*.jar; do
    cp=$jar:$cp
done

$JAVA_HOME/bin/java -classpath $cp org.ensembl.healthcheck.ListAllTests $*
