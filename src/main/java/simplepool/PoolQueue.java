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
final class PoolQueue<V> {

	/** The actual queue implementation. */
	private final Deque<PooledInstance<V>> queue;

	/**
	 * Determines if the queue executes in FIFO (queue) or LIFO (stack) mode.
	 */
	private final PoolMode poolMode;

	PoolQueue(int poolSize, PoolMode poolMode) {
		this.poolMode = poolMode;
		queue = new LinkedBlockingDeque<>(poolSize);
	}

//	int size() {
//		return queue.size();
//	}
//
//	/**
//	 * Clears the queue.
//	 */
//	void clear() {
//		queue.clear();
//	}
//
	/**
	 * Get the first free instance from the queue. <br>
	 * The instance is always drawn from the front of the queue.
	 * 
	 * @return
	 */
	Option<PooledInstance<V>> poll() {
		return Option(queue.poll());
	}

	/**
	 * Get the first free instance from the queue. <br>
	 * The instance may be returned to either the front or end of the queue
	 * depending on if executing in FIFO or LIFO mode.
	 * 
	 * @return
	 */
	boolean offer(PooledInstance<V> instance) {
		return poolMode == PoolMode.FIFO ? queue.offerLast(instance) : queue.offerFirst(instance);
	}

//	boolean remove(PooledInstance<V> instance) {
//		return queue.remove(instance);
//	}

}
