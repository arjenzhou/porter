package de.xab.porter.common.service;

/**
 * implementation of service not registered
 */
public class UnregisteredServiceImpl implements UnregisteredService {
  @Override
  public String hello() {
    return "hello";
  }
}
