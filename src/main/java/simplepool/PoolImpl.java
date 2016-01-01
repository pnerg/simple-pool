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

import static javascalautils.OptionCompanion.None;
import static javascalautils.OptionCompanion.Option;
import static javascalautils.TryCompanion.Try;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javascalautils.Option;
import javascalautils.ThrowableFunction0;
import javascalautils.Try;
import javascalautils.Unit;
import javascalautils.Validator;
import simplepool.Constants.PoolMode;

/**
 * The pool implementation.
 * 
 * @author Peter Nerg
 */
final class PoolImpl<T> implements Pool<T> {
	private final ThrowableFunction0<T> instanceFactory;
	private final Predicate<T> validator;
	private final Consumer<T> destructor;
	// /** Used to schedule and execute this idle object reaper. */
	// private final ScheduledExecutorService scheduledExecutorService =
	// Executors.newScheduledThreadPool(1, new
	// NamedSequenceThreadFactory("ObjectPool-Reaper"));

	/** The actual queue implementation. */
	private final PoolQueue<T> poolQueue;

	/**
	 * Acts as gate keeper only allowing a maximum number of concurrent users/threads for this pool.
	 */
	private final Semaphore getPermits;
	private final Semaphore returnPermits = new Semaphore(0);
	private final Duration idleTimeout;

	PoolImpl(ThrowableFunction0<T> instanceFactory, int maxSize, Predicate<T> validator, Consumer<T> destructor, PoolMode poolMode, Duration idleTimeout) {
		poolQueue = poolMode == PoolMode.FIFO ? new PoolQueueFIFO<>() : new PoolQueueLIFO<>();
		this.instanceFactory = instanceFactory;
		this.validator = validator;
		this.destructor = destructor;
		this.idleTimeout = idleTimeout;
		this.getPermits = new Semaphore(maxSize);
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
			if (!getPermits.tryAcquire(maxWaitTime.toMillis(), TimeUnit.MILLISECONDS)) {
				throw new TimeoutException("Timeout waiting for a free object in the pool");
			}

			returnPermits.release();
			return poolQueue.head().getOrElse(() -> createInstance());
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see simplepool.Pool#returnInstance(java.lang.Object)
	 */
	@Override
	public Try<Unit> returnInstance(T instance) {
		return Try(() -> {
			Validator.requireNonNull(instance);

			if (!returnPermits.tryAcquire()) {
				throw new PoolException("No permits left to return object to the pool");
			}

			// first validate the instance
			// if we fail validation the instance is destroyed and the pooled
			// instance is marked as destroyed
			if (validator.test(instance)) {
				poolQueue.add(instance);
			} else {
				destructor.accept(instance);
			}

			// now release a permit to take a new item from the pool
			getPermits.release();

			return new Unit();
		});
	}

	private T createInstance() {
		try {
			return instanceFactory.apply();
		} catch (Throwable ex) {
			// for some reason we failed to create an instance
			// release the semaphore that was previously acquired otherwise
			// me might drain all semaphores
			getPermits.release();
			throw new PoolException("Failed to create instance", ex);
		}
	}

}
