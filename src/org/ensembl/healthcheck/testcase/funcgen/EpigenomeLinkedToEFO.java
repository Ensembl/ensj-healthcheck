package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

public class EpigenomeLinkedToEFO extends AbstractTemplatedTestCase {
  
  public EpigenomeLinkedToEFO() {
    this.setTeamResponsible(Team.FUNCGEN);
  }

  @Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    
    SqlTemplate s = getTemplate(dbre);
    
    List<String> epigenomesWithoutEFOLink = s.queryForDefaultObjectList(
          "select name from epigenome where epigenome_id not in ("
        + "	select "
        + "		ensembl_id "
        + "	from  "
        + "		external_db " 
        + "		join xref on (external_db.external_db_id=xref.external_db_id and external_db.db_name=\"EFO\") " 
        + "		join object_xref on (object_xref.xref_id=xref.xref_id and object_xref.ensembl_object_type=\"Epigenome\") " 
        + " )",
        String.class
     );
    if (epigenomesWithoutEFOLink.size()==0) {
      return true;
    }
    Iterator<String> i = epigenomesWithoutEFOLink.iterator();
    while (i.hasNext()) {
      ReportManager.problem(this, dbre.getConnection(), 
          "The epigenome " + i.next() + " is not linked to the Experimental Factor Ontology."
      );
    }
    return false;
  }

}
