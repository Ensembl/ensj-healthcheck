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

package org.ensembl.healthcheck.util;
import static org.testng.Assert.*;

import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.Pair;
import org.ensembl.healthcheck.util.Quadruple;
import org.ensembl.healthcheck.util.Triple;
import org.ensembl.healthcheck.util.Unit;
import org.testng.annotations.Test;

/**
 * UnitTest
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */

/**
 * @author dstaines
 *
 */
public class UnitTest {

	@Test
	public void testUnit() {
		String s = "hello";
		String t = "goodbye";
		Unit<String> u1 = new Unit<String>(s);
		Unit<String> u2 = new Unit<String>(s);
		Unit<String> u3 = new Unit<String>(t);
		assertTrue(u1.equals(u2));
		assertTrue(u2.equals(u1));
		assertFalse(u1.equals(u3));
		assertEquals(u1.hashCode(),u2.hashCode());
		assertEquals(u1.toString(),u2.toString());
		assertFalse(u1.toString().equals(u3.toString()));
	}

	@Test
	public void testPair() {
		String s = "hello";
		int n = 69;
		int m = 96;
		Pair<String,Integer> p1 = CollectionUtils.pair(s, n);
		Pair<String,Integer> p2 = CollectionUtils.pair(s, n);
		Pair<String,Integer> p3 = CollectionUtils.pair(s, m);
		assertTrue(p1.equals(p2));
		assertTrue(p2.equals(p1));
		assertFalse(p1.equals(p3));
		assertEquals(p1.hashCode(),p2.hashCode());
		assertFalse(p1.hashCode()==p3.hashCode());
		assertEquals(p1.toString(),p2.toString());
		assertFalse(p1.toString().equals(p3.toString()));
	}

	@Test
	public void testTriple() {
		String s = "hello";
		int n = 69;
		String t = "banana";
		String u = "mango";
		Triple<String,Integer,String> t1 = CollectionUtils.triple(s, n, t);
		Triple<String,Integer,String> t2 = CollectionUtils.triple(s, n, t);
		Triple<String,Integer,String> t3 = CollectionUtils.triple(s, n, u);
		Triple<String,Integer,String> t4 = CollectionUtils.triple(t, n, s);
		assertTrue(t1.equals(t2));
		assertTrue(t2.equals(t1));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(t4));
		assertEquals(t1.hashCode(),t2.hashCode());
		assertFalse(t1.hashCode()==t3.hashCode());
		assertTrue(t1.hashCode()==t4.hashCode());
		assertEquals(t1.toString(),t2.toString());
		assertFalse(t1.toString().equals(t3.toString()));
		assertFalse(t1.toString().equals(t4.toString()));
	}

	@Test
	public void testQuadruple() {
		String s = "hello";
		int n = 69;
		String t = "banana";
		String u = "mango";
		int m = 96;
		Quadruple<String,Integer,String,Integer> q1 = CollectionUtils.quadruple(s, n, t, m);
		Quadruple<String,Integer,String,Integer> q2 = CollectionUtils.quadruple(s, n, t, m);
		Quadruple<String,Integer,String,Integer> q3 = CollectionUtils.quadruple(s, n, u, m);
		Quadruple<String,Integer,String,Integer> q4 = CollectionUtils.quadruple(t, n, s, m);
		assertTrue(q1.equals(q2));
		assertTrue(q2.equals(q1));
		assertFalse(q1.equals(q3));
		assertFalse(q1.equals(q4));
		assertEquals(q1.hashCode(),q2.hashCode());
		assertFalse(q1.hashCode()==q3.hashCode());
		assertTrue(q1.hashCode()==q4.hashCode());
		assertEquals(q1.toString(),q2.toString());
		assertFalse(q1.toString().equals(q3.toString()));
		assertFalse(q1.toString().equals(q4.toString()));
	}

}
