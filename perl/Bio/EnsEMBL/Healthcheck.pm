# $Source$
# $Revision$
# $Date$
# $Author$
#
# Base object for all classes responsible for loading data into an ensembl database
#
package Bio::EnsEMBL::Healthcheck;
use warnings;
use strict;
use Carp;
use Log::Log4perl qw(get_logger);

sub new {
	my ($class) = shift;
	$class = ref($class) || $class;
	my $self =
	  ( @_ && defined $_[0] && ( ref( $_[0] ) eq 'HASH' ) ) ? $_[0] : {@_};
	bless( $self, $class );
	if ( !$self->log() ) {
		$self->log( get_logger() );
	}
	return $self;
}

sub dba {
	my $self = shift;
	$self->{dba} = shift if @_;
	return $self->{dba};
}

sub log {
	my $self = shift;
	$self->{log} = shift if @_;
	return $self->{log};
}

sub problem {
	my ($self,$msg) = @_;
	print STDERR "PROBLEM:$msg\n";
}

sub correct {
	my ($self,$msg) = @_;
	print STDOUT "CORRECT:$msg\n";
}

sub run {
	croak "run subroutine must be overridden in test";
}
1;
