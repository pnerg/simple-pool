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
 * Exception used in conjunction with the pool. <br>
 * E.g. any issue creating an object to the pool will end up in one of these.
 * @author Peter Nerg
 */
public final class PoolException extends RuntimeException {
	private static final long serialVersionUID = 5382005746070605737L;
	
	/**
	 * Creates the exception
	 * @param message The message
	 * @param cause The underlying cause
	 */
	 PoolException(String message, Throwable cause) {
		 super(message, cause);
	}

	 PoolException(String message) {
		 super(message);
	}
}
