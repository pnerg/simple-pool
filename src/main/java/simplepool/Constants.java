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

/**
 * Constants to be used by the library.
 * 
 * @author Peter Nerg
 * @since 1.0
 */
public interface Constants {
	/**
	 * The modes for which the pool may operate.
	 * 
	 * @since 1.0
	 */
	enum PoolMode {
		/**
		 * First-In, First-Out. <br>
		 * A traditional queue, objects added to the pool are placed last. <br>
		 * This means that instances are rotated in the pool, all are equally used. <br>
		 * 
		 * @since 1.0
		 */
		FIFO,
		/**
		 * Last-In, First-Out. <br>
		 * A traditional stack, objects added to the pool are placed first. <br>
		 * Instances added back to the pool will be added first and thus be the most frequently used instances. <br>
		 * This can mean that items "lower" down in the stack may become stale and get destroyed in case an idle time has been configured.
		 * 
		 * @since 1.0
		 */
		LIFO
	}
}
