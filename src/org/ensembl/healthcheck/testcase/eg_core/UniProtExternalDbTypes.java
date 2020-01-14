/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to find UniProt xrefs which are not: - Uniprot/SPTREMBL -
 * Uniprot/SWISSPROT - Uniprot_gn - Uniprot_gn_gene_name - Uniprot_gn_trans_name - 
 * UniProtKB-KW
 * @author uma
 * 
 */
public class UniProtExternalDbTypes extends AbstractRowCountTestCase {

       public UniProtExternalDbTypes() {
              super();
               this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
                       this.appliesToType(DatabaseType.CORE);
                               this.setTeamResponsible(Team.ENSEMBL_GENOMES);
                                       this.setDescription("Test to find UniProt xrefs that are not in our approved list");
                                       }

       private final static String QUERY = "select count(*) from xref "
                     + "join external_db using (external_db_id) "
                             + "where db_name like 'Uniprot%' and "
                                      + "db_name not in ('Uniprot/SPTREMBL','Uniprot/SWISSPROT','Uniprot/Varsplic','Uniprot_gn','Uniprot_gn_gene_name','Uniprot_gn_trans_name','UniProtKB-KW','Uniprot/Varsplic')";

       /*
        * (non-Javadoc)
         * 
          * @see
           * org.ensembl.healthcheck.testcase.AbstractRowCountTestCase#getExpectedCount
            * ()
             */
             @Override
             protected int getExpectedCount() {
                       return 0;
                       }

       /*
        * (non-Javadoc)
         * 
          * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
           */
           @Override
           protected String getSql() {
                     return QUERY;
                     }

       @Override
       protected String getErrorMessage(int count) {
                 return count + " xrefs with non-standard UniProt names. To show, use: " + QUERY;
                 }

}
