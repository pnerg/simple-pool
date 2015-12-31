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

import javascalautils.Try;
import simplepool.Constants.PoolMode;

/**
 * Base test cases for the {@link PoolQueue}
 * @author Peter Nerg
 */
public abstract class AbstractPoolImplTest extends BaseAssert {

	final PoolImpl<String> pool;
	private final PoolMode poolMode;
	
	AbstractPoolImplTest(PoolMode poolMode) {
		this.poolMode = poolMode;
		pool = new PoolImpl<>(() -> "Peter", 2, v -> true, v -> {}, poolMode, Duration.ofDays(1));
	}
	
	@Test(timeout=5000)
	public void returnInstance_beyondCapacity() {
		assertIsSuccess(pool.returnInstance("one"));
		assertIsSuccess(pool.returnInstance("two"));
		assertIsFailure(pool.returnInstance("three"));
	}

	@Test(timeout=5000)
	public void getInstance_emptyQueue() throws Throwable {
		assertEquals("Peter", pool.getInstance().get());
	}
	
	@Test(timeout=5000)
	public void getInstance_Timeout() {
		assertIsSuccess(pool.getInstance());
		assertIsSuccess(pool.getInstance());
		assertIsFailure(pool.getInstance(Duration.ofMillis(5)));
	}
	
	@Test(timeout=5000)
	public void returnInstance_nullObject() {
		assertIsFailure(pool.returnInstance(null));
	}
	
	@Test(timeout=5000)
	public void getInstance_exhaustPool() {
		assertTrue(pool.getInstance().isSuccess());
		assertTrue(pool.getInstance().isSuccess());
		assertFalse(pool.getInstance(Duration.ofMillis(5)).isSuccess()); //should fail as pool size is only 2
	}

	@Test(timeout=5000)
	public void getInstance_failToCreateInstance() {
		PoolImpl<PoolableObject> pool = new PoolImpl<>(() -> {throw new Exception("Error, terror");}, 2, po -> true, po -> {}, PoolMode.FIFO, Duration.ofDays(1));
		Try<PoolableObject> instance = pool.getInstance();
		assertFalse(instance.isSuccess());
	}
	
	@Test(timeout=5000)
	public void returnInstance_objectFailsValidation() {
		PoolImpl<PoolableObject> p = new PoolImpl<>(() -> new PoolableObject(), 2, v -> v.isValid(), v -> v.destroy(), poolMode, Duration.ofDays(1));
		PoolableObject po = new PoolableObject(false);
		assertIsSuccess(p.returnInstance(po));
		assertTrue(po.isDestroyed());
	}
}
