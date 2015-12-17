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

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javascalautils.Failure;
import javascalautils.Option;
import javascalautils.ThrowableFunction0;
import javascalautils.Try;

/**
 * @author Peter Nerg
 */
final class PoolImpl<T> implements Pool<T> {
	private final ThrowableFunction0<T> instanceFactory;
	private final int maxSize;
	private final Option<Predicate<T>> validator;

	PoolImpl(ThrowableFunction0<T> instanceFactory, int maxSize, Option<Predicate<T>> validator) {
		this.instanceFactory = instanceFactory;
		this.maxSize = maxSize;
		this.validator = validator;
	}
	
	/* (non-Javadoc)
	 * @see simplepool.Pool#getInstance(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Try<T> getInstance(long waitTime, TimeUnit timeUnit) {
		// TODO Auto-generated method stub
		return new Failure<>(new UnsupportedOperationException());
	}
}
