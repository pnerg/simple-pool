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
 * Test the class {@link Factory}
 * @author Peter Nerg
 */
public class TestFactory extends BaseAssert {

	private final Factory<PoolableObject> factory = Factory.poolFor(() -> new PoolableObject());
	
	@Test
	public void create_WithOnlyFactory() {
		Pool<PoolableObject> pool = factory.create();
		assertNotNull(pool);
	}
	
	@Test
	public void create_WithSpecifiedSize() {
		Pool<PoolableObject> pool = factory.ofSize(666).create();
		assertNotNull(pool);
	}

	@Test
	public void create_WithValidator() {
		Pool<PoolableObject> pool = factory.withValidator(po -> true).create();
		assertNotNull(pool);
	}

	@Test
	public void create_WithDestructor() {
		Pool<PoolableObject> pool = factory.withDestructor(po -> {}).create();
		assertNotNull(pool);
	}
}
