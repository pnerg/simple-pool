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

import simplepool.Constants.PoolMode;

/**
 * Test the class {@link PoolQueue}
 * @author Peter Nerg
 */
public class TestPoolQueue extends BaseAssert {
	
	@Test
	public void testPollWithFifo() {
		PoolQueue<String> poolQueue = new PoolQueue<>(2, PoolMode.FIFO);
		poolQueue.offer(new PooledInstance<>("First"));
		poolQueue.offer(new PooledInstance<>("Second"));
		
		assertEquals("First", poolQueue.poll().get());
		assertEquals("Second", poolQueue.poll().get());
		assertFalse(poolQueue.poll().isDefined());
	}

	@Test
	public void testPollWithLifo() {
		PoolQueue<String> poolQueue = new PoolQueue<>(2, PoolMode.LIFO);
		poolQueue.offer(new PooledInstance<>("First"));
		poolQueue.offer(new PooledInstance<>("Second"));
		
		assertEquals("Second", poolQueue.poll().get());
		assertEquals("First", poolQueue.poll().get());
		assertFalse(poolQueue.poll().isDefined());
	}
	
}
