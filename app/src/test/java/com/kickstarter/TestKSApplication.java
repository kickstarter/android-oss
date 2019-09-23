package com.kickstarter;

public class TestKSApplication extends KSApplication {
  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
