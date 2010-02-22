#!/bin/sh

home='.'
cp=""
for jar in $home/lib/*.jar; do
    cp=$jar:$cp
done

$JAVA_HOME/bin/java -classpath "$cp" org.ensembl.healthcheck.gui.GuiTestRunner $*
