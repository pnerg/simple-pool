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
 * Base class for the two variants of internal queue.
 * @author Peter Nerg
 * @since 1.1
 */
abstract class PoolQueue<T> {

	transient PooledInstance<T> first;
	transient PooledInstance<T> last;

	/**
	 * Adds an item to the queue
	 * @param item
	 */
	final synchronized void add(T item) {
		// if first is null then queue is empty
		// simply set both first/last to point to the new item
		if(first == null) {
			this.first = new PooledInstance<>(item, first);
			this.last = this.first;
			return;
		}
		
		addToQueue(item);
	}
	
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
	
	protected abstract void addToQueue(T item);
	
	private Option<PooledInstance<T>> takeFirst() {
		Option<PooledInstance<T>> o = Option(first);
		o.forEach(pi -> {
			first = pi.next();
		});
		return o;
	}
	
}
