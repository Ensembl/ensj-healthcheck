#!/bin/sh

# create build directory for class files if it's not there already
if [ ! -d build ]; then
  mkdir build
fi

find src -name '*.java' | xargs $JAVA_HOME/bin/javac -classpath "lib/junit.jar:lib/looks-1.2.1.jar" -d build -sourcepath src

cp images/*.??? build/org/ensembl/healthcheck/gui/

$JAVA_HOME/bin/jar cf lib/ensj-healthcheck.jar -C build org 
