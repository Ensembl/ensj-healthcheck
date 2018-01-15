/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A collection row mappers used to work with the Tuple classes now available
 * in the healthcheck code
 * 
 * @author ayates
 */
public class TupleRowMappers {
  
  /**
   * Convenience method mapping to the default constructor (used to 
   * get around Generic declaration clutter)
   */
  public static <A> UnitRowMapper<A> unit(Class<A> a) {
    return new UnitRowMapper<A>(a);
  }
  
  /**
   * Convenience method mapping to the default constructor (used to 
   * get around Generic declaration clutter)
   */
  public static <A,B> PairRowMapper<A,B> pair(Class<A> a, Class<B> b) {
    return new PairRowMapper<A,B>(a,b);
  }
  
  /**
   * Convenience method mapping to the default constructor (used to 
   * get around Generic declaration clutter)
   */
  public static <A,B,C> TripleRowMapper<A,B,C> triple(Class<A> a, Class<B> b, Class<C> c) {
    return new TripleRowMapper<A,B,C>(a,b,c);
  }
  
  /**
   * Convenience method mapping to the default constructor (used to 
   * get around Generic declaration clutter)
   */
  public static <A,B,C,D> QuadrupleRowMapper<A,B,C,D> quadruple(Class<A> a, Class<B> b, Class<C> c, Class<D> d) {
    return new QuadrupleRowMapper<A,B,C,D>(a,b,c,d);
  }

  /**
   * Provides a mapper for bringing back a {@link Unit}.
   * 
   * @param <A> Type of the first element
   */
  public static class UnitRowMapper<A> implements RowMapper<Unit<A>> {
    
    private final DefaultObjectRowMapper<A> first;
    
    public UnitRowMapper(Class<A> firstClass) {
      this.first = new DefaultObjectRowMapper<A>(firstClass);
    }
    
    @Override
    public Unit<A> mapRow(ResultSet resultSet, int position)
        throws SQLException {
      return new Unit<A>(first.mapRow(resultSet, position));
    }

  }
  
  /**
   * Provides a mapper for bringing back a {@link Pair}.
   * 
   * @param <A> Type of the first element
   * @param <B> Type of the second element
   */
  public static class PairRowMapper<A,B> implements RowMapper<Pair<A,B>> {
    
    private final DefaultObjectRowMapper<A> first;
    private final DefaultObjectRowMapper<B> second;
    
    public PairRowMapper(Class<A> firstClass, Class<B> secondClass) {
      this.first = new DefaultObjectRowMapper<A>(firstClass,1);
      this.second = new DefaultObjectRowMapper<B>(secondClass,2);
    }
    
    @Override
    public Pair<A,B> mapRow(ResultSet resultSet, int position)
        throws SQLException {
      return new Pair<A,B>(
          first.mapRow(resultSet, position), 
          second.mapRow(resultSet, position));
    }

  }
  
  /**
   * Provides a mapper for bringing back a {@link Triple}.
   * 
   * @param <A> Type of the first element
   * @param <B> Type of the second element
   * @param <C> Type of the third element
   */
  public static class TripleRowMapper<A,B,C> implements RowMapper<Triple<A,B,C>> {
    
    private final DefaultObjectRowMapper<A> first;
    private final DefaultObjectRowMapper<B> second;
    private final DefaultObjectRowMapper<C> third;
    
    public TripleRowMapper(Class<A> firstClass, Class<B> secondClass, Class<C> thirdClass) {
      first = new DefaultObjectRowMapper<A>(firstClass,1);
      second = new DefaultObjectRowMapper<B>(secondClass,2);
      third = new DefaultObjectRowMapper<C>(thirdClass,3);
    }
    
    @Override
    public Triple<A,B,C> mapRow(ResultSet resultSet, int position)
        throws SQLException {
      return new Triple<A,B,C>(
          first.mapRow(resultSet, position), 
          second.mapRow(resultSet, position),
          third.mapRow(resultSet, position));
    }

  }
  
  /**
   * Provides a mapper for bringing back a {@link Quadruple}.
   * 
   * @param <A> Type of the first element
   * @param <B> Type of the second element
   * @param <C> Type of the third element
   * @param <D> Type of the fourth element
   */
  public static class QuadrupleRowMapper<A,B,C,D> implements RowMapper<Quadruple<A,B,C,D>> {
    
    private final DefaultObjectRowMapper<A> first;
    private final DefaultObjectRowMapper<B> second;
    private final DefaultObjectRowMapper<C> third;
    private final DefaultObjectRowMapper<D> fourth;
    
    public QuadrupleRowMapper(Class<A> firstClass, Class<B> secondClass, Class<C> thirdClass, Class<D> fourthClass) {
      first = new DefaultObjectRowMapper<A>(firstClass,1);
      second = new DefaultObjectRowMapper<B>(secondClass,2);
      third = new DefaultObjectRowMapper<C>(thirdClass,3);
      fourth = new DefaultObjectRowMapper<D>(fourthClass,4);
    }
    
    @Override
    public Quadruple<A,B,C,D> mapRow(ResultSet resultSet, int position)
        throws SQLException {
      return new Quadruple<A,B,C,D>(
          first.mapRow(resultSet, position), 
          second.mapRow(resultSet, position),
          third.mapRow(resultSet, position),
          fourth.mapRow(resultSet, position));
    }

  }
  
}
