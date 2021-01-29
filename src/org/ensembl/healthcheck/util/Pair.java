/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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

import org.apache.commons.lang.NullArgumentException;

/**
 * Generic class to associate two objects
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 * 
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> extends Unit<A> {

	public final B b;

	public Pair(A a, B b) {
		super(a);
		if (b == null) {
			throw new NullArgumentException(this.getClass().getSimpleName()
					+ " cannot be instantiated with null arguments");
		}
		this.b = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + (b==null?0:b.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
  @Override
	public boolean equals(Object obj) {
		if (obj == null || !this.getClass().equals(obj.getClass())) {
			return false;
		}
		Pair<A, B> other = (Pair<A, B>) obj;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b)) {
			return false;
		}
		return super.equals(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString()+",b=[" + String.valueOf(b) + "]";
	}


	/**
	 *  Return new pair issued from parameters values
	 *
	 */
	public static <A,B> Pair<A,B> of(A a, B b) {
		return new Pair<>(a, b);
	}

	public A first() { return a; }
	public B second() { return b; }

}
