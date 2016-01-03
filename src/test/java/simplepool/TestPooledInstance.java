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

import org.junit.Test;

/**
 * @author Peter Nerg
 *
 */
public class TestPooledInstance extends BaseAssert {
	private final PoolableObject object = new PoolableObject("");
	private final PooledInstance<PoolableObject> instance = new PooledInstance<>(object);
			
	@Test
	public void lastUsed() {
		assertTrue(instance.lastUsed() <= System.currentTimeMillis());
	}
	
	@Test
	public void instance() {
		assertEquals(object, instance.instance());
	}

	@Test
	public void markAsUsedOrDestroyed() {
		assertTrue(instance.markAsUsedOrDestroyed());
		assertFalse(instance.markAsUsedOrDestroyed());
	}
	
	@Test
	public void toString_t() {
		assertNotNull(instance.toString());
	}
}
