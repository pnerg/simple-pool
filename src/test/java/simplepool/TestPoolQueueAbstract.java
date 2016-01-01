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

import org.junit.Test;

/**
 * Base test cases for the {@link PoolQueue}
 * @author Peter Nerg
 */
public abstract class TestPoolQueueAbstract extends BaseAssert {

	protected final PoolQueue<PoolableObject> queue;

	TestPoolQueueAbstract(PoolQueue<PoolableObject> queue) {
		this.queue = queue;
	}
	
	@Test
	public void add_emptyQueue() {
		add("one");
	}
	
	@Test
	public void head_singleItemQueue() {
		PoolableObject po = add("one");
		assertHead(po);
	}

	@Test
	public void head_emptyQueue() {
		assertHeadIsEmpty();
	}
	
	@Test
	public void markStaleInstances_emptyQueue() {
		queue.markStaleInstances(Duration.ZERO, s -> s.destroy());
	}

	@Test
	public void markStaleInstances_nonEmptyQueueAllValid() {
		PoolableObject one = add("one");
		queue.markStaleInstances(Duration.ofDays(1), s -> s.destroy());
		
		assertIsValid(one);
		
		//should still have the same instance in Queue
		assertHead(one);
		assertHeadIsEmpty();
	}

	@Test
	public void markStaleInstances_nonEmptyQueueFoundStale() throws InterruptedException {
		PoolableObject one = add("one");
		PoolableObject two = add("two");
		Thread.sleep(1); //sleep one to be sure we expire all instances
		
		//sweep with zero duration, all instances should be marked as stale
		queue.markStaleInstances(Duration.ZERO, s -> s.destroy());
		
		assertIsDestroyed(one);
		assertIsDestroyed(two);
		
		//queue should be empty
		assertHeadIsEmpty();
	}

	@Test
	public void markStaleInstances_withStaleInstancesInQueue() throws InterruptedException {
		PoolableObject one = add("one");
		PoolableObject two = add("two");
		Thread.sleep(1); //sleep one to be sure we expire all instances
		
		//sweep with zero duration, all instances should be marked as stale
		queue.markStaleInstances(Duration.ZERO, s -> s.destroy());
		
		assertIsDestroyed(one);
		assertIsDestroyed(two);
		
		//sweep again, should make no difference as instances are already marked as dead
		queue.markStaleInstances(Duration.ZERO, s -> s.destroy());
		
		//queue should be empty
		assertHeadIsEmpty();
	}
	
	/**
	 * Creates a new {@link PoolableObject} and adds it to the queue.
	 * @return
	 */
	PoolableObject add(String value) {
		PoolableObject po = new PoolableObject(value);
		queue.add(po);
		return po;
	}
	
	void assertHead(PoolableObject expected) {
		assertSomeEquals(expected, queue.head());
	}
	
	/**
	 * Asserts that the head of the queue is empty.
	 */
	void assertHeadIsEmpty() {
		assertIsNone(queue.head());
	}
	
	/**
	 * Asserts that that the provided object has been destroyed due to going stale.
	 * @param po
	 */
	void assertIsDestroyed(PoolableObject po) {
		assertTrue("Expected ["+po+"] to be destroyed", po.isDestroyed());
	}
	
	/**
	 * Assert that the provided object is still valid, i.e. not destroyed.
	 * @param po
	 */
	void assertIsValid(PoolableObject po) {
		assertTrue("Expected ["+po+"] to be valid", po.isValid());
	}
}
