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

import static javascalautils.TryCompanion.Try;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javascalautils.Option;
import javascalautils.ThrowableFunction0;
import javascalautils.Try;
import javascalautils.Unit;
import javascalautils.Validator;
import javascalautils.concurrent.Future;
import simplepool.Constants.PoolMode;
import static javascalautils.concurrent.FutureCompanion.Future;

/**
 * The pool implementation.
 * 
 * @author Peter Nerg
 */
final class PoolImpl<T> implements Pool<T> {
	private final ThrowableFunction0<T> instanceFactory;
	private final Predicate<T> validator;
	private final Consumer<T> destructor;

	/** The actual queue implementation. */
	private final PoolQueue<T> poolQueue;

	/**
	 * Acts as gate keeper only allowing a maximum number of concurrent users/threads for this pool.
	 */
	private final Semaphore getPermits;
	private final Semaphore returnPermits = new Semaphore(0);
	private final Option<ScheduledFuture<?>> scheduledFuture;

	/**
	 * If this pool is valid. <br>
	 * I.e. {@link #destroy()} has not been invoked.
	 */
	private final AtomicBoolean isValid = new AtomicBoolean(true);
	private final int maxSize;

	PoolImpl(ThrowableFunction0<T> instanceFactory, int maxSize, Predicate<T> validator, Consumer<T> destructor, PoolMode poolMode, Duration idleTimeout, Option<ScheduledExecutorService> executor) {
		this.maxSize = maxSize;
		poolQueue = poolMode == PoolMode.FIFO ? new PoolQueueFIFO<>() : new PoolQueueLIFO<>();
		this.instanceFactory = instanceFactory;
		this.validator = validator;
		this.destructor = destructor;
		this.getPermits = new Semaphore(maxSize);

		long delayMillis = idleTimeout.toMillis();

		scheduledFuture = executor.map(ss -> {
			return ss.scheduleWithFixedDelay(() -> {
				poolQueue.markStaleInstances(idleTimeout, destructor);
			} , delayMillis, delayMillis / 4, TimeUnit.MILLISECONDS);
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see simplepool.Pool#getInstance(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Try<T> getInstance(Duration maxWaitTime) {
		if (!isValid.get()) {
			throw new IllegalStateException("Pool has been destroyed.");
		}

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

			return Unit.Instance;
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see simplepool.Pool#destroy()
	 */
	@Override
	public Future<Unit> destroy() {
		return Future(() -> {
			if (isValid.compareAndSet(true, false)) {
				scheduledFuture.forEach(sf -> sf.cancel(true));

				// immediately drain all free resources.
				int permitsLeft = maxSize - getPermits.drainPermits();

				// still outstanding resources borrowed from the pool
				// we must wait until each of them has been returned
				while (permitsLeft > 0) {
					getPermits.acquire();
					permitsLeft--;
				}
				
				// with all permits acquired we know all items in the pool have been returned (or never used)
				// we can now safely destroy all items in the pool
				// with Zero duration we will in practice mark any item in the pool as stale and destroy it
				poolQueue.markStaleInstances(Duration.ZERO, destructor);
			}
			return Unit.Instance;
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
			returnPermits.tryAcquire();
			throw new PoolException("Failed to create instance", ex);
		}
	}

}
