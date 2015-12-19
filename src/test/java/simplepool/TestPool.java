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

import static javascalautils.TryCompanion.Success;

import java.time.Duration;

import org.junit.Test;

import javascalautils.Try;
/**
 * Test the class {@link Pool}
 * @author Peter Nerg
 */
public class TestPool extends BaseAssert {

	private final Pool<String> pool = new Pool<String>() {
		@Override
		public Try<String> getInstance(Duration maxWaitTime) {
			return Success("Whatever");
		}

		@Override
		public void returnInstance(String instance) {
		}
	};
	
	@Test
	public void getInstance_withoutDuration() throws Throwable {
		assertEquals("Whatever", pool.getInstance().get());
	}
	
	
}
