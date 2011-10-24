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
