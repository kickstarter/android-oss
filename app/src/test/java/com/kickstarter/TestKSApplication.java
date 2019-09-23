package com.kickstarter;

public class TestKSApplication extends KSApplication {

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
