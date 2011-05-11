package org.ensembl.healthcheck;

import java.util.Comparator;

/**
 * 
 * Compares GroupOfTests by their name. Useful when sorting groups.
 * 
 * @author michael
 *
 */
public class GroupOfTestsComparator implements Comparator<GroupOfTests> {
	public int compare(GroupOfTests arg0, GroupOfTests arg1) {				
		return arg0.getName().compareTo(arg1.getName());
	}
}
