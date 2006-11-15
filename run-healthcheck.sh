#!/bin/sh

# Usage note: If you use -config FILE you must fully specify the path to FILE otherwise the program won't find the file.
# e.g. ~/dev/ensj-healthcheck/run-healthcheck.sh  -config `pwd`/db.properties -d my_database SOME_TEST

home=`dirname $0`
java=/usr/opt/java/bin/java

cp=$home
cp=$cp:$home/build/classes
cp=$cp:$home/lib/ensj-healthcheck.jar
cp=$cp:$home/lib/mysql-connector-java-3.0.15-ga-bin.jar

$java -Duser.dir=$home -cp $cp -Xmx1500m org.ensembl.healthcheck.TextTestRunner $*

