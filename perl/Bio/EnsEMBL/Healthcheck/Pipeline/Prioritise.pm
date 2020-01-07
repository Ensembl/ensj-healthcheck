
=head1 LICENSE

Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
Copyright [2016-2020] EMBL-European Bioinformatics Institute

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

package Bio::EnsEMBL::Healthcheck::Pipeline::Prioritise;
use base ('Bio::EnsEMBL::Hive::RunnableDB::JobFactory');  # All Hive databases configuration files should inherit from HiveGeneric, directly or indirectly

use strict;
use warnings;

sub param_defaults {
  my ($self) = @_;
  return { priority => { species => [], group => [] } };
}

sub run {
  my ($self) = @_;

  my $database = $self->param('dbname');
  my $priority = 0;

  foreach my $species ( @{ $self->param('priority')->{species} } ) {
    if ( $database =~ /^$species/xms ) {
      $priority++;
      $self->warning(
        "DB name ${database} matched the prioritised species ${species}"
      );
      last;
    }
  }
  foreach my $group ( @{ $self->param('priority')->{group} } ) {
    if ( $database =~ /_${group}_/xms ) {
      $priority++;
      $self->warning(
          "DB name ${database} matched the prioritised group ${group}");
      last;
    }
  }

  $priority = $self->prioritise_human_variation( $database, $priority );
  $self->param( 'priority', $priority );

  return;
} ## end sub run

sub prioritise_human_variation {
  my ( $self, $database, $priority ) = @_;
  if ( $database =~ /^homo_sapiens/ && $database =~ /_variation_/ ) {
    $priority++;
  }
  return $priority;
}

sub write_output {
  my ($self) = @_;
  my $priority_to_flow = { 0 => 2,    #basic flow
                           1 => 3,    #higher
                           2 => 4,    #highest
                           3 => 5,    #special human variation
  };
  my $dataflow = $priority_to_flow->{ $self->param('priority') };
  my $database = $self->param('dbname');
  my $session_id = $self->param('session_id');
  $self->dataflow_output_id( {dbname=>$database,session_id=>$session_id},
                             $dataflow );
  return;
}

1;
