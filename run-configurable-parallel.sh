#!/bin/sh
# Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


. /software/lsf-farm3/conf/profile.lsf

#cd into the dir but this file is already there. Use standard import pattern
dir=$HOME/src/ensj-healthcheck
cd $dir

home=`dirname $0`
. $home/setup.sh
jar
classpath

$JAVA_HOME/bin/java -server -Xmx256m org.ensembl.healthcheck.ParallelConfigurableTestRunner

