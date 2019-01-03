=pod 
=head1 NAME

=head1 SYNOPSIS

=head1 DESCRIPTION  

=head1 LICENSE
    Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
    Copyright [2016-2019] EMBL-European Bioinformatics Institute
    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and limitations under the License.
=head1 CONTACT
    Please subscribe to the Hive mailing list:  http://listserver.ebi.ac.uk/mailman/listinfo/ehive-users  to discuss Hive-related questions or to be notified of our updates
=cut


package Bio::EnsEMBL::Healthcheck::Pipeline::RunHealthchecks_ens_conf;

use strict;
use warnings;
use Data::Dumper;
use base ('Bio::EnsEMBL::Hive::PipeConfig::HiveGeneric_conf');  # All Hive databases configuration files should inherit from HiveGeneric, directly or indirectly

sub resource_classes {
    my ($self) = @_;
    return { 'default' => { 'LSF' => '-q production-rh7' },
              'himem' =>
              { 'LSF' => '-q production-rh7 -M 5000 -R "rusage[mem=5000]"' }
    };
}

sub default_options {
    my ($self) = @_;
    return {
        %{$self->SUPER::default_options},
        'hc_conn'    => undef,
        'prod_conn'  => undef,
        'hc_cmd'     => undef,
        'exclude_dbs' => undef,
        'properties' => undef,
        'hcdb'       => undef,
        'release'    => undef,
        'host'       => undef,
        'group'      => undef,
        'division'   => undef,
         priority => {
      species => [qw/homo_sapiens mus_musculus danio_rerio rattus_norvegicus/],
      group => [qw/core variation funcgen/],
    },
    }
}

=head2 pipeline_wide_parameters
=cut

sub pipeline_wide_parameters {
    my ($self) = @_;
    return {
        %{$self->SUPER::pipeline_wide_parameters}          # here we inherit anything from the base class, then add our own stuff
    };
}


=head2 pipeline_analyses
=cut

sub pipeline_analyses {
    my ($self) = @_;
    my $anal = [
        {   
            -logic_name => 'db_factory',
            -module     => 'Bio::EnsEMBL::Healthcheck::Pipeline::DatabaseJobFactory',
            -input_ids => [
                 {
                     'hc_conn'     => $self->o('hc_conn'),
                     'prod_conn'   => $self->o('prod_conn'),
                     'exclude_dbs' => $self->o('exclude_dbs'),
                     'properties'  => $self->o('properties'),
                     'group'       => $self->o('group'),
                     'release'     => $self->o('release'),
                     'host'        => $self->o('host'),
                     'division'    => $self->o('division')                        
                 }
                ],
            -parameters => {
            },
            -flow_into => {
                1 => 'finish_session' ,
                2 => 'prioritise'
            }
        },
        {
            -logic_name => 'prioritise',
            -module => 'Bio::EnsEMBL::Healthcheck::Pipeline::Prioritise',
            -parameters => { priority => $self->o('priority') },
            -flow_into => {
                2 => ['run_healthcheck'],
                3 => ['high_priority_run_healthcheck'],
                4 => ['super_priority_run_healthcheck'],
                5 => ['human_variation_run_healthcheck']
            }
        },

       {    -logic_name => 'human_variation_run_healthcheck',
            -module => 'Bio::EnsEMBL::Hive::RunnableDB::SystemCmd',
            -meadow_type => 'LSF',
            -parameters    => {
                'cmd'         => $self->o('hc_cmd'),
                'properties'  => $self->o('properties'),
                'group'       => $self->o('group'),
                'hcdb'        => $self->o('hcdb'),
                'division'    => $self->o('division')
            },
            -can_be_empty => 1,
            -hive_capacity => 30,
            -priority => 30,
            -rc_name => 'himem',
            -max_retry_count => 10
        },

        {
            -logic_name => 'super_priority_run_healthcheck',
            -module => 'Bio::EnsEMBL::Hive::RunnableDB::SystemCmd',
            -meadow_type => 'LSF',
            -parameters    => {
                'cmd'         => $self->o('hc_cmd'),
                'properties'  => $self->o('properties'),
                'group'       => $self->o('group'),
                'hcdb'        => $self->o('hcdb'),
                'division'    => $self->o('division')
            },
            -hive_capacity => 30,
            -priority => 20,
            -can_be_empty => 1,
            -rc_name => 'himem',
            -max_retry_count => 10
        },

        {
            -logic_name => 'high_priority_run_healthcheck',
            -module => 'Bio::EnsEMBL::Hive::RunnableDB::SystemCmd',
            -meadow_type => 'LSF',
            -parameters    => {
                'cmd'         => $self->o('hc_cmd'),
                'properties'  => $self->o('properties'),
                'group'       => $self->o('group'),
                'hcdb'        => $self->o('hcdb'),
                'division'    => $self->o('division')
            },
            -hive_capacity => 30,
            -priority => 10,
            -can_be_empty => 1,
            -wait_for => [qw/prioritise/],
            -rc_name => 'himem',
            -max_retry_count => 10
        },
        
        {
            -logic_name    => 'run_healthcheck',
            -module        => 'Bio::EnsEMBL::Hive::RunnableDB::SystemCmd',
            -meadow_type => 'LSF',
            -parameters    => {
                'cmd'         => $self->o('hc_cmd'),
                'properties'  => $self->o('properties'),
                'group'       => $self->o('group'),
                'hcdb'        => $self->o('hcdb'),
                'division'    => $self->o('division')
            },
            -hive_capacity => 30,
            -rc_name => 'default',
            -wait_for => [qw/prioritise/],
            -max_retry_count => 10
        },
        {
            -logic_name => 'finish_session',
            -module => 'Bio::EnsEMBL::Hive::RunnableDB::SqlCmd',
            -parameters => {
                db_conn => $self->o('hc_conn'),
                sql => 'update session set end_time=NOW() where session_id="#session_id#"'
            },
                    -wait_for => [qw/human_variation_run_healthcheck super_priority_run_healthcheck high_priority_run_healthcheck run_healthcheck/]
        }
        ];
    return $anal;
}
1;
