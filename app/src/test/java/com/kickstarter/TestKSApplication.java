package com.kickstarter;

public class TestKSApplication extends KSApplication {
  protected boolean isInUnitTests() {
    return true;
  }
}
