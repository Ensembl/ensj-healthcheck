#!/bin/sh

home=/nfs/acari/gp1/work/ensj-healthcheck
web=/nfs/acari/gp1/WWW
java=/usr/opt/java141/bin/java

cp=$home
cp=$cp:$home/lib/ensj-healthcheck.jar
cp=$cp:$home/lib/mysql-connector-java-3.0.15-ga-bin.jar

cd $home

rm -f timings.txt

# comment/uncomment the ones to run

$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_anopheles_gambiae.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_apis_mellifera.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_bos_taurus.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_caenorhabditis_elegans.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_canis_familiaris.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_ciona_intestinalis.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_danio_rerio.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_drosophila_melanogaster.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_fugu_rubripes.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_gallus_gallus.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_homo_sapiens.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_mus_musculus.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_pan_troglodytes.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_rattus_norvegicus.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_saccharomyces_cerevisiae.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_tetraodon_nigroviridis.properties
$java -server -classpath $cp org.ensembl.healthcheck.WebTestRunner $* -config web_xenopus_tropicalis.properties

cp -f $home/*.html $web
