package de.xab.porter.common.service;

/**
 * mock service impl
 */
public class MockServiceImpl implements MockService {
  @Override
  public String mock() {
    return "hello world";
  }
}
