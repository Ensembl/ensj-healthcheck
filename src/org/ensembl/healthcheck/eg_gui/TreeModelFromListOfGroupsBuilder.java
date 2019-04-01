/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.eg_gui;

import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.GroupOfTestsComparator;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import java.lang.reflect.Modifier;

/**
 * 
 * <p>
 * 	Provides static methods to build a TreeModel for a JTree from a group of 
 * tests or a List<GroupOfTests>. 
 * </p>
 * 
 * @author michael
 *
 */
public class TreeModelFromListOfGroupsBuilder {

	/**
	 * <p>
	 * 	Turns a List<GroupOfTests> like the list of all GroupOfTests found on 
	 * the classpath into a tree structure and returns the root node. 
	 * </p>
	 * 
	 * <p>
	 * 	The root node can be passed to the constructor of a JTree:
	 * </p>
	 * 
	 * <code>
	 *   JTree tree = new JTree(
	 *     GroupsOfTestsToTreeModelBuilder.GroupOfTestsToTreeModel(testGroupList)
	 *   );
	 * </code>
	 * 
	 * @param testGroupList
	 * @return instance of node
	 */
	public static MutableTreeNode GroupOfTestsToTreeModel(List<GroupOfTests> testGroupList) {

		MutableTreeNode root = new DefaultMutableTreeNode(Constants.TREE_ROOT_NODE_NAME);
		
		Collections.sort(testGroupList, new GroupOfTestsComparator());
		
		int index = 0;
		for (GroupOfTests currentGroup : testGroupList) {
			
			root.insert(GroupOfTestsToTreeModel(currentGroup), index);
			index++;
		}
		return root;
	}
	
	/**
	 * <p>
	 * 	Creates tree node with the same structure as the group of tests
	 * given as the parameter.
	 * </p>
	 * 
	 * @param g
	 * @return instance of node
	 * 
	 */
	public static MutableTreeNode GroupOfTestsToTreeModel(GroupOfTests g) {
		
		MutableTreeNode root = new GroupNode(g);
		
		// A group of tests to keep track of which tests are present in the 
		// groups of which the current group comprises. Tests will not be
		// listed separately again, if they were already added in a group.
		//
		GroupOfTests testsWhichAreInGroups = new GroupOfTests(); 
		
		int index = 0;
		
		for (GroupOfTests currentGroupOfTests : g.getSourceGroups()) {
			
			root.insert(
				GroupOfTestsToTreeModel(currentGroupOfTests),
				index
			);			
			testsWhichAreInGroups.addTest(currentGroupOfTests);			
			index++;
		}
		
		for (Class<? extends EnsTestCase> currentTest : g.getListOfTests()) {

			if (
				// Only list tests that are not already in one of the testgroups.
				//
				!testsWhichAreInGroups.hasTest(currentTest)
				//
				//
				&& !Modifier.isAbstract(currentTest.getModifiers())
			) {
				DefaultMutableTreeNode newNode = new TestNode(
					currentTest.getSimpleName(),
					currentTest.getCanonicalName()
				);
				
				root.insert(newNode, index);
				
				index++;
			}
		}		
		return root;
	}
}
