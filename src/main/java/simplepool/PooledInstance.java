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
 */
final class PooledInstance<V> {
    /** The pooled object instance. */
    private final V instance;

    private final AtomicBoolean inUse = new AtomicBoolean(false);
    private final long lastUsed = System.currentTimeMillis();

    PooledInstance(V object) {
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

    long lastUsed() {
    	return lastUsed;
    }

    V instance() {
    	return instance;
    }
    
}
