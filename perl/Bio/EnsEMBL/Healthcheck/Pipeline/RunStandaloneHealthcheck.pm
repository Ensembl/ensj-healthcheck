use strict;
use warnings;

package Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheck;
use base ('Bio::EnsEMBL::Hive::Process');

use Bio::EnsEMBL::Hive::Utils::URL;
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

sub run {

    my $self = shift @_;

    my $hc_jar = $self->param('hc_jar');

    my (undef,$fail_file) = tempfile('_HealthcheckDatabase_XXXXXX',  SUFFIX => '.json', TMPDIR => 1, OPEN=>0);
    my (undef,$log_file) = tempfile('_HealthcheckDatabase_XXXXXX',  SUFFIX => '.log', TMPDIR => 1, OPEN=>0);

    my $command = sprintf("java -jar %s --output_format json --output_file %s --release %s", $hc_jar, $fail_file, software_version());

    $command .= get_db_str( $self->param_required('db_uri'));

    my $hc_names = $self->param('hc_names');
    my $hc_groups = $self->param('hc_groups');

    croak "No healthchecks or groups supplied" if !defined $hc_names && !defined $hc_groups;

    $command .= ' -t '.join(' ', @$hc_names) if defined $hc_names;
    $command .= ' -g '.join(' ', @$hc_groups) if defined $hc_groups;

    $command .= get_db_str($self->param('prod_uri'), 'prod_');
    $command .= get_db_str($self->param('compara_uri'), 'compara_');
    $command .= get_db_str($self->param('live_uri'), 'secondary_');

    $command .= " >& $log_file";
    my $output = {
		  db_uri=>$self->param_required('db_uri'),
		  command=>$command
		 };

    $output->{hc_groups} = $hc_groups if defined $hc_groups;
    $output->{hc_names} = $hc_names if defined $hc_names;

    $logger->info($command);
    my $exit = system($command);
    $exit >>= 8;
    $logger->info("Exited with status $exit");
    if($exit == 0) {
      $logger->info("No failures found");
      $output->{status} = 'passed';
    } elsif($exit == 1) {
      $logger->info("Failures found");
      $output->{status} = 'failed';
      $output->{failures} = decode_json(read_file($fail_file));
    } else {
      my $log = read_file($log_file);
      croak "Could not execute $command: $log";
    }
    unlink $log_file;    
    $logger->debug(Dumper($output));
    $self->dataflow_output_id({
			       job_id=>$self->input_job()->dbID(),
			       output=>encode_json($output)
			      }, 2);
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
