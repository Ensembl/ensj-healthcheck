package Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthchecksParallel_conf;
use warnings;
use strict;
use parent 'Bio::EnsEMBL::Hive::PipeConfig::EnsemblGeneric_conf';

sub default_options {
  my ($self) = @_;
  return { %{ $self->SUPER::default_options }, 'hc_jar' => undef, };
}

sub pipeline_analyses {
  my $self = shift;
  return [ {
      -logic_name => 'RunStandaloneHealthcheckFactory',
      -module =>
        'Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheckFactory'
      -input_ids   => [],        # required for automatic seeding
      -parameters => { hc_jar => $self->o('hc_jar') },
      -flow_into  => {'2->A' => ['RunStandaloneHealthcheckParallel'],
                      'A->1' => ['RunStandaloneHealthcheckMerge'] } }, {
      -logic_name => 'RunStandaloneHealthcheckParallel',
      -module =>
        'Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheckParallel',
      -rc_name       => 'default',
      -hive_capacity => 30,
      -parameters    => { hc_jar => $self->o('hc_jar'), java_opts => '' },
      -flow_into     => {
                      -1 => ['RunStandaloneHealthcheckParallelHimem'],
                      2  => ['?accu_name=hc_output&accu_address=[]'] } }, {
      -logic_name => 'RunStandaloneHealthcheckParallelHimem',
      -module =>
        'Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheckParallel',
      -rc_name       => 'himem',
      -hive_capacity => 30,
      -parameters => { hc_jar => $self->o('hc_jar'), java_opts => '-Xmx16g' },
      -flow_into => { 2 => ['?accu_name=hc_output&accu_address=[]'] } }, {
      -logic_name => 'RunStandaloneHealthcheckMerge',
      -module =>
        'Bio::EnsEMBL::Healthcheck::Pipeline::RunStandaloneHealthcheckMerge'
      -parameters  => {},
      -flow_into   => {
        2 => ['?table_name=result']

      } }, ];
} ## end sub pipeline_analyses

sub pipeline_create_commands {
  my ($self) = @_;
  return [
    @{ $self->SUPER::pipeline_create_commands }
    ,    # inheriting database and hive tables' creation

    # additional tables needed for long multiplication pipeline's operation:
    $self->db_cmd(
       'CREATE TABLE result (job_id int(10), output LONGTEXT, PRIMARY KEY (job_id))'
    ),
    $self->db_cmd('CREATE TABLE job_progress (job_progress_id int(11) NOT NULL AUTO_INCREMENT, job_id int(11) NOT NULL , message TEXT,  PRIMARY KEY (job_progress_id))'),
    $self->db_cmd('ALTER TABLE job_progress ADD INDEX (job_id)'),
    $self->db_cmd('ALTER TABLE job DROP KEY input_id_stacks_analysis'),
    $self->db_cmd('ALTER TABLE job MODIFY input_id TEXT') ];
}

sub resource_classes {
  my ($self) = @_;
  return {
     'default' => { 'LSF' => '-q production-rh7' },
     'himem' => { 'LSF' => '-q production-rh7 -M 16384 -R "rusage[mem=16384]"' }
  };
}

1;
