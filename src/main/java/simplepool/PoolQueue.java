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
 * @author Peter Nerg
 * @since 1.1
 */
final class PoolQueue<T> {

	private transient PooledInstance<T> first;
	private transient PooledInstance<T> last;
	private transient int size = 0;

	/**
	 * Adds an item to the head/first of the queue.
	 * 
	 * @param item
	 */
	void addFirst(T item) {
		this.first = new PooledInstance<>(item, first);

		// queue was empty, now both head/tail are the same item
		if (last == null) {
			last = first;
		}
	}

	void addLast(T item) {
		PooledInstance<T> pi = new PooledInstance<>(item);
		// non-null last, set it to point to the "new" last
		if (last != null) {
			last.next(pi);
		}
		last = pi;

		// queue was empty, now both head/tail are the same item
		if (first == null) {
			first = last;
		}
	}

	Option<T> head() {
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
			size--;
		});
		return o;
	}
}
