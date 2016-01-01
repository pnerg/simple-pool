/**
 *  Copyright 2015 Peter Nerg
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package simplepool;

/**
 * Dummy object used to test the pool
 * @author Peter Nerg
 */
final class PoolableObject {

	private final String value;
	private boolean isDestroyed = false;
	private boolean isValid = true;
	
	PoolableObject(String value) {
		this.value = value;
	}
	
	/**
	 * Will make this instance to fail validation
	 */
	void failValidation() {
		this.isValid = false;
	}
	
	boolean isValid() {
		return isValid && !isDestroyed;
	}
	
	void destroy() {
		if(isDestroyed) {
			throw new IllegalStateException("Instance is already destroyed");
		}
		isDestroyed = true;
	}
	
	boolean isDestroyed() {
		return isDestroyed;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
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
		PoolableObject other = (PoolableObject) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	String value() {
		return value;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+":"+value;
	}
}
