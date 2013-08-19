#!/bin/sh

home=`dirname $0`
. $home/setup.sh
classpath

$JAVA_HOME/bin/java org.ensembl.healthcheck.gui.GuiTestRunner $*
