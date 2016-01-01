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

import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import junitextensions.OptionAssert;
import junitextensions.TryAssert;

/**
 * Base test class.
 * 
 * @author Peter Nerg
 */
public class BaseAssert extends Assert implements OptionAssert, TryAssert {

    static {
        // Configure language for proper logging outputs
        Locale.setDefault(Locale.US);
        System.setProperty("user.country", Locale.US.getCountry());
        System.setProperty("user.language", Locale.US.getLanguage());
        System.setProperty("user.variant", Locale.US.getVariant());
    }

    @BeforeClass
    public final static void setTempDirectoryToTarget() {
        System.setProperty("java.io.tmpdir", "target/");
    }

    @AfterClass
    public final static void resetTempDirectoryToTarget() {
        System.clearProperty("java.io.tmpdir");
    }

    /**
     * Assert that the provided arrays do not contain the same data.
     * 
     * @param expected
     * @param actual
     */
    public static void assertNotEquals(byte[] expected, byte[] actual) {
        if (expected == null && actual == null) {
            return;
        }

        // different size, can not be the same data
        if (expected.length != actual.length) {
            return;
        }

        // check each position in the arrays, return on the first found non-match
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != expected[i]) {
                return;
            }
        }
        fail("The expected and the actual array are the same");
    }

    /**
     * Assert that the provided arrays contain the same data.
     * 
     * @param expected
     * @param actual
     */
    public static void assertEquals(byte[] expected, byte[] actual) {
        if (expected == null && actual == null) {
            return;
        }

        assertEquals("The length of the arrays do not match", expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals("The data on index [" + i + "] does not match", expected[i], actual[i]);
        }
    }

    /**
     * Assert that the provided arrays contain the same data.
     * 
     * @param expected
     * @param actual
     */
    public static void assertEquals(char[] expected, char[] actual) {
        if (expected == null && actual == null) {
            return;
        }

        assertEquals("The length of the arrays do not match", expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals("The data on index [" + i + "] does not match", expected[i], actual[i]);
        }
    }

    /**
     * Assert that the provided arrays contain the same data.
     * 
     * @param expected
     * @param actual
     */
    public static void assertEquals(int[] expected, int[] actual) {
        if (expected == null && actual == null) {
            return;
        }

        assertEquals("The length of the arrays do not match", expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals("The data on index [" + i + "] does not match", expected[i], actual[i]);
        }
    }
    
	/**
	 * Asserts that that the provided object has been destroyed due to going stale.
	 * @param po
	 */
	static void assertIsDestroyed(PoolableObject po) {
		assertTrue("Expected ["+po+"] to be destroyed", po.isDestroyed());
	}
	
	/**
	 * Assert that the provided object is still valid, i.e. not destroyed.
	 * @param po
	 */
	static void assertIsValid(PoolableObject po) {
		assertTrue("Expected ["+po+"] to be valid", po.isValid());
	}


}
