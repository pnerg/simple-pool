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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper for pooled object. <br>
 * Keeps state information such as when the object last was used, i.e. pooled and if the object is valid.
 * @author Peter Nerg
 * @param V The type of the value kept by this instance
 */
final class PooledInstance<T> {
    /** The pooled object instance. */
    private final T instance;

    private final AtomicBoolean inUse = new AtomicBoolean(false);
    private final long lastUsed = System.currentTimeMillis();

    /**
     * Creates a wrapper instance
     * @param object The object instance to wrap
     */
    PooledInstance(T object) {
        this.instance = object;
    }

    /**
     * Attempt to mark this pooled instance as either used or destroyed. <br>
     * "In use" means that it has been pulled out of the pool. <br>
     * "Destroyed" means that the idle time reaper has destroyed the instance.
     * 
     * @return <code>true</code> it the operation was successful.
     */
    boolean markAsUsedOrDestroyed() {
        return inUse.compareAndSet(false, true);
    }

    /**
     * The last time the wrapped object was used.
     * @return Time in millis
     */
    long lastUsed() {
    	return lastUsed;
    }

    /**
     * Get the wrapped object instance
     * @return The object instance
     */
    T instance() {
    	return instance;
    }
    
}
