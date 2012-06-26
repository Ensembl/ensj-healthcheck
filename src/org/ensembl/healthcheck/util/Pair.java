/**
 * Tuple
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import org.apache.commons.lang.NullArgumentException;

/**
 * Generic class to associate two objects
 * 
 * @author dstaines
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

}