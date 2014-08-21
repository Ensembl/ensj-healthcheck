#!/usr/bin/env perl

# Propagate manual_ok_all_releases from one release to another
# modified to support EG procedures

use strict;
use warnings;
use DBI;
use Getopt::Long;
use IO::File;
use Carp;
use File::Basename;
use Data::Dumper;

$Data::Dumper::Sortkeys = 1;

my $core_like_dbs =
  [qw/core cdna otherfeatures vega rnaseq vega_update/];

my ( $host_hc,   $port_hc,     $user_hc,     $pass_hc,
	 $host1,     $port1,       $user1,       $pass1,
	 $host_prev, $port_prev,   $user_prev,   $pass_prev,
	 $dbname,    $old_release, $new_release, $quiet,
	 $new_dbname );

GetOptions( 'user=s'        => \$user1,
			'pass=s'        => \$pass1,
			'host=s'        => \$host1,
			'port=i'        => \$port1,
			'user_hc=s'     => \$user_hc,
			'pass_hc=s'     => \$pass_hc,
			'host_hc=s'     => \$host_hc,
			'port_hc=i'     => \$port_hc,
			'user_prev=s'   => \$user_prev,
			'pass_prev=s'   => \$pass_prev,
			'host_prev=s'   => \$host_prev,
			'port_prev=i'   => \$port_prev,
			'dbname=s'      => \$dbname,
			'old_release=i' => \$old_release,
			'new_release=i' => \$new_release,
			'quiet'         => \$quiet,
			'new_dbname=s'  => \$new_dbname,
			'help'          => sub { usage(); exit(0); } );

if ( !( $old_release && $new_release ) ) {
  print "Must specify -old_release and -new_release\n";
  exit(1);
}

my $dbi1 = DBI->connect( "DBI:mysql:host=$host1:port=$port1",
						 $user1, $pass1, { 'RaiseError' => 1 } ) ||
  die "Can't connect to database\n";

my $dbi_hc = DBI->connect(
			   "DBI:mysql:host=$host_hc:port=$port_hc;database=$dbname",
			   $user_hc, $pass_hc, { 'RaiseError' => 1 } ) ||
  die "Can't connect to HC database $dbname on $host_hc:port_hc\n";

# connect to server where previous release databases should be
my $dbi_prev = DBI->connect("DBI:mysql:host=$host_prev:port=$port_prev",
							$user_prev,
							$pass_prev,
							{ 'RaiseError' => 1 } ) ||
  die "Can't connect to previous release database server\n";

# cache database name mappings
my $old_to_new_db_name;
my $session_id;

# metacache used for calculating genebuild changes
my $meta_cache = create_meta_cache( $dbi1, $dbi_prev );

# if we are propagating all the databases within the release, get all databases from staging
if ( !$new_dbname ) {

  $old_to_new_db_name =
	create_db_name_cache( $dbi_hc, $dbi1, $old_release, $new_release );

  # create a new session for new release or reuse an existing one
  $session_id = session_id( $dbi_hc, $old_release, $new_release );

}
else {
  # we are propagating a single database
  $old_to_new_db_name =
	create_db_name_cache( $dbi_hc, $dbi1, $old_release, $new_release,
						  $new_dbname );

  # we will use the latest session_id (if it has already been created)
  $session_id = get_latest_session_id( $dbi_hc, $new_release );
  if ( $session_id < 0 ) {

	# the session_id is not correct, probably not run propagate before;
	print STDERR
"There is no session_id for release $new_release in the database\n. Have you propagated all the databases before?\n";
	exit(1);
  }
}

sub create_meta_cache {

  print "Caching genebuild.start_date/version meta entries\n"
	unless ($quiet);

  my %cache;

  foreach my $dbi (@_) {

	my $list_sth = $dbi->prepare("SHOW DATABASES LIKE ?");

# get all core databases on this server whose names are in the (could be optimised by just looking at the ones we need)
	$list_sth->execute("%\\_core\\_%");

	my $dbname;
	$list_sth->bind_columns( \$dbname );

	while ( $list_sth->fetch() ) {

	  if($dbname =~ m/^[a-z0-9_]+_core_[0-9]+_[0-9]+$/) {

	  my $meta_sth = $dbi->prepare(
"SELECT meta_value FROM $dbname.meta WHERE meta_key='genebuild.version' and species_id=1"
	  );    # can't do this with a prepared statemen

	  $meta_sth->execute();

	  my $start_date = ( $meta_sth->fetchrow_array() )[0]
		;    # || die "Error getting genebuild.version from $dbname";
	  if ( !defined $start_date ) {
		my $meta_sth = $dbi->prepare(
"SELECT meta_value FROM $dbname.meta WHERE meta_key='genebuild.start_date'"
		);    # can't do this with a prepared statemen

		$meta_sth->execute();

		$start_date = ( $meta_sth->fetchrow_array() )[0]
		  ;  # || die "Error getting genebuild.start_date from $dbname";

	  }
	  if ( defined $start_date ) {
		$cache{$dbname} = $start_date;
	  }
	  }
	} ## end while ( $list_sth->fetch(...))

  } ## end foreach my $dbi (@_)

  return \%cache;

} ## end sub create_meta_cache

#Meta cache for regulation
my $regulatory_build_cache =
  create_regulation_cache( $dbi1, $dbi_prev );

# propagate! propagate!
propagate( $dbi_hc, $dbi_prev, $old_release, $new_release );

#set_end_time( $dbi1 );

# --------------------------------------------------------------------------------

sub create_db_name_cache {
  my ( $healthcheck_dbi, $server_dbi, $old_release,
	   $new_release,     $new_dbname ) = @_;

  # get list of databases for old session
  my $sql =
"SELECT distinct(database_name) from report WHERE database_name LIKE ?";
  my $hc_like;
  if ($new_dbname) {
	# only propagate for new_dbname
	$new_dbname =~ /([a-z]+_[a-z]+_[a-z]+)_\d+/;
	$hc_like = "%$1\\_$old_release%";
  }
  else {
	# propagate for all databases in old_release
	$hc_like = "%\\_${old_release}_%";
  }
  my @old_dbs =
	@{ $healthcheck_dbi->selectcol_arrayref( $sql, {}, $hc_like ) };
  my @new_dbs =
	@{ get_new_databases( $server_dbi, $new_release, $new_dbname ) };

  # create mapping
  my %cache;
  foreach my $old_db (@old_dbs) {
	my $new_db = find_match( $old_db, @new_dbs );
	if ($new_db) {
	  #      print "$old_db -> $new_db\n" unless ($quiet);
	  $cache{$old_db} = $new_db;
	}
  }

  return \%cache;
} ## end sub create_db_name_cache

# --------------------------------------------------------------------------------

sub get_new_databases {
  my ( $server_dbi, $new_release, $new_dbname ) = @_;
  my $new_like = ($new_dbname) ? "%$new_dbname%" :    # get a single DB
	"%\_${new_release}\_%"
	;    # get all DBs; note this will exclude master_schema_48 etc
  my $new_dbs = [
			 grep { $_ !~ /_temp_/ }
			   @{
			   $server_dbi->selectcol_arrayref( "SHOW DATABASES LIKE ?",
												{}, $new_like ) } ];
  return $new_dbs;
}

# --------------------------------------------------------------------------------
# Returns databases which could be parsed by the extract_prefix code
#

sub get_valid_databases {
  my ( $server_dbi, $release ) = @_;
  my @databases = grep { ( extract_prefix($_) ) ? 1 : 0 }
	@{ get_new_databases( $server_dbi, $release ) };
  return \@databases;
}

# --------------------------------------------------------------------------------

sub find_match {
  my ( $old_db, @new_dbs ) = @_;
  foreach my $new_db (@new_dbs) {
	return $new_db if compare_dbs( $old_db, $new_db );
  }
  return;
}

# --------------------------------------------------------------------------------

sub compare_dbs {
  my ( $old, $new ) = @_;
  return extract_prefix($old) eq extract_prefix($new);
}

# --------------------------------------------------------------------------------

sub extract_prefix {
  my ($dbname) = @_;
  my $regex = qr/(
    \A [a-z0-9]+ _ [a-z0-9]+  #binomial component 
    (?: _ [a-z0-9]+)?         #trinomial component or group
    _ [a-z]+                  #group
    ) _ \d                    # _ release number of some description
  /xms;
  my ($prefix) = $dbname =~ $regex;
  #  print $dbname."->".$prefix."\n";
  return $prefix;
}

# --------------------------------------------------------------------------------

sub propagate {
  my ( $dbi, $dbi_prev, $old_release, $new_release ) = @_;

  my %propagated_new_databases;

  my @types =
	qw(manual_ok_all_releases manual_ok_this_assembly manual_ok_this_genebuild manual_ok_this_regulatory_build);

  # get complete list of unannotated failures from current session

  my $problem_sql = q/select testcase,text,report_id from report 
left join annotation using (report_id) 
where database_name=? and result='PROBLEM' 
and last_session_id=? and annotation.report_id is null/;
  my $problem_sth = $dbi->prepare($problem_sql);

  my $ann_sql =
q/select testcase,text,person,action,comment,created_at,modified_at,created_by,modified_by
from report join annotation using (report_id) where action=? and database_name=? and result='PROBLEM'/;
  my $ann_sth = $dbi->prepare($ann_sql);

  my $reann_sql =
q/insert into annotation(report_id,person,action,comment,created_at,modified_at,created_by,modified_by)
values (?,?,?,?,?,?,?,?)/;
  my $reann_sth = $dbi->prepare($reann_sql);

  my %counts = ();

  # find all manual_ok_all_releases etc for each old database
  foreach my $old_database ( sort keys %$old_to_new_db_name ) {

	my $new_database = $old_to_new_db_name->{$old_database};

	my $problems = {};
	$problem_sth->execute( $new_database, $session_id );
	foreach my $row ( @{ $problem_sth->fetchall_arrayref() } ) {
	  $problems->{ $row->[0] . $row->[1] } = $row->[2];
	}

	foreach my $type (@types) {

	  print "Propagating $type for $old_database to $new_database\n"
		unless ($quiet);
	  $counts{$type} = 0;

	  # type eq manual_ok_this_assembly:
	  # will only need to propagate if it is the same assembly
	  if ( $type eq 'manual_ok_this_assembly' ) {
		if ( new_assembly( $old_database, $new_database, $new_release,
						   $dbi1,         $dbi_prev ) )
		{
		  print
"Skipping $new_database as it is a new assembly so we ignore '$type'\n"
			if !$quiet;
		  next;
		}
	  }

	  # type eq manual_ok_this_genebuild
	  # will only need to propagate if it is the same genebuild
	  # i.e. if genebuild.start_date values in meta table are the same
	  # so skip propagation if they are not the same
	  if ( $type eq 'manual_ok_this_genebuild' ) {
		if ( new_genebuild( $old_database, $new_database, $new_release,
							$dbi1,         $dbi_prev ) )
		{
		  print
"Skipping $new_database as it is a new genebuild so we ignore '$type'\n"
			if !$quiet;
		  next;
		}
	  }

	  # type eq manual_ok_this_regulatory_build:
	  # will only need to propagate if it is the same regulatory build
	  if ( $type eq 'manual_ok_this_regulatory_build' ) {
		if ( new_regulatory_build( $old_database, $new_database ) ) {
		  print
"Skipping $new_database as it is a new regulatory build so we ignore '$type'\n"
			if !$quiet;
		  next;
		}
	  }

	  # iterate over previous annotations of this type
	  # look for matches
	  my $count = 0;

	  $ann_sth->execute( $type, $old_database );
	  foreach my $row ( @{ $ann_sth->fetchall_arrayref() } ) {
		my $report_id = $problems->{ $row->[0] . $row->[1] };
		if ( defined $report_id ) {
		  $reann_sth->execute(
			$report_id, $row->[2], $row->[3], $row->[4], $row->[5],
			$row->[6], $row->[7], $row->[8]
		  );
		  $count++;
		}
	  }

	  print "Propagated " .
		$count . " $type reports for $new_database\n"
		unless ($quiet);

	} ## end foreach my $type (@types)

  }    # foreach type

  return;
} ## end sub propagate

# --------------------------------------------------------------------------------

# this method will return true if it is a new species assembly, false otherwise
# checks the contents of meta

sub new_assembly {
  my $old_database      = shift;
  my $new_database      = shift;
  my ($old_db_assembly) = $old_database =~ /_(\d+)[a-z]{0,1}$/;
  my ($new_db_assembly) = $new_database =~ /_(\d+)[a-z]{0,1}$/;
  return ( $old_db_assembly != $new_db_assembly );
}

# --------------------------------------------------------------------------------

# Will check if this is a new genebuild

sub new_genebuild {
  my ( $old_dbname, $new_dbname ) = @_;
  return _compare_caches( $old_dbname, $new_dbname, $meta_cache );
}
# --------------------------------------------------------------------------------

# Will check if this is a new regulatory build. New means either DB's build
# was null (as we can't check it) or the build values were not the same

sub new_regulatory_build {
  my ( $old_dbname, $new_dbname ) = @_;
  return _compare_caches( $old_dbname, $new_dbname,
						  $regulatory_build_cache );
}

# --------------------------------------------------------------------------------

sub _compare_caches {
  my ( $old_dbname, $new_dbname, $cache ) = @_;
  #logic copied from old script. Get value or default to nothing
  my $old = $cache->{$old_dbname} || q{};
  my $new = $cache->{$new_dbname} || q{};
  return ( "$old" eq "$new" ) ? 0 : 1;    #force string comparison
}

sub _check_changes {
  my ( $old_dbname, $new_dbname, $table, $dbi1, $dbi2, $dbi_prev ) = @_;
  my $result    = 0;
  my $old_check = get_checksum( $old_dbname, $table, $dbi_prev );
  my $new_check = get_checksum( $new_dbname, $table, $dbi1, $dbi2 );
  if ( !$old_check ) {
	return 1;
  }
  if ( $old_check != $new_check ) {
	$result = 1;
  }
  return $result;
}

sub get_checksum {
  my ( $dbname, $table, @dbhs ) = @_;
  my $result = 0;
  foreach my $dbh (@dbhs) {
	my $sth_status = $dbh->prepare("CHECKSUM table $dbname.$table");
	$sth_status->execute();
	my $sth_res = $sth_status->fetchrow_array();
	if ($sth_res) {
	  $result = $sth_res;
	}
  }
  return $result;
}

sub _check_declaration {
  my ( $new_dbname, $new_release, $declaration ) = @_;
  my $prod_dbi = get_production_DBAdaptor();
  my $sth = $prod_dbi->prepare(
	"SELECT count(*)
     FROM   db_list dl, db d
     WHERE  dl.db_id = d.db_id and db_type = 'core' and is_current = 1 
     AND full_db_name = ?
     AND    species_id IN (
     SELECT species_id 
     FROM   changelog c, changelog_species cs 
     WHERE  c.changelog_id = cs.changelog_id 
     AND    release_id = ?
     AND    status not in ('cancelled', 'postponed') 
     AND    $declaration = 'Y')" );

  $sth->execute( $new_dbname, $new_release );
  my $result = $sth->fetchrow_array();
  return $result;
}

sub get_production_DBAdaptor {
  my $prod_dbi = DBI->connect(
		"DBI:mysql:host=$host1:port=$port1;database=ensembl_production",
		$user1, $pass1, { 'RaiseError' => 1 } );
  return $prod_dbi;
}

# --------------------------------------------------------------------------------

# Combines the other two session methods to add to an existing session
# or create a new one if there is not one already available.

sub session_id {
  my ( $dbi, $old_release, $new_release ) = @_;
  my $last_session_id = get_latest_session_id( $dbi, $new_release );
  return $last_session_id if $last_session_id >= 0;
  return create_session( $dbi, $old_release, $new_release );
}

# --------------------------------------------------------------------------------

sub create_session {
  my ( $dbi, $old_release, $new_release ) = @_;
  my $sth = $dbi->prepare(
"INSERT INTO session (db_release,config,start_time) VALUES (?,?,NOW())"
  );
  $sth->execute(
	$new_release,
"Propagation of entries from release $old_release to release $new_release"
	) ||
	die "Error creating new session\n";
  my $new_session_id = $sth->{'mysql_insertid'};
  print "Created session ID $new_session_id for propagation\n"
	unless $quiet;
  return $new_session_id;
}

# --------------------------------------------------------------------------------

# method that will return the latest session_id in the database for the new_release

sub get_latest_session_id {
  my ( $dbi, $new_release ) = @_;
  #get latest session_id for the new release
  my $sql = 'SELECT max(session_id) FROM session WHERE db_release = ?';
  my $res = $dbi->selectcol_arrayref( $sql, {}, $new_release );
  my ($session) = @{$res};
  $session ||= -1;
  printf( "Found session %d in the DB\n", $session );
  return $session;
}

# --------------------------------------------------------------------------------
# Works with the propagation table which records what species have been seen
# by this script meaning we can support multiple runs

sub record_propagation {
  my ( $dbname, $action, $amount ) = @_;
  my %allowed = map { $_ => 1 } (
		  'manual_ok_all_releases',   'manual_ok_this_assembly',
		  'manual_ok_this_genebuild', 'manual_ok_this_regulatory_build',
		  'none' );
  die "Do not understand the action $action " unless $allowed{$action};
  $amount ||= 0;
  my $sth = $dbi_hc->prepare(
'insert into propagated (database_name, action, session_id, amount) values (?,?,?,?)'
  );
  $sth->execute( $dbname, $action, $session_id, $amount )
	;    #session_id is a global
  $sth->finish();
  return;
}

# --------------------------------------------------------------------------------
# Queries the propagation table for the given database name and returns the
# count of entries which map to this

sub has_been_propagated {
  my ( $dbname, $action ) = @_;
  my $sql    = 'select count(*) from propagated where database_name =?';
  my @params = ($dbname);
  if ($action) {
	$sql .= ' and action =?';
	push( @params, $action );
  }
  my $sth = $dbi_hc->prepare($sql);
  $sth->execute(@params);
  my ($count) = $sth->fetchrow_array();
  $sth->finish();
  return $count;
}

sub get_propagated_problem_count {
  my ($dbname) = @_;
  my $sql =
'select * from propagated where database_name =? and action="problem_count"';
  my @params = ($dbname);
  my $sth    = $dbi_hc->prepare($sql);
  $sth->execute(@params);
  my @cnts = $sth->fetchrow_array();
  $sth->finish();
  if ( scalar(@cnts) == 0 ) {
	my $sth = $dbi_hc->prepare(
'insert into propagated(database_name,action,amount) values(?,?,?)' );
	$sth->execute( $dbname, 'problem_count', 0 );
	$sth->finish();
	return 0;
  }
  else {
	return $cnts[0];
  }
}

sub get_current_problem_count {
  my ($dbname) = @_;
  my $sql =
'select count(*) from report where last_session_id=? and database_name=? and result=?';
  my @params = ( $session_id, $dbname, 'PROBLEM' );
  my $sth = $dbi_hc->prepare($sql);
  $sth->execute(@params);
  my ($count) = $sth->fetchrow_array();
  $sth->finish();
  return $count;
}

sub record_problem_count {
  my ( $dbname, $amount ) = @_;
  my $sth = $dbi_hc->prepare(
'update propagated set amount=? where database_name =? and action = ?'
  );
  $sth->execute( $amount, $dbname, 'problem_count' )
	;    #session_id is a global
  $sth->finish();
  return;
}

# --------------------------------------------------------------------------------
# Attempt to find all databases on the live release but did not have any data
# propagated. We still want to flag that we saw it but did nothing with it. We
# also need to check that we have not already propagated this data into the
# schema
sub flag_non_propagated_dbs {
  my ($propagated_new_databases) = @_;
  my %live_new_databases =
	map { $_ => 1 } @{ get_new_databases( $dbi1, $new_release ) };
  foreach my $new_database ( keys %live_new_databases ) {
	if ( !$propagated_new_databases->{$new_database} &&
		 !has_been_propagated($new_database) )
	{
	  printf(
"Recording '%s' has been seen but we had no data to propagate\n",
		$new_database )
		if !$quiet;
	  record_propagation( $new_database, 'none', 0 );
	}
  }
  return;
}

# --------------------------------------------------------------------------------
# Cache all the meta entries in all databases applicable
# Note all databases = all on $dbi_prev, $dbi1, $dbi2, e.g. all on ens-livemirror, ens-staging1, ens-staging2

sub create_regulation_cache {
  my ( $dbi1, $dbi2, $dbi_prev ) = @_;
  my $die_if_none = 0;
  return
	build_meta_cache( ['funcgen'], 'regbuild.last_annotation_update',
					  $die_if_none, $dbi1, $dbi2, $dbi_prev );
}

# --------------------------------------------------------------------------------
# Cache all the meta entries in all databases applicable using the specified keys

sub build_meta_cache {
  my ( $dbtypes, $meta_key, $die_if_none, @dbhs ) = @_;
  confess 'barp' unless defined $dbtypes;
  my $types_str = join( q{,}, @{$dbtypes} );
  print "Building cache '$meta_key' for '$types_str'\n" unless $quiet;
  my %cache;
  foreach my $dbtype ( @{$dbtypes} ) {
	foreach my $dbh (@dbhs) {
	  if ( defined $dbh ) {
		my $like = "%\\_${dbtype}\\_%";
		my $dbs =
		  $dbh->selectcol_arrayref( 'SHOW DATABASES LIKE ?', {},
									$like );

		foreach my $db ( @{$dbs} ) {
		  my $sql =
			sprintf( 'SELECT meta_value FROM %s.meta where meta_key =?',
					 $db );
		  my @row = $dbh->selectrow_array( $sql, {}, $meta_key );
		  my ($value) = @row;
		  if ( $die_if_none && !$value ) {
			croak
			  "Error getting $meta_key from $db. Program will abort";
		  }
		  $cache{$db} = $value if $value;
		}
	  }
	}
  }
  return \%cache;
} ## end sub build_meta_cache

# --------------------------------------------------------------------------------

sub set_end_time {
  my ($dbi) = @_;

  my $sth = $dbi->prepare(
				"UPDATE session SET end_time=NOW() WHERE session_id=?");
  $sth->execute($session_id) || die "Error setting end time\n";

  print "Set end time for session\n" unless $quiet;

}

