#!/usr/bin/env perl
# Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
# Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

# $Source$
# $Revision$
# $Date$
# $Author$
#
# script to execute the specified healthcheck module
#
use warnings;
use strict;
use Getopt::Long;
use Log::Log4perl qw(:easy);
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Utils::ScriptUtils qw(inject);
use Carp;

my ( $host, $user, $pass, $port, $dbname, $species_id, $module );

# Allow password to be passed by setting the environment variable 'pass'.
#
$pass = $ENV{'pass'};

GetOptions(
	"host=s",       \$host,
	"user=s",       \$user,
	"pass:s",       \$pass,
	"port=i",       \$port,
	"dbname=s",     \$dbname,
	"species_id=s", \$species_id,
	"module=s",     \$module
);

if (   !defined $host
	|| !defined $port
	|| !defined $user
	|| !defined $module
	|| !defined $dbname ) {
	croak (
		"Usage: $0 -host host -port port -user user [-pass password] -dbname db [-species_id species_id] -module module"
	);
}

my %args = (
	-HOST   => $host,
	-USER   => $user,
	-PORT   => $port,
	-PASS   => $pass,
	-DBNAME => $dbname
);
if ( defined $species_id ) {
	$args{-SPECIES_ID}      = $species_id;
	$args{-MULTISPECIES_DB} = 1;
}
my $dba         = new Bio::EnsEMBL::DBSQL::DBAdaptor(%args);
inject($module);
my $healthcheck = $module->new( dba => $dba );

my $healthcheck_passed = $healthcheck->run();

if ($healthcheck_passed) {
	exit 0;
} else {
	exit 1;
}
