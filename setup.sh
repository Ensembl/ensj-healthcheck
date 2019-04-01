# Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
# Copyright [2016-2019] EMBL-European Bioinformatics Institute
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#### To Import
# home=`dirname $0`
# . $home/setup.sh
#
# To use:
#
# jar
# classpath
# echoerr "I am a warning to STDERR\n"
#
# Set DEBUG to see debug output and PRINT_ANT to see the ant process output
#
####

echoerr() { 
  if [ -n "$DEBUG" ]; then
    printf "$@" 1>&2; 
  fi
}

run_ant() {
  cmd=$1
  if [ -n "$PRINT_ANT" ]; then
    echoerr "\n" #put in a line break now otherwise it looks odd
    ANT_OPTS='-Xmx300M' ant $cmd
    ant_output=""
  else
    ant_output=$(ANT_OPTS='-Xmx300M' ant $cmd 2>&1)  
  fi

  if [[ $? != 0 ]]; then
    echoerr "FAILED. Please check the following output for possible reasons:\n"
    echoerr "\n"
    echoerr $ant_output
  fi
  echoerr "Finished\n"
  echoerr "########################################################\n"
}

clean() {
  echoerr "########################################################\n"
  echoerr "Cleaning compiled code ..."
  run_ant 'clean'
}

jar() {
  echoerr "########################################################\n"
  echoerr "Compiling Healthchecks ..."
  run_ant 'jar'
}

classpath() {
  if [ -n "$JAVA_HOME" ]; then
    if [ ! -f "$JAVA_HOME/bin/java" ]; then
      echoerr "FAILED. Cannot find a Java binary at $JAVA_HOME/bin/java\n"
      exit 2
    fi
  else
    echoerr "FAILED. Need to set JAVA_HOME to a JDK installation directory\n"
    exit 2
  fi

  home=`dirname $0`
  cp=$home:$home/build/
  for jar in $home/lib/*.jar; do
    if [[ "$jar" == *'ensj-healthcheck.jar' ]]; then
      echo "ERROR: Detected a ensj-healthcheck.jar at $jar. Please remove"
      continue
    fi
    cp=$jar:$cp
  done
  cp=$home/target/dist/ensj-healthcheck.jar:$cp
  cp=$cp:src/:$home/resources/runtime
  export CLASSPATH=$cp
}
