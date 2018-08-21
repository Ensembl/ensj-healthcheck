#!/bin/sh -x

div=$1
db=$2
sid=$3
properties=$4
group=$5
hcdb=$6

if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS=-Xmx15g
fi
export JAVA_OPTS

report_dir=$(dirname $0)/hc_reports/$div/$sid
mkdir -p $report_dir

echo "Running HCs on $div:$db in session $sid to $report_dir/$db.out"
div=${div,,}

if [[ $db =~ core ]] || [[ $db =~ otherfeatures ]] || [[ $db =~ rnaseq ]] || [[ $db =~ cdna ]] ; then
./run-configurable-testrunner.sh \
  -g $group\
  -c $properties \
  -R Database --output.database $hcdb \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out
elif [[ $db =~ compara ]]; then

./run-configurable-testrunner.sh \
  -g EGCompara \
  -c $properties \
  -R Database --output.database $hcdb \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out

elif [[ $db =~ variation ]]; then

./run-configurable-testrunner.sh \
  -g VariationRelease \
  -c $properties \
  -R Database --output.database $hcdb \
  -d $db \
  --sessionID $sid >& $report_dir/$db.out

elif [[ $db =~ funcgen ]]; then

./run-configurable-testrunner.sh \
  -g FuncgenIntegrity \
  -c $properties \
  -R Database --output.database $hcdb \
  -d $db \
  --datafile_base_path /nfs/production/panda/ensembl/production/ensemblftp/data_files/ \
  --sessionID $sid >& $report_dir/$db.out

else
    echo "Do not know how to handle database $db - skipping"
fi

