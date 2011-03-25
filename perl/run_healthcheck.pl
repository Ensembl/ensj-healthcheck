#!/usr/bin/env perl
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
