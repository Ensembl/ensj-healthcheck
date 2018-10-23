#!/bin/bash -l
# Script for healthcheck execution with locking

div='EV'

function msg {
    echo $(date +"%Y-%m-%d %H:%M:%S") $1
}

properties=$1

if [ -z "$properties" ]; then
  properties=${div,,}-database.properties
fi

hive_host=$(sed -n 's/^ *hive.host *= *\([^ ]*.*\)/\1/p' < $properties)
hive_port=$(sed -n 's/^ *hive.port *= *\([^ ]*.*\)/\1/p' < $properties)
hive_user=$(sed -n 's/^ *hive.user *= *\([^ ]*.*\)/\1/p' < $properties)
hive_pass=$(sed -n 's/^ *hive.password *= *\([^ ]*.*\)/\1/p' < $properties)

hive="mysql -h $hive_host -P $hive_port -u $hive_user -p$hive_pass"
msg "Will use hive $hive"


LOG_FILE=${div}.log
msg "Running hived checks for Ensembl as process $$ on $(hostname)"
cd $(dirname $0)
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS=-Xmx15g
fi
export JAVA_OPTS
if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME=/nfs/software/ensembl/latest/jenv/versions/1.8
fi
export JAVA_HOME
cwd=$(pwd)
export PATH=$HOME/src/ensembl/ensembl-hive/scripts:$HOME/ensj-healthcheck:$PATH
export PERL5LIB=$cwd/perl:$HOME/src/ensembl/ensembl/modules:$HOME/src/ensembl/ensembl-hive/modules:$PERL5LIB

#Regenerating the hc jar
. $HOME/work/ensj-healthcheck/setup.sh
jar
export NO_JAR=1

# Get parameters for HC database.
HCDB=$(sed -n 's/^ *output.database *= *\([^ ]*.*\)/\1/p' < $properties)
HCDB_HOST=$(sed -n 's/^ *output.host *= *\([^ ]*.*\)/\1/p' < $properties)
HCDB_PORT=$(sed -n 's/^ *output.port *= *\([^ ]*.*\)/\1/p' < $properties)
HCDB_USER=$(sed -n 's/^ *output.user *= *\([^ ]*.*\)/\1/p' < $properties)
HCDB_PASS=$(sed -n 's/^ *output.password *= *\([^ ]*.*\)/\1/p' < $properties)
hc_url=mysql://$HCDB_USER:$HCDB_PASS@$HCDB_HOST:$HCDB_PORT/$HCDB
msg "Will use HC db $hc_url"

# Get parameters for the production database.
PRODDB=$(sed -n 's/^ *production.database *= *\([^ ]*.*\)/\1/p' < $properties)
PRODDB_HOST=$(sed -n 's/^ *production.host *= *\([^ ]*.*\)/\1/p' < $properties)
PRODDB_PORT=$(sed -n 's/^ *production.port *= *\([^ ]*.*\)/\1/p' < $properties)
PRODDB_USER=$(sed -n 's/^ *production.user *= *\([^ ]*.*\)/\1/p' < $properties)
PRODDB_PASS=$(sed -n 's/^ *production.password *= *\([^ ]*.*\)/\1/p' < $properties)
production_url=mysql://$PRODDB_USER:$PRODDB_PASS@$PRODDB_HOST:$PRODDB_PORT/$PRODDB
msg "Will use Production db $production_url"

# Get options for HC run
group=$(sed -n 's/^ *groups *= *\([^ ]*.*\)/\1/p' < $properties)
exclude_dbs=$(sed -n 's/^ *exclude_dbs *= *\([^ ]*.*\)/\1/p' < $properties)
hosts=$(sed -n 's/^ *host[0-9]* *= *\([^ ]*.*\)/\1/p' < $properties)
release=$(sed -n 's/^ *release *= *\([0-9]*\)/\1/p' < $properties)
prev_release=$(($release - 1))
msg "Will run $group HCs on hosts $hosts, ignoring $exclude_dbs, for release $release"

# Get parameters for main database server (this release)
DB_HOST=$(sed -n 's/^ *host *= *\([^ ]*.*\)/\1/p' < $properties)
DB_PORT=$(sed -n 's/^ *port *= *\([^ ]*.*\)/\1/p' < $properties)
DB_USER=$(sed -n 's/^ *user *= *\([^ ]*.*\)/\1/p' < $properties)

# Get parameters for second database server (last release)
PREVDB_HOST=$(sed -n 's/^ *secondary.host *= *\([^ ]*.*\)/\1/p' < $properties)
PREVDB_PORT=$(sed -n 's/^ *secondary.port *= *\([^ ]*.*\)/\1/p' < $properties)
PREVDB_USER=$(sed -n 's/^ *secondary.user *= *\([^ ]*.*\)/\1/p' < $properties)

# Create output files
TIMINGS_FILE=/tmp/timings.txt
PROPAGATE_FILE=propagate.log
touch $LOG_FILE
touch $TIMINGS_FILE
touch $PROPAGATE_FILE
chmod g+rwx $LOG_FILE
chmod g+rwx $TIMINGS_FILE
chmod g+rwx $PROPAGATE_FILE

# Check if there's a lock.
MYSQL_CMD="mysql --skip-secure-auth --host=$HCDB_HOST --port=$HCDB_PORT --user=$HCDB_USER --password=$HCDB_PASS $HCDB"
msg "Looking for lock"
LOCK_EXISTS=$($MYSQL_CMD --column-names=false -e "select count(*) from information_schema.tables where table_name=\"hc_lock\" and table_schema=\"$HCDB\"" 2>/dev/null)

if [ "$LOCK_EXISTS" == "1" ]; then
  LOCK_DETAILS=$($MYSQL_CMD --skip-column-names -e 'SELECT user, hostname from hc_lock;')
  echo "Execution of healthchecks failed - database locked by " $LOCK_DETAILS 1>&2;
  exit
else
    msg "No lock found"    
  # Generate lock table.
  LOCK_SQL="CREATE TABLE hc_lock "
  LOCK_SQL+="(user varchar(25), hostname varchar(100), process_id varchar(25), lsf_job_id varchar(25)) "
  LOCK_SQL+="ENGINE=MyISAM; "

  LOCK_SQL+="INSERT INTO hc_lock VALUES (\"$USER\", \"$HOSTNAME\", \"$$\", \"local\"); "
  echo $LOCK_SQL > /tmp/${HCDB}.sql
  $MYSQL_CMD < /tmp/${HCDB}.sql
  msg "Lock generated"
fi

msg "Running Propagate script for ${div}"
#Running the propagate script
$(perl propagate.pl -host1 $DB_HOST -port1 $DB_PORT -user1 $DB_USER -host2 $DB_HOST -port2 $DB_PORT -user2 $DB_USER -dbname $HCDB -old_release $prev_release -new_release $release -host_hc $HCDB_HOST -port_hc $HCDB_PORT -user_hc $HCDB_USER -pass_hc $HCDB_PASS  -host_prev $PREVDB_HOST -port_prev $PREVDB_PORT -user_prev $PREVDB_USER >& $PROPAGATE_FILE)

msg "Starting healthcheck run for ${div}"
# do hive stuff
pipeline_db="run_${HCDB}"
msg "Creating hive ${USER}_$pipeline_db"
init_pipeline.pl Bio::EnsEMBL::Healthcheck::Pipeline::RunHealthchecks_ens_conf -hc_conn $hc_url -prod_conn $production_url -pipeline_db -user=$hive_user -pipeline_db -pass=$hive_pass -pipeline_db -host=$hive_host -pipeline_db -port=$hive_port -hive_force_init 1 -division $div -hc_cmd "./run_ens_hc_hive.sh #division# #dbname# #session_id# #properties# #group# #hcdb#" -pipeline_name $pipeline_db -properties $properties -group "$group" -exclude_dbs "$exclude_dbs" -host "$hosts" -hcdb "$HCDB" -release "$release"
msg "Running beekeeper"
hive_url=mysql://$hive_user:$hive_pass@$hive_host:$hive_port/${USER}_${pipeline_db}
beekeeper.pl -url $hive_url -loop >& $LOG_FILE.hive
msg "Beekeeper complete"

failN=$($hive --column-names=false ${USER}_${pipeline_db} -e "select count(*) from job where status=\"FAILED\"")

if [ "$failN" != "0" ]; then
    msg "${failN} failed hive jobs found when running healthchecks for division ${div} - please check the hive ${url} for details"
    exit 2
fi

# Populate log table and remove lock.
LOG_SQL="CREATE TABLE IF NOT EXISTS last_log "
LOG_SQL+="(hostname varchar(100), std_err_out varchar(255), timings varchar(255)) "
LOG_SQL+="ENGINE=MyISAM; "

LOG_SQL+="TRUNCATE TABLE last_log; "

LOG_SQL+="INSERT INTO last_log VALUES (\"$HOSTNAME\", \"$LOG_FILE\", \"/tmp/timings.txt\"); "

$MYSQL_CMD -e "$LOG_SQL"

$MYSQL_CMD -e "DROP TABLE hc_lock;"

msg "Completed healthcheck run for ${div}"

exit 0
