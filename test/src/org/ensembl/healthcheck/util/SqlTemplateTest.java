package org.ensembl.healthcheck.util;

import static org.ensembl.healthcheck.util.CollectionUtils.createArrayList;
import static org.ensembl.healthcheck.util.CollectionUtils.createLinkedHashSet;
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
    String update = "insert into numbers values(?)";
    for(Integer i: new Integer[]{1,1,2,3,4,5,6,7,8,8}) {
      t.update(update, i);
    }
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
  
}
