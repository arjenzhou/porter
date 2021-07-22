package de.xab.porter.common.test.spi;

import de.xab.porter.api.exception.PorterException;
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
        MockService impl = ExtensionLoader.getExtensionLoader().loadExtension("impl", MockService.class);
        assertEquals("hello world", impl.mock());
    }

    @Test
    public void testNotExists() {
        assertThrows(PorterException.class, () ->
                ExtensionLoader.getExtensionLoader().loadExtension("none", MockService.class));
    }

    @Test
    public void testTypoImpl() {
        assertThrows(PorterException.class, () ->
                ExtensionLoader.getExtensionLoader().loadExtension("typo", MockService.class));
    }

    @Test
    public void testNoImplemented() {
        assertThrows(PorterException.class, () ->
                ExtensionLoader.getExtensionLoader().loadExtension("noimpl", MockService.class));
    }

    @Test
    public void testNoneRegistered() {
        assertThrows(PorterException.class, () ->
                ExtensionLoader.getExtensionLoader().loadExtension("any", UnregisteredService.class));
    }
}
