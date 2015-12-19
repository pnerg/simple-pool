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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javascalautils.ThrowableFunction0;
import javascalautils.Try;
import simplepool.Constants.PoolMode;

/**
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

	/**
	 * Keeps references to object instances. <br>
	 * The number of objects in the queue/pool can range between zero and max
	 * instances. <br>
	 * All depending on the load and if instances have timed-out and have been
	 * destroyed
	 */
	private final PoolQueue<T> pool;

	/**
	 * Acts as gate keeper only allowing a maximum number of concurrent
	 * users/threads for this pool.
	 */
	private final Semaphore permits;

	PoolImpl(ThrowableFunction0<T> instanceFactory, int maxSize, Predicate<T> validator, Consumer<T> destructor, PoolMode poolMode) {
		this.instanceFactory = instanceFactory;
		this.validator = validator;
		this.destructor = destructor;
		this.permits = new Semaphore(maxSize);
		this.pool = new PoolQueue<>(maxSize, poolMode); 
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
			
			return pool.poll().getOrElse(() -> createInstance());
		});
	}

	/* (non-Javadoc)
	 * @see simplepool.Pool#returnInstance(java.lang.Object)
	 */
	@Override
	public void returnInstance(T instance) {
		//null instances are ignored
        if (instance == null) {
            return;
        }

        if (validator.test(instance)) {
			// ok
		}
        else {
        	destructor.accept(instance);
        }

        

//        try {
//            // invoke the life-cycle event cleanup()
//            factory.cleanup(object);
//
//            // destroy the object if:
//            // 1) the pool has been destroyed
//            // 2) it fails validation
//            // 3) if the queue rejects the object (queue is full)
//            if (destroyed.get() || !factory.isValid(object) || !pool.offer(new PooledInstanceWrapper(object))) {
//                destroy(object);
//            }
//        } catch (Exception e) {
//            logger.warn("Failed to execute [cleanup] on instance of [" + object.getClass().getName() + "] for [" + factory + "]", e);
//        } finally {
//            // last but not least, release one permit
//            // this is done regardless if the object was destroyed or not
//            // we would otherwise sooner or later run out of permits
//            permits.release();
//        }
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

}
