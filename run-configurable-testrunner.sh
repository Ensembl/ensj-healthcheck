#!/bin/sh

home=`dirname $0`
cp=$home:$home/build/

for jar in $home/lib/*.jar; do
    echo Adding $jar
    cp=$cp:$jar
done

echo ---------------------------
echo $cp
echo ---------------------------

java -cp $cp:resources/runtime/ -Xmx1500m -Djava.util.logging.config.file=config/logger/logging.properties org.ensembl.healthcheck.ConfigurableTestRunner $*

