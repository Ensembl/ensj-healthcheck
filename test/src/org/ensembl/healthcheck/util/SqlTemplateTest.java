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

import static org.ensembl.healthcheck.util.CollectionUtils.createArrayList;
import static org.ensembl.healthcheck.util.CollectionUtils.createLinkedHashSet;
import static org.ensembl.healthcheck.util.CollectionUtils.pair;
import static org.ensembl.healthcheck.util.CollectionUtils.quadruple;
import static org.ensembl.healthcheck.util.CollectionUtils.triple;
import static org.ensembl.healthcheck.util.CollectionUtils.unit;
import static org.testng.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlTemplateTest {
  
  private Connection conn = null;
  private SqlTemplate t = null;
  
  private static final String SQL_INT = "select * from numbers order by a desc";
  private static final Integer[] TABLE_ROWS = new Integer[]{1,1,2,3,4,5,6,7,8,8};
  
  @BeforeClass
  void setupDb() throws SQLException {
    conn = ConnectionPool.getConnection("org.h2.Driver", "jdbc:h2:mem:sqltemplatetest", "sa", "");
    t = new ConnectionBasedSqlTemplateImpl(conn);
    
    t.execute("create table numbers(a int)");
    for(Integer i: new Integer[]{1,1,2,3,4,5,6,7,8,8}) {
      String update = "insert into numbers values(?)";
      t.update(update, i);
    }
    
    t.execute("create table tupletastic(a int, b varchar, c float, d boolean)");
    t.update("insert into tupletastic values(?,?,?,?)", 1, "hello", 4.0D, true);
  }

  @Test
  public void setCreationTest() {
    //Assert ints
    Set<Integer> ints = t.queryForDefaultObjectSet(SQL_INT, Integer.class);
    assertEquals(ints.getClass(), LinkedHashSet.class, "We should always get a linked hashset back");
    assertEquals(ints.size(), 8, "Asserting set length");
    assertEquals(ints, createLinkedHashSet(new Integer[]{8,7,6,5,4,3,2,1}), "Making sure we retain order");
    
    //Assert doubles
    Set<Double> doubles = t.queryForSet(SQL_INT, new RowMapper<Double>() {
      @Override
      public Double mapRow(ResultSet resultSet, int position)
          throws SQLException {
        return resultSet.getDouble(1);
      }
    });
    assertEquals(doubles, createLinkedHashSet(new Double[]{8.0D,7.0D,6.0D,5.0D,4.0D,3.0D,2.0D,1.0D}), "Making sure we retain order and they are doubles");
  }
  
  @Test
  public void listCreationTest() {
    List<Integer> ints = t.queryForDefaultObjectList(SQL_INT, Integer.class);
    assertEquals(ints.getClass(), ArrayList.class, "We should always get an array list back");
    assertEquals(ints.size(), TABLE_ROWS.length, "Asserting length");
    List<Integer> expected = createArrayList(TABLE_ROWS);
    Collections.reverse(expected);
    assertEquals(ints, expected, "Making sure we retain order");
  }
  
  @Test
  public void tupleMethods() {
    String sql = "select a,b,c,d from tupletastic";
    Class<Integer> a = Integer.class;
    Class<String> b = String.class;
    Class<Double> c = Double.class;
    Class<Boolean> d = Boolean.class;
    assertEquals(unit(1), t.queryForObject(sql, TupleRowMappers.unit(a)), "Checking unit ok");
    assertEquals(pair(1, "hello"), t.queryForObject(sql, TupleRowMappers.pair(a,b)), "Checking pair ok");
    assertEquals(triple(1, "hello", 4.0D), t.queryForObject(sql, TupleRowMappers.triple(a,b,c)), "Checking triple ok");
    assertEquals(quadruple(1, "hello", 4.0D, true), t.queryForObject(sql, TupleRowMappers.quadruple(a, b, c, d)), "Checking quadruple ok");
  }
}
