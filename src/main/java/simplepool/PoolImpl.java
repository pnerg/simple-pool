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
	private final Deque<PooledInstance<T>> queue = new ArrayDeque<>();

	/**
	 * Determines if the queue executes in FIFO (queue) or LIFO (stack) mode.
	 */
	private final PoolMode poolMode;

	private final int poolSize;

	/**
	 * Acts as gate keeper only allowing a maximum number of concurrent users/threads for this pool.
	 */
	private final Semaphore permits;
	private final ReentrantLock lock = new ReentrantLock();

	PoolImpl(ThrowableFunction0<T> instanceFactory, int maxSize, Predicate<T> validator, Consumer<T> destructor, PoolMode poolMode) {
		this.instanceFactory = instanceFactory;
		poolSize = maxSize;
		this.validator = validator;
		this.destructor = destructor;
		this.poolMode = poolMode;
		this.permits = new Semaphore(maxSize);
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

			return head().getOrElse(() -> createInstance());
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

			PooledInstance<T> pooledInstance = new PooledInstance<>(instance);

			// first validate the instance
			// if we fail validation the instance is destroyed and the pooled
			// instance is marked as destroyed
			boolean isValid = validator.test(instance);
			if (!isValid) {
				destroy(pooledInstance);
			}

			try(AutoReleaseLock l = new AutoReleaseLock()) {
				if (queue.size() >= poolSize) {
					throw new PoolException("Pool is full");
				}

				// only add the instance back to the pool if it was valid
				if (isValid) {
					if (poolMode == PoolMode.FIFO)
						queue.addLast(pooledInstance);
					else
						queue.addFirst(pooledInstance);
				}

				// now release a permit to take a new item from the pool
				permits.release();
			}
			
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
			permits.release();
			throw new PoolException("Failed to create instance", ex);
		}
	}

	private void destroy(PooledInstance<T> pooledInstance) {
		if (pooledInstance.markAsUsedOrDestroyed()) {
			destructor.accept(pooledInstance.instance());
		}
	}

	/**
	 * Get the first free instance from the queue. <br>
	 * The instance is always drawn from the front of the queue.
	 * 
	 * @return The instance, or None if no valid instance was found
	 */
	private Option<T> head() {
		Option<PooledInstance<T>> head = None();
		try(AutoReleaseLock l = new AutoReleaseLock()) {
			// first take the head of the queue and validate it's defines, i.e. exists
			// then attempt to mark the instance as used
			// if we fail to do so it means that the idle reaper has
			// destroyed it, thus we skip and take the next
			// keep looping until either a valid object is found or the end of the queue is reached
			do {
				 head = Option(queue.poll());
			} while(head.isDefined() && !head.map(pi -> pi.markAsUsedOrDestroyed()).getOrElse(() -> false));
			
			return head.map(pi -> pi.instance());
		}
		
	}

	/**
	 * Nothing but a wrapper to get auto/take-release of the internal lock. <br>
	 * It's to be able to write try-with-resources statements that automatically close this class and thus releasing the lock.
	 * @author Peter Nerg
	 */
	private final class AutoReleaseLock implements Closeable {

		private AutoReleaseLock() {
			lock.lock();
		}

		/**
		 * Releases the lock taken in the constructor.
		 */
		@Override
		public void close()  {
			lock.unlock();
		}
		
	}
}
