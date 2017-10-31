use strict;
use warnings;

package Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheckMerge;
use base ('Bio::EnsEMBL::Hive::Process');

use Bio::EnsEMBL::Hive::Utils::URL;

use Carp qw/croak/;
use Data::Dumper;
use File::Slurp qw/read_file/;
use File::Temp qw/tempfile/;
use JSON;
use Log::Log4perl qw/:easy/;

my $logger = get_logger();
if(!Log::Log4perl->initialized()) {
  Log::Log4perl->easy_init($DEBUG);
}

# Run a single HC against a single DB
sub run {

    my $self = shift @_;

    my $token = $self->param_required('init_job_id');
    my $db_uri = $self->param_required('db_uri');
    my $db = Bio::EnsEMBL::Hive::Utils::URL::parse($db_uri)->{dbname};
    my $results = $self->param('hc_output');

    my $output = {
		  db_name=>$db,
		  db_uri=>$db_uri,
		  results=>{},
		  status=>'passed'
		 };

    for my $result (@{$results}) {
      if($result->{status} eq 'failed') {
	$output->{status} = 'failed';
      }
      $output->{results}->{$result->{hc_name}} = {
						  status=>$result->{status},
						  messages=>$result->{messages}
						 };
    }
    $self->dataflow_output_id(	   
			      {
			       job_id => $token,
			       output=>encode_json(
						   $output
						  )
			      }
			      , 2);
    return;

}

sub get_db_str {
  my ($uri, $suffix) = @_;
  return '' unless defined $uri;
  $suffix ||= '';
  my $db = Bio::EnsEMBL::Hive::Utils::URL::parse($uri);
  my $db_str = sprintf(" --%shost %s --%sport %s --%suser %s", $suffix, $db->{host}, $suffix, $db->{port}, $suffix, $db->{user});
  if(defined $db->{pass}) {
    $db_str .= sprintf(" --%spass %s", $suffix, $db->{pass});
  }
  if(defined $db->{dbname}) {
    $db_str .= sprintf(" --%sdbname %s", $suffix, $db->{dbname});
  }
  return $db_str;
}

1;
