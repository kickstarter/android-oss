package com.kickstarter;

public class TestKSApplication extends KSApplication {
  @Override
  protected boolean isInUnitTests() {
    return true;
  }
}
