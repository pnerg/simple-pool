/**
 * Copyright 2015 Peter Nerg
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

import static javascalautils.Option.None;
import static javascalautils.OptionCompanion.Some;
import static javascalautils.Validator.requireNonNull;
import java.util.function.Predicate;

import javascalautils.Option;
import javascalautils.ThrowableFunction0;
/**
 * Factory for creating pool instances.
 * @author Peter Nerg
 */
public final class Factory<T> {
	
	private final ThrowableFunction0<T> instanceFactory;
	private int size = 50;
	private Option<Predicate<T>> validator = None();

	private Factory(ThrowableFunction0<T> instanceFactory) {
		this.instanceFactory = instanceFactory;
	}
	
	public static <T> Factory<T> poolFor(ThrowableFunction0<T> instanceFactory){ 
		requireNonNull(instanceFactory);
		return new Factory<>(instanceFactory);
	}
	
	public Factory<T> ofSize(int size) {
		this.size = size;
		return this;
	}
	
	public Factory<T> withValidator(Predicate<T> validator) {
		this.validator = Some(validator);
		return this;
	}

	public Pool<T> create() {
		return new PoolImpl<>(instanceFactory, size, validator);
	}
	
}
