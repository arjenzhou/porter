package de.xab.porter.common.test.spi.impl;

import de.xab.porter.common.test.spi.service.UnregisteredService;

/**
 * implementation of service not registered
 */
public class UnregisteredServiceImpl implements UnregisteredService {
    @Override
    public String hello() {
        return "hello";
    }
}
