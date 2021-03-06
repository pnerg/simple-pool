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

import static javascalautils.OptionCompanion.Option;

import java.time.Duration;
import java.util.function.Consumer;

import javascalautils.Option;

/**
 * Base class for the two variants of internal queue. <br>
 * The queue is a simplistic one-way linked list where we only really care of the first and last item in the Queue. <br>
 * Each item ({@link PooledInstance}) has a pointer to the next in the queue. <br>
 * The queue keeps track on the first and last instance making it very efficient to add instances to either start/end of the queue.
 * @author Peter Nerg
 * @since 1.1
 */
abstract class PoolQueue<T> {

	protected transient PooledInstance<T> first;
	protected transient PooledInstance<T> last;

	/**
	 * Adds an item to the queue
	 * @param item
	 */
	final synchronized void add(T item) {
		// if first is null then queue is empty
		// simply set both first/last to point to the new item
		if(first == null) {
			this.first = new PooledInstance<>(item, null);
			this.last = this.first;
			return;
		}
		
		addToQueue(item);
	}
	
	/**
	 * Takes the first valid (non-stale) item from the queue. <br>
	 * The item is also removed from the queue. <br>
	 * Should there be any invalid (stale) items at the front these are removed one by one until either a valid item is found or the end of the queue is met.
	 * @return The item, None if no valid item was found
	 */
	final synchronized Option<T> head() {
		Option<PooledInstance<T>> head;
		// first take the head of the queue and validate it's defined, i.e. exists
		// then attempt to mark the instance as used
		// if we fail to do so it means that the idle reaper has
		// destroyed it, thus we skip and take the next
		// keep looping until either a valid object is found or the end of the queue is reached
		do {
			head = takeFirst();
		} while (head.isDefined() && !head.map(pi -> pi.markAsUsedOrDestroyed()).getOrElse(() -> false));

		return head.map(pi -> pi.instance());
	}

	/**
	 * Finds and marks all stale instances as destroyed. <br>
	 * A stale instance is an item that has been sitting in the pool for longer than the provided max idle time. <br>
	 * Items are not expunged from the pool, only marked with {@link PooledInstance#markAsUsedOrDestroyed() markAsUsedOrDestroyed}. <br>
	 * This way the object is anyways dropped when we pick items using {@link #head()}. <br>
	 * Since we don't touch the structure of the queue we don't need to synchronize this operation. <br>
	 * Thread safety is guaranteed by the {@link PooledInstance#markAsUsedOrDestroyed() markAsUsedOrDestroyed} operation.
	 * @param maxIdleTime The maximum idle time
	 * @param destructor The function used to destroy the instance
	 */
	final void markStaleInstances(Duration maxIdleTime, Consumer<T> destructor) {
		long deadLine = System.currentTimeMillis()-maxIdleTime.toMillis();
		PooledInstance<T> head = first;
		while(head != null) {
			if(head.lastUsed() < deadLine && head.markAsUsedOrDestroyed()) {
				destructor.accept(head.instance());
			}
			head = head.next();
		}
	}
	
	/**
	 * Adds the provided item to the queue. <br>
	 * Where it's placed (first/last) depends on the queue implementation
	 * @param item The item to add
	 */
	protected abstract void addToQueue(T item);
	
	/**
	 * Takes/removes the first item in the queue. <br>
	 * The pointer {@link #first} is set to be the next in line.
	 * @return The first item, None if Queue was empty
	 */
	private Option<PooledInstance<T>> takeFirst() {
		Option<PooledInstance<T>> o = Option(first);
		//set the "first" pointer to be next() in line
		o.forEach(pi -> {
			first = pi.next();
		});
		return o;
	}
	
}
