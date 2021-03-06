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

import java.time.Duration;

import javascalautils.Try;
import javascalautils.Unit;
import javascalautils.concurrent.Future;

/**
 * Represents a pool of objects.
 * 
 * @author Peter Nerg
 * @param <T>
 *            The type returned by the pool
 * @since 1.0
 */
public interface Pool<T> {

	/**
	 * Request a object instance from the pool. <br>
	 * If no free objects this method waits until an object is returned. <br>
	 * <b>Warning! This means that the thread may be held here forever.</b> <br>
	 * If the pool has been {@link #destroy() destroyed} an {@link IllegalStateException} is raised.
	 * @return The object instance
	 * @since 1.0
	 */
	default Try<T> getInstance() {
		return getInstance(Duration.ofMillis(Long.MAX_VALUE));
	}

	/**
	 * Request a object instance from the pool. <br>
	 * If no free objects this method waits (for the provided time) until an object is returned. <br>
	 * If the pool has been {@link #destroy() destroyed} an {@link IllegalStateException} is raised.
	 * @param maxWaitTime
	 *            The time to wait for a free object
	 * @return The object instance
	 * @since 1.0
	 */
	Try<T> getInstance(Duration maxWaitTime);

	/**
	 * Returns a borrowed instance to the pool. <br>
	 * Should the pool be full and an attempt is made to return an instance the operation will result in a {@link javascalautils.Failure}. <br>
	 * This operation is safe to use even if the pool has been {@link #destroy() destroyed}.
	 * @param instance
	 *            The instance to return
	 * @return The result of returning the instance
	 * @since 1.0
	 */
	Try<Unit> returnInstance(T instance);
	
	/**
	 * Permanently destroys the pool and all instances in it. <br>
	 * Attempts to use the pool after its destruction will yield an error. <br>
	 * Should any instances be borrowed the operation will wait for them to be returned and destroy them as well. <br>
	 * The operation will not be completed until all instances in the pool has been destroyed. <br>
	 * Invoking the operation multiple times will make no difference.
	 * @return The Future that will be completed once the pool is completely destroyed
	 * @since 1.1
	 */
	Future<Unit> destroy();
}
