/**
 * Unit
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import org.apache.commons.lang.NullArgumentException;

/**
 * @author dstaines
 *
 * @param <A>
 */
public class Unit<A> {
	
	public final A a;

	public Unit(A a) {
		if (a == null) {
			throw new NullArgumentException(this.getClass().getSimpleName()
					+ " cannot be instantiated with null arguments");
		}
		this.a = a;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (a == null) ? 0 : a.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit<A> other = (Unit<A>) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "a=[" + a + "]";
	}

}