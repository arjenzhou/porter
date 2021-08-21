package de.xab.porter.common.test.spi;

import de.xab.porter.common.spi.ExtensionLoader;
import de.xab.porter.common.test.spi.service.MockService;
import de.xab.porter.common.test.spi.service.UnregisteredService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * test class for porter SPI
 */
public class SPITest {
    @Test
    public void testExists() {
        MockService impl = ExtensionLoader.getExtensionLoader(MockService.class).loadExtension(null, "impl");
        assertEquals("hello world", impl.mock());
    }

    @Test
    public void testNotExists() {
        assertThrows(TypeNotPresentException.class, () ->
                ExtensionLoader.getExtensionLoader(MockService.class).loadExtension(null, "none"));
    }

    @Test
    public void testTypoImpl() {
        assertThrows(IllegalArgumentException.class, () ->
                ExtensionLoader.getExtensionLoader(MockService.class).loadExtension(null, "typo"));
    }

    @Test
    public void testNoImplemented() {
        assertThrows(IllegalStateException.class, () ->
                ExtensionLoader.getExtensionLoader(MockService.class).loadExtension(null, "noimpl"));
    }

    @Test
    public void testNoneRegistered() {
        assertThrows(TypeNotPresentException.class, () ->
                ExtensionLoader.getExtensionLoader(UnregisteredService.class).loadExtension(null, "any"));
    }
}
