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

package org.ensembl.healthcheck.util;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A set of useful methods you might want to use when creating collections and
 * working with Collections. The first set of methods allow you to escape from
 * generics hell i.e.
 * 
 * <code>
 * <pre>
 * Map&lt;String,List&lt;Map&lt;String,String&gt;&gt;&gt; myMap =
 *   new HashMap&lt;String,List&lt;Map&lt;String,String&gt;&gt;&gt;();
 *  / / We can write this as:
 * Map&lt;String,List&lt;Map&lt;String,String&gt;&gt;&gt; myMap = CollectionUtils.createHashMap();
 * </pre>
 * </code>
 * 
 * Decide for yourself if this is easier or not (and remember that in Java5 you
 * can do static imports so this truncates down to a call to the createHashMap
 * method).
 * 
 * @author ayates
 */
public class CollectionUtils {
  
  public static <A> Unit<A> unit(A a) {
    return new Unit<A>(a);
  }

	/**
	 * Create a new {@link Pair} of two supplied objects
	 * 
	 * @param a
	 * @param b
	 * @return new pair
	 */
	public static <A, B> Pair<A, B> pair(A a, B b) {
		return new Pair<A, B>(a, b);
	}


	/**
	 * Create a new {@link Triple} of three supplied objects
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return new triple
	 */
	public static <A, B, C> Triple<A, B, C> triple(A a, B b, C c) {
		return new Triple<A, B, C>(a, b, c);
	}
	
	/**
	 * Create a new {@link Quadruple} of four supplied objects
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return new quadruple
	 */
	public static <A, B, C, D> Quadruple<A, B, C, D> quadruple(A a, B b, C c, D d) {
		return new Quadruple<A, B, C, D>(a, b, c, d);
	}
	
	/**
	 * Returns a hash map typed to the generics specified in the method call
	 */
	public static <K, V> Map<K, V> createHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * Returns a hash map typed to the generics specified in the method call
	 * with the given initial capacity
	 */
	public static <K, V> Map<K, V> createHashMap(int initialCapacity) {
		return new HashMap<K, V>(initialCapacity);
	}

	/**
	 * Returns a typed array list
	 */
	public static <T> List<T> createArrayList() {
		return new ArrayList<T>();
	}

	/**
	 * Returns a typed array list with the given initial capacity
	 */
	public static <T> List<T> createArrayList(int initialCapacity) {
		return new ArrayList<T>(initialCapacity);
	}

	/**
	 * Creates a list and populates it with the contents of args
	 * 
	 * @param <T>
	 *            Generic type of list
	 * @param args
	 *            Elements to go into the list
	 * @return List of args typed accordingly
	 */
	public static <T> List<T> createArrayList(T... args) {
		List<T> list = createArrayList();
		list.addAll(asList(args));
		return list;
	}

	/**
	 * Returns a linked hash map typed to the generics specified in the method
	 * call
	 */
	public static <K, V> Map<K, V> createLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}

	/**
	 * Returns a linked hash map typed to the generics specified in the method
	 * call with the given initial capacity
	 */
	public static <K, V> Map<K, V> createLinkedHashMap(int initialCapacity) {
		return new LinkedHashMap<K, V>(initialCapacity);
	}

	/**
	 * Returns a hash set typed to the generics specified in the method call
	 */
	public static <T> Set<T> createHashSet() {
		return new HashSet<T>();
	}

	/**
	 * Returns a hash set typed to the generics specified in the method call
	 * with the given initial capacity
	 */
	public static <T> Set<T> createHashSet(int initialCapacity) {
		return new HashSet<T>(initialCapacity);
	}

	/**
	 * Returns a linked hash set typed to the generics specified in the method
	 * call
	 */
	public static <T> Set<T> createLinkedHashSet() {
		return new LinkedHashSet<T>();
	}

	/**
	 * Returns a linked hash set typed to the generics specified in the method
	 * call with the given initial capacity
	 */
	public static <T> Set<T> createLinkedHashSet(int initialCapacity) {
		return new LinkedHashSet<T>(initialCapacity);
	}

	/**
	 * Returns a linked hash set typed to the generics specified in the method
	 * call with the given elements as the contents
	 */
	public static <T> Set<T> createLinkedHashSet(T... objects) {
		List<T> l = Arrays.asList(objects);
		return new LinkedHashSet<T>(l);
	}

	/**
	 * Method which will return the "last" element in the given collection or a
	 * null value if not found.
	 * 
	 * @param <T>
	 *            generic collection type
	 * @param collection
	 *            collection to be checked
	 * @param defaultValue
	 *            default value if list is empty
	 * @return last element or default value
	 */
	public static <T> T getLastElement(Collection<T> collection, T defaultValue) {
		T elem = defaultValue;
		if (collection != null && !collection.isEmpty()) {
			if (List.class.isAssignableFrom(collection.getClass())) {
				elem = ((List<T>) collection).get(collection.size() - 1);
			} else {
				for (T item : collection) {
					elem = item;
				}
			}
		}
		return elem;
	}

	public static <T> T getFirstElement(Collection<T> collection, T defaultValue) {
		T elem = defaultValue;
		if (collection != null && !collection.isEmpty()) {
			if (List.class.isAssignableFrom(collection.getClass())) {
				elem = ((List<T>) collection).get(0);
			} else {
				for (T item : collection) {
					elem = item;
					break;
				}
			}
		}
		return elem;
	}

}
