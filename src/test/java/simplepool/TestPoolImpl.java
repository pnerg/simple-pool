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
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import javascalautils.ThrowableFunction0;
import javascalautils.Try;
import simplepool.Constants.PoolMode;
import static javascalautils.OptionCompanion.None;

/**
 * Base test cases for the {@link PoolQueue}
 * 
 * @author Peter Nerg
 */
public class TestPoolImpl extends BaseAssert {

	private final AtomicLong counter = new AtomicLong(1);
	private final PoolImpl<PoolableObject> pool = createPool(() -> new PoolableObject("" + counter.getAndIncrement()));

	@Test(timeout = 5000)
	public void getInstance_emptyQueue() throws Throwable {
		assertEquals("1", pool.getInstance().get().value());
	}

	@Test(timeout = 5000)
	public void returnInstance_withoutTakingAnInstance() throws Throwable {
		assertIsFailure(pool.returnInstance(new PoolableObject("This should fail")));
	}

	@Test(timeout = 5000)
	public void getInstance_Timeout() {
		assertIsSuccess(pool.getInstance());
		assertIsSuccess(pool.getInstance());
		assertIsFailure(pool.getInstance(Duration.ofMillis(5)));
	}

	@Test(timeout = 5000)
	public void returnInstance_nullObject() {
		assertIsFailure(pool.returnInstance(null));
	}

	@Test(timeout = 5000)
	public void getInstance_exhaustPool() {
		assertTrue(pool.getInstance().isSuccess());
		assertTrue(pool.getInstance().isSuccess());
		assertFalse(pool.getInstance(Duration.ofMillis(5)).isSuccess()); // should fail as pool size is only 2
	}

	@Test(timeout = 5000)
	public void getInstance_failToCreateInstance() {
		PoolImpl<PoolableObject> pool = createPool(() -> {
			throw new Exception("Error, terror!!!");
		});
		Try<PoolableObject> instance = pool.getInstance();
		assertIsFailure(instance);
	}

	@Test(timeout = 5000)
	public void returnInstance_ok() throws Throwable {
		// must first take an instance to be able to return one
		PoolableObject po = pool.getInstance().get();

		assertIsSuccess(pool.returnInstance(po));
		assertIsValid(po);
	}

	@Test(timeout = 5000)
	public void returnInstance_objectFailsValidation() throws Throwable {
		// must first take an instance to be able to return one
		PoolableObject po = pool.getInstance().get();
		po.failValidation();

		assertIsSuccess(pool.returnInstance(po));
		assertIsDestroyed(po);
	}

	private static PoolImpl<PoolableObject> createPool(ThrowableFunction0<PoolableObject> instanceFactory) {
		return new PoolImpl<>(instanceFactory, 2, po -> po.isValid(), po -> po.destroy(), PoolMode.FIFO, Duration.ofDays(1), None());
	}
}
