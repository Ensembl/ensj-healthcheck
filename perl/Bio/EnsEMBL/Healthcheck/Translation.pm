=head1 LICENSE

Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
Copyright [2016-2018] EMBL-European Bioinformatics Institute

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
=head1 Bio::EnsEMBL::Healthcheck::Translation

	Protein tanslation healthcheck

	Iterates over all protein coding genes of a core database, checks every 
	gene, if it contains stop codons or comprises only of Xs. Fails, if it 
	finds such a gene. 

=cut
package Bio::EnsEMBL::Healthcheck::Translation;
use warnings;
use strict;
use Carp;
use base qw(Bio::EnsEMBL::Healthcheck);
use Data::Dumper;
use Bio::Seq;
use Bio::EnsEMBL::TranscriptMapper;
use Bio::EnsEMBL::Mapper::Coordinate;
sub new {
	my $caller = shift;
	my $class  = ref($caller) || $caller;
	my $self   = $class->SUPER::new(@_);
	
	return $self;
}

sub createGeneStableIdIterator {
	
	my $self = shift;

	my $dba = $self->dba();
	
	my $sql =
      'select stable_id from gene join seq_region using (seq_region_id) join coord_system using (coord_system_id) '
      . 'where biotype="protein_coding" and species_id=' . $dba->species_id();

	my $db_connection = $self->dba()->dbc();

	my $sth = $db_connection->prepare($sql);

	$sth->execute();

	return sub {
		
		my $result = $sth->fetchrow_array();
		
		if ($result) {
			return $result;
		}
		
		$sth->finish();
		return 
	}
}

sub fetchNumberOfGenes {
	
	my $self = shift;

	my $dba = $self->dba();

	my $sql =
      'select count(*) from gene join seq_region using (seq_region_id) join coord_system using (coord_system_id) '
      . 'where biotype="protein_coding" and species_id=' . $dba->species_id();

	my $sth = $self->dba()->dbc->prepare($sql);
	$sth->execute();

	return $sth->fetchrow_array();
}

=head2 run

	Iterates over all protein coding genes, checks if there is a stop codon 
	in them or if the sequence comprises only of Xs.
	
	If there were genes with problems, returns a table with all
	transcripts and all their stop codons.

=cut
sub run {
	my ($self) = @_;

	(my $fh, my $file) = $self->open_external_result_file();

	# Indicates whether or not the test has passed.
	#
	my $passes = 1;

	$self->log()->debug("Getting all protein coding genes");
	
	my $problem_report_stop_codons_tabular_all;
	my $table_header_report_stop_codons;
	
	#my $num_of_genes = @{$genes};
	my $num_of_genes = $self->fetchNumberOfGenes();
	my $genes_tested = 0;
	my $num_genes_until_lifesign_printed = 50;
	my $num_genes_until_printed_since_last_lifesign = 0;

	my $user_notified_of_problems = 0;
	
	# Used for testing so a run of this testcase doesn't take forever.
	#
	my $max_genes_to_test  = 100;
	my $only_test_upto_max = 0; 
	
	my $stable_id_iterator = $self->createGeneStableIdIterator;
	my $gene_adaptor = $self->dba()->get_GeneAdaptor();
	
	while ( my $current_stable_id = $stable_id_iterator->() ) {
		
		my $gene = $gene_adaptor->fetch_by_stable_id($current_stable_id); 
		
		if ($only_test_upto_max && $genes_tested>$max_genes_to_test) {
			return $passes;
		}
		
		for my $transcript ( @{ $gene->get_all_Transcripts() } ) {

			my $seq = $transcript->translate();
			next unless $transcript->biotype() eq 'protein_coding';
			if ($seq) {
				
				my $sequence = $seq->seq();

				if ($self->sequence_comprises_only_of_Xs($sequence)) {

					$passes = 0;
					
					$fh->print( "Transcript for "
						  . "\ndbID: ".$transcript->dbID
						  . "\ndisplay_id: ".$transcript->display_id
						  . "\ntranscript stable_id: ".$transcript->stable_id
						  . "\ncomprises only of X's\n"
					);
				}
				
				if ( $sequence =~ m/\*/ ) {
					
					$passes = 0;
					
					my $problem_report_tabular;
					my $recommended_fixes;

					($problem_report_tabular, $table_header_report_stop_codons) = $self->report_problem_for_transcript($transcript);

					$problem_report_stop_codons_tabular_all .= $problem_report_tabular; 		

					$fh->print( "Transcript for "
						  . "\ndbID: ".$transcript->dbID
						  . "\ndisplay_id: ".$transcript->display_id
						  . "\ntranscript stable_id: ".$transcript->stable_id
						  . "\ncontains stop codons: $sequence\n"
					);
					# Reporting all coordinates of stop codons during the run 
					# is too verbose, hence commented out.
					#
					#$self->problem($table_header_report_stop_codons . "\n" . $problem_report_tabular);
										
				}
			} else {
				$fh->print( "No translation found for transcript ID "
					  . $transcript->dbID );
			}
		}
		$genes_tested++;
		$num_genes_until_printed_since_last_lifesign++;
		
		if ($num_genes_until_printed_since_last_lifesign>=$num_genes_until_lifesign_printed) {
			
			$self->progress("Tested $genes_tested out of $num_of_genes genes.");
			$num_genes_until_printed_since_last_lifesign = 0;
		}
		if (!$passes && !$user_notified_of_problems) {
			$self->problem("This database contains transcripts with incorrect translations. See $file for detailed error messages.");
			$user_notified_of_problems = 1;
		}
	}
	#
	# If there were problems, report the details in tabluar format in the end.
	#
	if (!$passes) {
		$fh->print(
			  "\n\n---------------- problems: -------------------\n\n"
			  
			. $table_header_report_stop_codons . "\n" . $problem_report_stop_codons_tabular_all				  
			  
		);
	}
	$fh->close();
	return $passes;
}

=head2 sequence_comprises_only_of_Xs

	Checks whether a protein sequence comprises only of Xs.

=cut
sub sequence_comprises_only_of_Xs {

	my $self                  = shift;
	my $translation_sequence  = shift;
	
	return $translation_sequence =~ /^X+$/;	
}

=head2 report_problem_for_transcript

	Finds all stop codons in the the protein sequence of a 
	Bio::EnsEMBL::Transcript and returns them as a tab separated table. The 
	second return value is a string with the headers in tab separated format.

=cut
sub report_problem_for_transcript {

	my $self        = shift;
	my $transcript  = shift;
	
	confess("Type error") unless($transcript->isa('Bio::EnsEMBL::Transcript'));
	
	my $seq = $transcript->translate();
	
	confess("Type error") unless($seq->isa('Bio::Seq'));
	
	my $table_header = join "\t", (
			"transcript",
			"dbID",
			"start",
			"internal_stop_position",
			"end",
			"seq_region_name",
			"display_id",
			"stable_id",			
			"protein_sequence_position",
		);
	my $result="";
	my $transcript_mapper = Bio::EnsEMBL::TranscriptMapper->new($transcript);
	my $stop_codon = $self->_arrayref_of_stop_codons($seq->seq());

	my $recommended_fix;
 
	foreach my $current_stop_codon (@$stop_codon) { 
		my $coordinate_of_stop_codon_in_transcript =
                   join "," , map {$_->start } $transcript_mapper -> pep2genomic($current_stop_codon,$current_stop_codon);

		if (!defined $transcript->dbID)                       { confess("dbID is not defined for transcript:\n" .        Dumper($transcript)) }
		if (!defined $transcript->start)                      { confess("start is not defined for transcript:\n" .       Dumper($transcript)) }
		if (!defined $transcript->end)                        { confess("end is not defined for transcript:\n" .         Dumper($transcript)) }
		#
		# Otherwise the join later will complain about an undefined value
		#
		if (!defined $transcript->seq_region_name)                { confess("seq_region_name is not defined for transcript:\n" .  Dumper($transcript)) }
		if (!defined $transcript->display_id)                 { confess("display_id is not defined for transcript:\n" .  Dumper($transcript)) }
		if (!defined $transcript->stable_id)                  { confess("stable_id is not defined for transcript:\n" .   Dumper($transcript)) }
		if (!defined $coordinate_of_stop_codon_in_transcript) { confess("coordinate_of_stop_codon_in_transcript are not defined for transcript:\n" . Dumper($transcript)) }

		$result .= join "\t", (
			"transcript",
			$transcript->dbID,
			$transcript->start,
			$coordinate_of_stop_codon_in_transcript,
			$transcript->end,
			$transcript->seq_region_name,
			$transcript->display_id,
			$transcript->stable_id,
			$current_stop_codon,
			"\n"
		);
	}
	return ($result, $table_header);
}

=head2 _arrayref_of_stop_codons

	Takes the sequence of a protein as a string.

	Searches for the stop codon symbol ("*") in the protein sequence
	and returns an array of indexes as an array.

=cut
sub _arrayref_of_stop_codons {

	my $self        = shift;
	my $sequence    = shift;
	my $stop_codons = [];

	my $stop_codon_symbol = "*";

	my $search_start = 0;
	my $index        = 0;

	my $stop_codon_found = 1;

	while ($stop_codon_found) {

		$search_start = $index + 1;
		$index = index($sequence, $stop_codon_symbol, $search_start);
		if ($index==-1) {
			$stop_codon_found = 0;
		}
		push @$stop_codons, $index unless(!$stop_codon_found);
	}
	return $stop_codons;
}

1;
