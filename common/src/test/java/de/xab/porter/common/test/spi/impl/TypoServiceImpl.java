package de.xab.porter.common.test.spi.impl;

import de.xab.porter.common.test.spi.service.MockService;

/**
 * service not registered
 */
public class TypoServiceImpl implements MockService {
    @Override
    public String mock() {
        return "none registered";
    }
}
