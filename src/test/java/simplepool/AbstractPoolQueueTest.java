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

import simplepool.Constants.PoolMode;

/**
 * Base test cases for the {@link PoolQueue}
 * @author Peter Nerg
 */
public abstract class AbstractPoolQueueTest extends BaseAssert {

	final PoolImpl<String> pool;
	private final PoolMode poolMode;
	
	AbstractPoolQueueTest(PoolMode poolMode) {
		this.poolMode = poolMode;
		pool = new PoolImpl<>(() -> "Peter", 2, v -> true, v -> {}, poolMode);
	}
	
	@Test
	public void returnInstance_beyondCapacity() {
		assertTrue(pool.returnInstance("one").isSuccess());
		assertTrue(pool.returnInstance("two").isSuccess());
		assertFalse(pool.returnInstance("three").isSuccess());
	}

	@Test
	public void getInstance_emptyQueue() throws Throwable {
		assertEquals("Peter", pool.getInstance().get());
	}
	
	@Test
	public void getInstance_Timeout() {
		assertTrue(pool.getInstance().isSuccess());
		assertTrue(pool.getInstance().isSuccess());
		assertFalse(pool.getInstance(Duration.ofMillis(5)).isSuccess());
	}
	
	@Test
	public void returnInstance_objectFailsValidation() {
		PoolImpl<PoolableObject> p = new PoolImpl<>(() -> new PoolableObject(), 2, v -> v.isValid(), v -> v.destroy(), poolMode);
		PoolableObject po = new PoolableObject(false);
		assertTrue(p.returnInstance(po).isSuccess());
		assertTrue(po.isDestroyed());
	}
}
