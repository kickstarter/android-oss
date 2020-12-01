package com.kickstarter;

import com.facebook.FacebookSdk;

public class TestKSApplication extends KSApplication {

  @Override
  public void onCreate() {
    FacebookSdk.sdkInitialize(this);
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
