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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javascalautils.Try;

/**
 * Represents a pool of objects.
 * 
 * @author Peter Nerg
 * @param <T>
 *            The type returned by the pool
 */
public interface Pool<T> {

	/**
	 * Request a object instance from the pool. <br>
	 * If no free objects this method waits until an object is returned. <br>
	 * <b>Warning! This means that the thread may be held here forever.<b>
	 * 
	 * @return The object instance
	 */
	default Try<T> getInstance() {
		return getInstance(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	/**
	 * Request a object instance from the pool. <br>
	 * If no free objects this method waits (for the provided time) until an
	 * object is returned.
	 * 
	 * @param waitTime
	 *            The time to wait for a free object
	 * @param timeUnit
	 *            The unit of the waitTime
	 * @return The object instance
	 */
	Try<T> getInstance(long waitTime, TimeUnit timeUnit);

}
