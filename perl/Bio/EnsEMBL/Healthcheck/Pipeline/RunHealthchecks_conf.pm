=pod 
=head1 NAME

=head1 SYNOPSIS

=head1 DESCRIPTION  

=head1 LICENSE
    Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
    Copyright [2016-2021] EMBL-European Bioinformatics Institute
    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and limitations under the License.
=head1 CONTACT
    Please subscribe to the Hive mailing list:  http://listserver.ebi.ac.uk/mailman/listinfo/ehive-users  to discuss Hive-related questions or to be notified of our updates
=cut


package Bio::EnsEMBL::Healthcheck::Pipeline::RunHealthchecks_conf;

use strict;
use warnings;
use Data::Dumper;
use base ('Bio::EnsEMBL::Hive::PipeConfig::HiveGeneric_conf');  # All Hive databases configuration files should inherit from HiveGeneric, directly or indirectly

sub resource_classes {
    my ($self) = @_;
    return { 'default' => { 'LSF' => '-q production-rh74' },
              'himem' =>
              { 'LSF' => '-q production-rh74 -M 16384 -R "rusage[mem=16384]"' }
    };
}

sub default_options {
    my ($self) = @_;
    return {
        %{$self->SUPER::default_options},
        'hc_conn'=>undef,
        'prod_conn'=> undef,
        'hc_cmd'=>undef,
        'division'=>undef
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
                     'hc_conn'    => $self->o('hc_conn'),
                     'prod_conn'   => $self->o('prod_conn'),
                     'division'    => $self->o('division')                        
                 }
                ],
            -parameters => {
            },
            -flow_into => {
                1 => 'finish_session' ,
                2 => 'run_healthcheck'
            }
        },
        
        {   
            -logic_name    => 'run_healthcheck',
            -module        => 'Bio::EnsEMBL::Hive::RunnableDB::SystemCmd',
            -meadow_type => 'LSF',
            -parameters    => {
                'cmd'        => $self->o('hc_cmd'),
                'division'    => $self->o('division')                        
            },
            -analysis_capacity => 10,
                    -rc_name => 'himem'
        },
        {
            -logic_name => 'finish_session',
            -module => 'Bio::EnsEMBL::Hive::RunnableDB::SqlCmd',
            -parameters => {
                db_conn => $self->o('hc_conn'),
                sql => 'update session set end_time=NOW() where session_id="#session_id#"'
            },
                    -wait_for => ['run_healthcheck']
        }
        ];
    return $anal;
}
1;
