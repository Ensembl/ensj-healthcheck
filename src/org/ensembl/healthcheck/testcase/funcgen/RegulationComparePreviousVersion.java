package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;



public abstract class RegulationComparePreviousVersion extends ComparePreviousVersionBase {

    public RegulationComparePreviousVersion() {
     
    }

    @Override
	public boolean run(DatabaseRegistryEntry dbre) {
		boolean result = true;

		if (System.getProperty("ignore.previous.checks") != null) {
			logger.finest("ignore.previous.checks is set in database.properties, skipping this test");
			return true;
		}

        boolean skipCondition = skipCondition(dbre);

        if (skipCondition) {
             logger.finest("Skipping test as data is not yet available");
             return true;
        }

		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

		if (sec == null) {
			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}

		logger.finest("Equivalent database on secondary server is " + sec.getName());

		Map currentCounts = getCounts(dbre);
		Map secondaryCounts = getCounts(sec);

		// compare each of the secondary (previous release, probably) with current
		Set externalDBs = secondaryCounts.keySet();
		Iterator it = externalDBs.iterator();
		String successText = "";

		// show % tolerance here?
		double tolerance = (100 - ((threshold() / 1) * 100));

		if (testUpperThreshold()) {
			successText = " - within tolerance +/-" + tolerance + "%";

		} else {
			successText = " - greater or within tolerance";
		}

		while (it.hasNext()) {

			String key = (String) it.next();

			int secondaryCount = ((Integer) (secondaryCounts.get(key))).intValue();

			if (secondaryCount == 0) {
				continue;
			}

			// check it exists at all
			if (currentCounts.containsKey(key)) {

				int currentCount = ((Integer) (currentCounts.get(key))).intValue();

				double percentage = ((((double) currentCount) / (double) secondaryCount) * 100) - 100;


				if ( percentage < 0 ) percentage *= -1;

				if ( compareReturnProblem() ) { 
					if ( percentage > 0 ) {
						if ( secondaryCount > currentCount ){
							ReportManager.problem(this, dbre.getConnection(), sec.getName() + " has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " only has " + currentCount + " (dif: " + String.format("%1.4f", percentage) + "%)" );
							result = false;
						}else {
							ReportManager.problem(this, dbre.getConnection(), sec.getName() + " only has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " has " + currentCount + " (dif: " + String.format("%1.4f", percentage) + "%)");
							result = false;
						}
					}
				}else{ 
					if ( secondaryCount > currentCount ){
						ReportManager.warning(this, dbre.getConnection(), sec.getName() + " has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " only has " + currentCount + " (dif: " + String.format("%1.4f", percentage) + "%)" );

					}else {
						ReportManager.warning(this, dbre.getConnection(), sec.getName() + " only has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " has " + currentCount + " (dif: " + String.format("%1.4f", percentage) + "%)");

					}
				}
			} else {
				ReportManager.problem(this, dbre.getConnection(), sec.getName() + " has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " has none");
				result = false;
			}
		}
		return result;

	}

	protected boolean compareReturnProblem(){
        return false;
    }
}