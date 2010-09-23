# $Source$
# $Revision$
# $Date$
# $Author$
#
# Object for loading data into an ensembl database
#
package Bio::EnsEMBL::Healthcheck::Translation;
use warnings;
use strict;
use Carp;
use base qw(Bio::EnsEMBL::Healthcheck);
use Data::Dumper;
use Bio::Seq;

sub new {
	my $caller = shift;
	my $class  = ref($caller) || $caller;
	my $self   = $class->SUPER::new(@_);
}

=head2 run

	Iterates over all protein coding genes, checks if there is a stop codon 
	in them.
	
	If there were genes with internal stop codons, returns a table with all
	transcripts and all their stop codons.

=cut
sub run {
	my ($self) = @_;
	my $passes = 1;
	$self->log()->debug("Getting all protein coding genes");
	my $genes = $self->dba()->get_GeneAdaptor()->fetch_all_by_biotype("protein_coding");
	
	my $problem_report_tabular_all;
	my $table_header;
	
	for my $gene ( @{$genes} ) {
		for my $transcript ( @{ $gene->get_all_Transcripts() } ) {
			my $seq = $transcript->translate();
			if ($seq) {
				
				my $sequence = $seq->seq();
				
				if ( $sequence =~ m/\*.+/ ) {
					
					$passes = 0;
					
					my $problem_report_tabular;
					my $recommended_fixes;

					($problem_report_tabular, $table_header) = $self->_report_problem_for_transcript($transcript);

					$problem_report_tabular_all .= $problem_report_tabular; 		

					$self->problem( "Transcript for "
						  . "\ndbID:"                   . $transcript->dbID
						  . "\ndisplay_id:"             . $transcript->display_id
						  . "\ntranscript stable_id: "  . $transcript->stable_id
						  . "\ncontains stop codons:\n" . $sequence
					);
					# Reporting all coordinates of stop codons during the run 
					# is too verbose, hence commented out.
					#
					#$self->problem($table_header . "\n" . $problem_report_tabular);
										
				}
			} else {
				$self->problem( "No translation found for transcript ID "
					  . $transcript->dbID );
			}
		}
	}
	#
	# If there were problems, report the details in tabluar format in the end.
	#
	if (!$passes) {
		$self->problem(
			  "\n\n---------------- problems: -------------------\n\n"
			  
			. $table_header . "\n" . $problem_report_tabular_all				  
			  
		);
	}
	
	return $passes;
}

=head2 _report_problem_for_transcript

	Finds all stop codons in the the protein sequence of a 
	Bio::EnsEMBL::Transcript and returns them as a tab separated table. The 
	second return value is a string with the headers in tab separated format.

=cut
sub _report_problem_for_transcript {

	my $self        = shift;
	my $transcript  = shift;
	
	confess("Type error") unless($transcript->isa('Bio::EnsEMBL::Transcript'));
	
	my $seq = $transcript->translate();
	
	confess("Type error") unless($seq->isa('Bio::Seq'));
	
	my $table_header = join "\t", (
			"transcript",
			"dbID",
			"start",
			"start+internal_stop_position",
			"end",
			"description",
			"display_id",
			"stable_id",			
			"current_stop_codon",
		);
	my $result;
	
	my $sequence   = $seq->seq();	
	my $stop_codon = $self->_arrayref_of_stop_codons($sequence);

	my $recommended_fix;

	foreach my $current_stop_codon (@$stop_codon) { 

		my $coordinate_of_stop_codon_in_transcript = $transcript->start + $current_stop_codon; 

		if (!defined $transcript->dbID)                       { confess("dbID is not defined for transcript:\n" .        Dumper($transcript)) }
		if (!defined $transcript->start)                      { confess("start is not defined for transcript:\n" .       Dumper($transcript)) }
		if (!defined $transcript->end)                        { confess("end is not defined for transcript:\n" .         Dumper($transcript)) }
		#
		# Otherwise the join later will complain about an undefined value
		#
		if (!defined $transcript->description)                { $transcript->description("") }
		if (!defined $transcript->display_id)                 { confess("display_id is not defined for transcript:\n" .  Dumper($transcript)) }
		if (!defined $transcript->stable_id)                  { confess("stable_id is not defined for transcript:\n" .   Dumper($transcript)) }
		if (!defined $coordinate_of_stop_codon_in_transcript) { confess("coordinate_of_stop_codon_in_transcript are not defined for transcript:\n" . Dumper($transcript)) }

		$result .= join "\t", (
			"transcript",
			$transcript->dbID,
			$transcript->start,
			$coordinate_of_stop_codon_in_transcript,
			$transcript->end,
			$transcript->description,
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
