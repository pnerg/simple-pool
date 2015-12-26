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
import static javascalautils.OptionCompanion.Option;
import static javascalautils.Validator.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javascalautils.Option;
import javascalautils.ThrowableFunction0;
import simplepool.Constants.PoolMode;

/**
 * Factory for creating pool instances. <br>
 * The factory works according to a builder pattern where one starts by creating the factory ({@link #poolFor(ThrowableFunction0) poolFor}) and finishes by invoking {@link #create()}. <br>
 * In between those invocations one may add optional behavior, such as the {@link #ofSize(int) size} of the pool.
 * @author Peter Nerg
 * @param <T> The type the pool shall produce
 */
public final class Factory<T> {
	
	private final ThrowableFunction0<T> instanceFactory;
	private int size = 50;
	private PoolMode poolMode = PoolMode.FIFO; //TODO make this configurable
	private Option<Predicate<T>> validator = None();
	private Option<Consumer<T>> destructor = None();
	
	private Factory(ThrowableFunction0<T> instanceFactory) {
		this.instanceFactory = instanceFactory;
	}
	
	/**
	 * Creates the pool factory. <br>
	 * This is the starting point for building a pool. <br>
	 * The <i>instanceFactory</i> is mandatory as it is the function the pool will use when it needs to create instances.
	 * @param instanceFactory The function that shall produce the instances for the pool.
	 * @return The pool factory
	 */
	public static <T> Factory<T> poolFor(ThrowableFunction0<T> instanceFactory){ 
		requireNonNull(instanceFactory);
		return new Factory<>(instanceFactory);
	}
	
	/**
	 * Specifies the maximum size of the pool (optional). <br>
	 * If not specified the default size is <tt>50</tt>
	 * @param size The maximum size of the pool
	 * @return The pool factory
	 */
	public Factory<T> ofSize(int size) {
		this.size = size;
		return this;
	}
	
	/**
	 * Provides a validator function to the pool (Optional). <br>
	 * The validator is used every time an instance is returned to the pool. <br>
	 * If the instance fails the validation it will be destroyed as opposed to returned to the pool.
	 * @param validator The validator function
	 * @return The pool factory
	 */
	public Factory<T> withValidator(Predicate<T> validator) {
		this.validator = Option(validator);
		return this;
	}

	/**
	 * Provides a destructor function to the pool (Optional). <br>
	 * The destructor is used by the pool when an instance is discarded and destroyed from the pool. <br>
	 * Typical scenarios are:
	 * <ul>
	 * <li>An instance fails {@link #withValidator(Predicate) validation} when returning it to the pool</li>
	 * <li>The instance has passed its idle time in the pool and is therefore evicted and destroyed.</li>
	 * </ul>
	 * 
	 * @param destructor The destructor function
	 * @return The pool factory
	 */
	public Factory<T> withDestructor(Consumer<T> destructor) {
		this.destructor  = Option(destructor); 
		return this;
	}
	
	/**
	 * Creates the pool instance. <br>
	 * Final operation once the all needed properties have been set on the factory.
	 * @return The pool
	 */
	public Pool<T> create() {
		Predicate<T> v = validator.getOrElse(() -> t -> true); //default validator always states true
		Consumer<T>  c = destructor.getOrElse(() -> t -> {}); //default destructor does nothing

		return new PoolImpl<>(instanceFactory, size, v, c, poolMode);
	}
	
}
