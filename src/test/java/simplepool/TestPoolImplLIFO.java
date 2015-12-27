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

import simplepool.Constants.PoolMode;

/**
 * Test the class {@link PoolQueue}
 * @author Peter Nerg
 */
public class TestPoolImplLIFO extends AbstractPoolQueueTest {

	public TestPoolImplLIFO() {
		super(PoolMode.LIFO);
	}

	@Test(timeout=5000)
	public void poll_nonEmpty() throws Throwable {
		pool.returnInstance("First");
		pool.returnInstance("Second");
		
		assertEquals("Second", pool.getInstance().get());
		assertEquals("First", pool.getInstance().get());
	}
	
}
