#!/bin/sh -x

div=$1
db=$2
sid=$3

if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS=-Xmx15g
fi
export JAVA_OPTS

report_dir=$(dirname $0)/hc_reports/$div/$sid
mkdir -p $report_dir

echo "Running HCs on $div:$db in session $sid to $report_dir/$db.out"
div=${div,,}

if [[ $db =~ core ]] || [[ $db =~ otherfeatures ]]; then
./run-configurable-testrunner.sh \
  -T ProteinTranslation MultiDbStableId MultiDbCompareNames \
  -t MySQLStorageEngine  \
  -g EGCore \
  -c staging.properties output.properties  \
  -R Database --output.database healthchecks_${div} \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out
elif [[ $db =~ compara ]]; then

./run-configurable-testrunner.sh \
  -T CheckSynteny MultiDbStableId MultiDbCompareNames \
  -t MySQLStorageEngine  \
  -g EGCompara \
  -c staging.properties output.properties  \
  -R Database --output.database healthchecks_${div} \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out

elif [[ $db =~ variation ]]; then

./run-configurable-testrunner.sh \
  -T MultiDbStableId MultiDbCompareNames \
  -t MySQLStorageEngine  \
  -g EGVariation \
  -c staging.properties output.properties  \
  -R Database --output.database healthchecks_${div} \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out

elif [[ $db =~ funcgen ]]; then

./run-configurable-testrunner.sh \
  -T MultiDbStableId MultiDbCompareNames \
  -t MySQLStorageEngine  \
  -g EGFuncgen \
  -c staging.properties output.properties  \
  -R Database --output.database healthchecks_${div} \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out

else 
    echo "Do not know how to handle database $db - skipping"
fi


