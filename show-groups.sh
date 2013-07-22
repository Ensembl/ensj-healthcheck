#!/bin/sh

home=`dirname $0`
cp=$home:$home/build/
for jar in $home/lib/*.jar; do
    cp=$jar:$cp
done

$JAVA_HOME/bin/java -Xmx1500m -classpath $cp org.ensembl.healthcheck.ListAllTests -groups
