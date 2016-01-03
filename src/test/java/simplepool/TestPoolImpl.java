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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Test;

import javascalautils.ThrowableFunction0;
import javascalautils.Try;
import javascalautils.Unit;
import javascalautils.concurrent.Future;
import simplepool.Constants.PoolMode;

/**
 * Base test cases for the {@link PoolQueue}
 * 
 * @author Peter Nerg
 */
public class TestPoolImpl extends BaseAssert {

	private static final long MaxTestTime = 5000;
	private final AtomicLong counter = new AtomicLong(1);
	private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
	private final PoolImpl<PoolableObject> pool = createPool(() -> new PoolableObject("" + counter.getAndIncrement()));

	@After
	public void after() throws TimeoutException, Throwable {
		pool.destroy();
		pool.destroy(); // invoking a second time shall make no difference
		scheduledExecutorService.shutdownNow();
	}

	@Test(timeout = MaxTestTime)
	public void assertIdleTimeout() throws Throwable {
		PoolImpl<PoolableObject> idlingPool = createPool(()-> new PoolableObject("xxx"), Duration.ofMillis(10));
		PoolableObject instance = idlingPool.getInstance().get();
		idlingPool.returnInstance(instance);
		
		//let the idle reaper sweep
		Thread.sleep(50);
		
		//should now be have been destroyed
		assertIsDestroyed(instance);
	}

	@Test(timeout = MaxTestTime)
	public void getInstance_emptyQueue() throws Throwable {
		assertEquals("1", getAndAssertInstance().value());
	}

	@Test(timeout = MaxTestTime)
	public void returnInstance_withoutTakingAnInstance() throws Throwable {
		assertIsFailure(pool.returnInstance(new PoolableObject("This should fail")));
	}

	@Test(timeout = MaxTestTime)
	public void getInstance_Timeout() {
		getAndAssertInstance();
		getAndAssertInstance();
		assertIsFailure(pool.getInstance(Duration.ofMillis(5)));
	}

	@Test(timeout = MaxTestTime)
	public void returnInstance_nullObject() {
		assertIsFailure(pool.returnInstance(null));
	}

	@Test(timeout = MaxTestTime)
	public void getInstance_exhaustPool() {
		getAndAssertInstance();
		getAndAssertInstance();
		assertIsFailure(pool.getInstance(Duration.ofMillis(5))); // should fail as pool size is only 2
	}

	@Test(timeout = MaxTestTime)
	public void getInstance_failToCreateInstance() {
		PoolImpl<PoolableObject> pool = createPool(() -> {
			throw new Exception("Error, terror!!!");
		});
		Try<PoolableObject> instance = pool.getInstance();
		assertIsFailure(instance);
	}

	@Test(timeout = MaxTestTime)
	public void returnInstance_ok() {
		// must first take an instance to be able to return one
		PoolableObject po = getAndAssertInstance();

		assertIsSuccess(pool.returnInstance(po));
		assertIsValid(po);
	}

	@Test(timeout = MaxTestTime)
	public void returnInstance_objectFailsValidation() {
		// must first take an instance to be able to return one
		PoolableObject po = getAndAssertInstance();
		po.failValidation();

		assertIsSuccess(pool.returnInstance(po));
		assertIsDestroyed(po);
	}

	@Test(expected = IllegalStateException.class, timeout = MaxTestTime)
	public void getInstance_afterDestruction() throws TimeoutException, Throwable {
		// destroy the pool and wait for it to be destroyed
		pool.destroy().result(1, TimeUnit.SECONDS);
		pool.getInstance(); // pool is destroyed and shall yield an exception
	}
	
	@Test(timeout = MaxTestTime)
	public void destroy_withBorrowedInstances() throws TimeoutException, Throwable {
		//take a few instances, both are expected to be ok
		PoolableObject po1 = getAndAssertInstance();
		PoolableObject po2 = getAndAssertInstance();
		
		returnAndAssertResponse(po1);
		
		Future<Unit> future = pool.destroy();
		assertFalse(future.isCompleted());
		
		//wait a while to simulate usage of the borrowed instance
		Thread.sleep(50);
		
		//now return the missing instance
		returnAndAssertResponse(po2);
		
		future.result(MaxTestTime, TimeUnit.MILLISECONDS);
		
		//both items should now have been destroyed
		assertIsDestroyed(po1);
		assertIsDestroyed(po2);
	}

	private PoolableObject getAndAssertInstance() {
		Try<PoolableObject> t = pool.getInstance();
		assertIsSuccess(t);
		//orNull will never happen as we've already confirmed it's a success
		//gets rid of exception handling
		return t.orNull(); 
	}
	
	private void returnAndAssertResponse(PoolableObject po) {
		assertIsSuccess(pool.returnInstance(po));
	}
	
	private PoolImpl<PoolableObject> createPool(ThrowableFunction0<PoolableObject> instanceFactory) {
		return createPool(instanceFactory, Duration.ofDays(1));
	}

	private PoolImpl<PoolableObject> createPool(ThrowableFunction0<PoolableObject> instanceFactory, Duration idleTimeout) {
		return new PoolImpl<>(instanceFactory, 2, po -> po.isValid(), po -> po.destroy(), PoolMode.FIFO, idleTimeout, Option(scheduledExecutorService));
	}
}
