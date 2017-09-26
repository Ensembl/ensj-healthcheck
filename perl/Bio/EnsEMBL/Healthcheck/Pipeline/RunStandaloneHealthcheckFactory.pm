use strict;
use warnings;

package Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheckFactory;
use base ('Bio::EnsEMBL::Hive::Process');

use Bio::EnsEMBL::ApiVersion;

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

    my (undef,$list_file) = tempfile('_HealthcheckDatabaseList_XXXXXX',  SUFFIX => '.json', TMPDIR => 1, OPEN=>0);
    my (undef,$log_file) = tempfile('_HealthcheckDatabaseList_XXXXXX',  SUFFIX => '.log', TMPDIR => 1, OPEN=>0);

    my $hc_jar = $self->param('hc_jar');   
    my $db_uri = $self->param_required('db_uri');

    my $live_uri = $self->param('live_uri');
    my $staging_uri = $self->param('staging_uri');
    my $production_uri = $self->param('production_uri');
    my $compara_uri = $self->param('compara_uri');

    my $command = sprintf("java -jar %s -l --output_format json --output_file %s", $hc_jar, $list_file);

    my $hc_names = $self->param('hc_names');
    if(defined $hc_names && ref($hc_names) ne 'ARRAY') {
      $hc_names = [$hc_names];
    }

    my $hc_groups = $self->param('hc_groups');
    if(defined $hc_groups && ref($hc_groups) ne 'ARRAY') {
      $hc_groups = [$hc_groups];
    }

    croak "No healthchecks or groups supplied" if !defined $hc_names && !defined $hc_groups;

    $command .= ' -t '.join(' ', @$hc_names) if defined $hc_names;
    $command .= ' -g '.join(' ', @$hc_groups) if defined $hc_groups;
    $command .= " >& $log_file";

    $logger->info($command);
    my $exit = system($command);
    $exit >>= 8;
    $logger->info("Exited with status $exit");
    if($exit == 0) {
      my $testcases = decode_json(read_file($list_file));
      for my $testcase (@{$testcases}) {
	$logger->info("Submitting job for $testcase vs $db_uri");
	$self->dataflow_output_id({
				   init_job_id=>$self->input_job()->dbID(),
				   db_uri => $db_uri,
				   hc_name => $testcase,
				   production_uri=>$production_uri,
				   live_uri=>$live_uri,
				   staging_uri=>$staging_uri,
				   compara_uri=>$compara_uri				   
				  }, 2);
      }
      unlink $log_file;    
      unlink $list_file;    
    } else {
      my $log = read_file($log_file);
      croak "Could not execute $command: $log";
    }
    # main semaphore flow to 1 with original input
    $self->dataflow_output_id(
			      {
			       init_job_id=>$self->input_job()->dbID(),
			       db_uri=>$db_uri,
			       production_uri=>$production_uri,
			       live_uri=>$live_uri,
			       staging_uri=>$staging_uri,
			       compara_uri=>$compara_uri
			      },
			      1
			     );

    return;

}

1;
