#!/bin/sh

# create build directory for class files if it's not there already
if [ ! -d build ]; then
  mkdir build
fi

find src -name '*.java' | xargs /usr/opt/java142/bin/javac -classpath "lib/junit.jar:lib/looks-1.2.1.jar" -d build -sourcepath src

cp images/*.??? build/org/ensembl/healthcheck/gui/

/usr/opt/java142/bin/jar cf lib/ensj-healthcheck.jar -C build org 
