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
 * A Last-In-First-Out variant of the Queue.
 * @author Peter Nerg
 * @since 1.1
 */
final class PoolQueueLIFO<T> extends PoolQueue<T>{
	/**
	 * Adds an item to the head/start of the queue.
	 * 
	 * @param item The item to add
	 */
	protected void addToQueue(T item) {
		this.first = new PooledInstance<>(item, first);
	}
}
