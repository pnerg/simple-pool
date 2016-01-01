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
	synchronized void add(T item) {
		// queue was empty, now both head/tail are the same item
		if(first == null && last == null) {
			this.first = new PooledInstance<>(item, first);
			this.last = this.first;
			return;
		}
		
		addToQueue(item);
	}
	
	protected abstract void addToQueue(T item);
	
	synchronized Option<T> head() {
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

	private Option<PooledInstance<T>> takeFirst() {
		Option<PooledInstance<T>> o = Option(first);
		o.forEach(pi -> {
			first = pi.next();
		});
		return o;
	}
}
