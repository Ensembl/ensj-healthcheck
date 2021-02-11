=head1 LICENSE

Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
Copyright [2016-2021] EMBL-European Bioinformatics Institute

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

=cut

# $Source$
# $Revision$
# $Date$
# $Author$
#
=head1 Bio::EnsEMBL::Healthcheck

	Base class for all perl module based healthchecks. Override the run method
	to implement your test.
	
	Use the dba method to get a database connection.
	
	Use the methods log, progress, problem and correct for output. These 
	methods add prefixes that are human readable an they are also parsed 
	in org.ensembl.healthcheck.testcase.AbstractPerlModuleBasedTestCase
	in order to decide what to do with the output.
	
	Don't use print in your test.

=cut
package Bio::EnsEMBL::Healthcheck;
use warnings;
use strict;
use Carp;
use Log::Log4perl qw(get_logger);

$|=1;

sub new {
	my ($class) = shift;
	$class = ref($class) || $class;
	my $self =
	  ( @_ && defined $_[0] && ( ref( $_[0] ) eq 'HASH' ) ) ? $_[0] : {@_};
	bless( $self, $class );
	if ( !$self->log() ) {
		$self->_initialise_logger();
		$self->log( get_logger() );
	}
	return $self;
}

=head2 _initialise_logger

	See here
	http://search.cpan.org/~mschilli/Log-Log4perl-1.33/lib/Log/Log4perl.pm#Configuration_files
	for details on how to configure.

=cut
sub _initialise_logger {
	
	Log::Log4perl->init( {
		
		"log4perl.rootLogger"                           => "DEBUG, A1",
        "log4perl.appender.A1"                          => "Log::Log4perl::Appender::Screen",
        "log4perl.appender.A1.layout"                   => "PatternLayout",

        # The LOG: prefix is used in 
        # org.ensembl.healthcheck.testcase.AbstractPerlModuleBasedTestCase
        # to determine that the output is a log message.
        #
        "log4perl.appender.A1.layout.ConversionPattern" => "LOG: %p - %c - %m%n",
        
	} );
}

=head2 get_healthcheck_name

Gets the last part of the package name. This is the name under which the
healthcheck will most commonly be known.

Example: Bio::EnsEMBL::Healthcheck::Translation -> Translation

=cut
sub get_healthcheck_name {

	my $self = shift;

	my @package_parts = split '::', ref $self;
	my $last_part = pop @package_parts;
	return $last_part;
}

sub create_external_result_file_name {

	my $self = shift;

	my $dbname = $self->dba->dbc->dbname;

	my $healthcheck_name = $self->get_healthcheck_name;

	my $dir           = "external_reports/$dbname";
	my $file_basename = "${healthcheck_name}.log";

	use File::Spec;
	use Cwd;

	my $file = File::Spec->join(cwd(), $dir, $file_basename);

	return ($file, $dir, $file_basename);
}

sub open_external_result_file {

	my $self = shift;

	(my $file, my $dir, my $file_basename)
		= $self->create_external_result_file_name();

	use File::Path qw( make_path );
	make_path ($dir);

	use IO::File;
	my $fh = IO::File->new($file, 'w');

	confess("Can't open file $file for writing!")
		unless (defined $fh);

	return ($fh, $file);
}

sub dba {
	my $self = shift;
	$self->{dba} = shift if @_;

	confess("Type eror!")
	  unless('Bio::EnsEMBL::DBSQL::DBAdaptor');

	return $self->{dba};
}

=head2 log

	Getter and setter for the logger. Initialised in the constructor. Use it 
	like this:

	$self->log()->info("Doing important things");

=cut
sub log {
	my $self = shift;
	$self->{log} = shift if @_;
	return $self->{log};
}

=head2 progress

	Use this to print messages indicating the progress of your healthcheck.
	
	Progress indicators are meant to show the user how far the test has 
	progressed and assure him it has not crashed. They are not stored in 
	memory in the ReportManager or go to logfiles.  

=cut
sub progress {
	
	my $self = shift;
	my $msg  = shift;
	
	print STDOUT "PROGRESS:$msg\n";
}

=head2 problem

	Use this to indicate that there is a problem with the data that your 
	healthcheck is testing. If something is written to problem, the test
	is considered to have failed.

=cut
sub problem {
	my ($self,$msg) = @_;
	
	if ($msg=~/\n/) {
		my @single_line = split "\n", $msg;
		
		foreach my $current_single_line (@single_line) {
			$self->problem($current_single_line);
		}
	}
	
	print STDERR "PROBLEM:$msg\n";
}

=head2 correct

	Messages sent to correct will appear in the report. They do not affect the
	outcome of the test.

=cut
sub correct {
	my ($self,$msg) = @_;
	print STDOUT "CORRECT:$msg\n";
}

sub run {
	croak "run subroutine must be overridden in test";
}

1;
