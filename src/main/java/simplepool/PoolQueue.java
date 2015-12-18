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

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import javascalautils.Option;
import simplepool.Constants.PoolMode;

import static javascalautils.OptionCompanion.Option;

/**
 * @author Peter Nerg
 */
final class PoolQueue<T> {

	/** The actual queue implementation. */
	private final Deque<PooledInstance<T>> queue;

	/**
	 * Determines if the queue executes in FIFO (queue) or LIFO (stack) mode.
	 */
	private final PoolMode poolMode;

	PoolQueue(int poolSize, PoolMode poolMode) {
		this.poolMode = poolMode;
		queue = new LinkedBlockingDeque<>(poolSize);
	}

	/**
	 * Get the first free instance from the queue. <br>
	 * The instance is always drawn from the front of the queue.
	 * 
	 * @return The instance, or None if no valid instance was found
	 */
	Option<T> poll() {
		boolean foundInstance = false;
		PooledInstance<T> wrapper;
		// keep looping until either a non destroyed object is found or the end
		// of the queue is reached
		T instance = null;
		while (!foundInstance && (wrapper = queue.poll()) != null) {
			// attempt to mark the instance as used
			// if we fail to do so it means that the idle reaper has destroyed
			// it, thus we skip and take the next
			if (wrapper.markAsUsedOrDestroyed()) {
				instance = wrapper.instance();
				foundInstance = true;
			}
		}

		return Option(instance);
	}

	/**
	 * Get the first free instance from the queue. <br>
	 * The instance may be returned to either the front or end of the queue
	 * depending on if executing in FIFO or LIFO mode.
	 * 
	 * @return
	 */
	boolean offer(T instance) {
		PooledInstance<T> pooledInstance = new PooledInstance<>(instance);
		return poolMode == PoolMode.FIFO ? queue.offerLast(pooledInstance) : queue.offerFirst(pooledInstance);
	}

}
