#!/bin/sh

home=`dirname $0`
. $home/setup.sh
jar
classpath

$JAVA_HOME/bin/java org.ensembl.healthcheck.gui.GuiTestRunner $*
