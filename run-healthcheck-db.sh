#!/bin/sh

# Usage note: If you use -config FILE you must fully specify the path to FILE otherwise the program won't find the file.
# e.g. ~/dev/ensj-healthcheck/run-healthcheck.sh  -config `pwd`/db.properties -d my_database SOME_TEST

home=`dirname $0`
cp=$home:$home/build/classes
for jar in $home/lib/*.jar; do
    cp=$jar:$cp
done

$JAVA_HOME/bin/java -Duser.dir=$home -cp $cp -Xmx1500m org.ensembl.healthcheck.DatabaseTestRunner $*
