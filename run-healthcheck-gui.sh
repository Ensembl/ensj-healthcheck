#!/bin/sh

/usr/opt/java142/bin/java -classpath "lib/ensj-healthcheck.jar:lib/mysql-connector-java-3.0.15-ga-bin.jar:lib/looks-1.2.1.jar" org.ensembl.healthcheck.gui.GuiTestRunner $*
