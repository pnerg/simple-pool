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

import org.junit.Test;

/**
 * Base test cases for the {@link PoolQueue}
 * @author Peter Nerg
 */
public abstract class AbstractPoolQueueTest extends BaseAssert {

	
	protected final PoolQueue<String> queue;

	AbstractPoolQueueTest(PoolQueue<String> queue) {
		this.queue = queue;
	}
	
	@Test
	public void add_emptyQueue() {
		queue.add("one");
	}
	
	@Test
	public void head_singleItemQueue() {
		add_emptyQueue();
		assertHead("one");
	}

	@Test
	public void head_emptyQueue() {
		assertHeadIsEmpty();
	}
	
	void assertHead(String expected) {
		assertSomeEquals(expected, queue.head());
	}
	
	void assertHeadIsEmpty() {
		assertIsNone(queue.head());
	}
	
}
