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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javascalautils.Failure;
import javascalautils.Option;
import javascalautils.ThrowableFunction0;
import javascalautils.Try;
import simplepool.Constants.PoolMode;
import static javascalautils.TryCompanion.Try;

/**
 * @author Peter Nerg
 */
final class PoolImpl<T> implements Pool<T> {
	private static final Logger logger = LoggerFactory.getLogger(PoolImpl.class);
	private final ThrowableFunction0<T> instanceFactory;
	private final int maxSize;
	private final Option<Predicate<T>> validator;

	// /** Used to schedule and execute this idle object reaper. */
	// private final ScheduledExecutorService scheduledExecutorService =
	// Executors.newScheduledThreadPool(1, new
	// NamedSequenceThreadFactory("ObjectPool-Reaper"));

	/**
	 * Keeps references to object instances. <br>
	 * The number of objects in the queue/pool can range between zero and max
	 * instances. <br>
	 * All depending on the load and if instances have timed-out and have been
	 * destroyed
	 */
	private final PoolQueue pool;

	/**
	 * Acts as gate keeper only allowing a maximum number of concurrent
	 * users/threads for this pool.
	 */
	private final Semaphore permits;

	PoolImpl(ThrowableFunction0<T> instanceFactory, int maxSize, Option<Predicate<T>> validator) {
		this.instanceFactory = instanceFactory;
		this.maxSize = maxSize;
		this.validator = validator;
		this.permits = new Semaphore(maxSize);
		this.pool = new PoolQueue<>(maxSize, PoolMode.FIFO); // TODO make
																// FIFO/LIFO
																// configurable
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see simplepool.Pool#getInstance(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Try<T> getInstance(Duration maxWaitTime) {
		return Try(() -> {
			// attempt to get a go ahead by acquiring a semaphore
			if (!permits.tryAcquire(maxWaitTime.toMillis(), TimeUnit.MILLISECONDS)) {
				throw new TimeoutException("Timeout waiting for idle object in the pool");
			}

			// OK so now we got a semaphore, i.e. we got a go ahead to take a
			// pooled
			// instance
			// But if the queue/pool is empty it means that there is a missing
			// instance in the pool
			// In this case we simply attempt to create a new instance.
//			T instance = null;

			
//			 // if no instance was found create one
//			 // this is the case when the pool has not yet been maxed out
//			 if (instance == null) {
//			 logger.debug("No working objects and the pool [{}] has not reached limit, will create new object.", factory);
//			 try {
//			 instance = instanceFactory.apply();
//			 } catch (Exception e) {
//			 // for some reason we failed to create an instance
//			 // release the semaphore that was previously acquired otherwise
//			 // me might drain all semaphores
//			 String message = "Failed to create instance for [" + factory +"]";
//			 logger.warn(message, e);
//			 permits.release();
//			 throw new ObjectPoolException(message, e);
//			 }
//			 }

			// invoke the life-cycle event initialize()
			// factory.initialize(instance);

			 return null;
		});
	}

}
