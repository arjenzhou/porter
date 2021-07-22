package de.xab.porter.common.test.string;

import de.xab.porter.common.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * test class for Strings util
 */
public class StringTest {
    @Test
    public void testNull() {
        String str = null;
        assertFalse(Strings.notNullOrEmpty(str));
    }

    @Test
    public void testEmpty() {
        String str = "";
        assertFalse(Strings.notNullOrEmpty(str));
    }

    @Test
    public void testNotNullOrEmpty() {
        String str = "abc";
        Assertions.assertTrue(Strings.notNullOrEmpty(str));
    }

    @Test
    public void testLongEmpty() {
        String str = " ";
        assertFalse(Strings.notNullOrEmpty(str));
    }
}
