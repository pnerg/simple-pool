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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import javascalautils.Try;
import simplepool.Constants.PoolMode;

/**
 * Test the class {@link Factory}
 * 
 * @author Peter Nerg
 */
public class TestFactory extends BaseAssert {

	private final AtomicLong counter = new AtomicLong(1);
	private final Factory<PoolableObject> factory = Factory.poolFor(() -> new PoolableObject("" + counter.getAndIncrement()));

	@Test
	public void create_withOnlyFactory() {
		Pool<PoolableObject> pool = factory.create();
		assertCreatedPool(pool);
	}

	@Test
	public void create_withSpecifiedSize() {
		Pool<PoolableObject> pool = factory.ofSize(666).create();
		assertCreatedPool(pool);
	}

	@Test
	public void create_withValidator() {
		Pool<PoolableObject> pool = factory.withValidator(po -> false).create();
		assertCreatedPool(pool);
	}

	@Test
	public void create_withDestructor() {
		Pool<PoolableObject> pool = factory.withDestructor(po -> po.destroy()).create();
		assertCreatedPool(pool);
	}

	@Test
	public void create_withIdleTimeout(){
		Pool<PoolableObject> pool = factory.withIdleTimeout(Duration.ofMillis(666)).create();
		assertCreatedPool(pool);
	}

	@Test
	public void create_withPoolMode() {
		Pool<PoolableObject> pool = factory.withPoolMode(PoolMode.LIFO).create();
		assertCreatedPool(pool);
	}

	private void assertCreatedPool(Pool<PoolableObject> pool) {
		assertNotNull(pool);
		Try<PoolableObject> instance = pool.getInstance(Duration.ofMillis(69));
		assertIsSuccess(instance);
		instance.forEach(po ->{
			assertEquals("1", po.value());
			assertIsSuccess(pool.returnInstance(po));
		});
	}
}
