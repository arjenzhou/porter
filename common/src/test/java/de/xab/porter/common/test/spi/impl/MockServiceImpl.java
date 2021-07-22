package de.xab.porter.common.test.spi.impl;

import de.xab.porter.common.test.spi.service.MockService;

/**
 * mock service impl
 */
public class MockServiceImpl implements MockService {
    @Override
    public String mock() {
        return "hello world";
    }
}
