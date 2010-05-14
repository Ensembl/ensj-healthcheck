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

sub new {
	my $caller = shift;
	my $class  = ref($caller) || $caller;
	my $self   = $class->SUPER::new(@_);
}

sub run {
	my ($self) = @_;
	my $passes = 1;
	$self->log()->debug("Getting all protein coding genes");
	my $genes =
	  $self->dba()->get_GeneAdaptor()->fetch_all_by_biotype("protein_coding");
	for my $gene ( @{$genes} ) {
		for my $transcript ( @{ $gene->get_all_Transcripts() } ) {
			if($transcript) {}
			my $seq = $transcript->translate();
			if ($seq) {
				if ( $seq->seq() =~ m/\*.+/ ) {
					$self->problem( "Translation for "
						  . $transcript->dbID
						  . " contains stop codons: ".$seq->seq() );
					$passes = 0;
				}
			} else {
				$self->problem( "No translation found for transcript ID "
					  . $transcript->dbID );
			}
		}
	}
	return $passes;
}
1;
