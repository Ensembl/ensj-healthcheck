/**
 * Triple
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import org.apache.commons.lang.NullArgumentException;

/**
 * Generic class to associate three objects
 * @author dstaines
 *
 */
public class Triple<A,B,C> extends Pair<A,B> {

	public final C c;
	
	/**
	 * @param a
	 * @param b
	 */
	public Triple(A a, B b, C c) {
		super(a, b);
		if(c==null)  {
			throw new NullArgumentException(this.getClass().getSimpleName()
					+ " cannot be instantiated with null arguments");
		}
		this.c = c;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + (c==null?0:c.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !this.getClass().equals(obj.getClass())) {
			return false;
		}
		Triple<A, B, C> other = (Triple<A, B, C>) obj;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c)) {
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
		return super.toString()+",c=[" + c.toString() + "]";
	}
	
}
