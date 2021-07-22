package de.xab.porter.common;

import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.service.MockService;
import de.xab.porter.common.service.UnregisteredService;
import de.xab.porter.common.spi.ExtensionLoader;
import org.junit.jupiter.api.Test;

/**
 * test class for porter SPI
 */
public class SPITest {
  @Test
  public void testExists() {
    final MockService impl = ExtensionLoader.getExtensionLoader().loadExtension(
        "impl", MockService.class);
    assert "hello world".equals(impl.mock());
  }

  @Test
  public void testNotExists() {
    try {
      ExtensionLoader.getExtensionLoader().loadExtension("none",
                                                         MockService.class);
    } catch (PorterException e) {
      assert e.getCause() instanceof ClassNotFoundException;
    }
  }

  @Test
  public void testTypoImpl() {
    try {
      ExtensionLoader.getExtensionLoader().loadExtension("typo",
                                                         MockService.class);
    } catch (PorterException e) {
      assert e.getCause() instanceof ClassNotFoundException;
    }
  }

  @Test
  public void testNoImplemented() {
    try {
      ExtensionLoader.getExtensionLoader().loadExtension("noimpl",
                                                         MockService.class);
    } catch (PorterException e) {
      assert e.getCause() instanceof ClassNotFoundException;
    }
  }

  @Test
  public void testNoneRegistered() {
    try {
      ExtensionLoader.getExtensionLoader().loadExtension(
          "any", UnregisteredService.class);
    } catch (RuntimeException e) {
      assert e instanceof PorterException;
    }
  }
}
