#!/bin/sh

home=`dirname $0`
cp=$home:$home/build/

for jar in $home/lib/*.jar; do
    cp=$cp:$jar
done

java -cp $cp:resources/runtime/ -Xmx1000m -Djava.util.logging.config.file=config/logger/logging.properties org.ensembl.healthcheck.TestCaseInfo $*

