#!/bin/sh

# create build directory for class files if it's not there already
if [ ! -d build ]; then
  mkdir build
fi

find src -name '*.java' | xargs $JAVA_HOME/bin/javac -d build -sourcepath src

$JAVA_HOME/bin/jar cf lib/ensj-healthcheck.jar -C build org images/*.gif
