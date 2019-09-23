package com.kickstarter;

import com.facebook.FacebookSdk;

public class TestKSApplication extends KSApplication {

  @Override
  public void onCreate() {
    super.onCreate();

    FacebookSdk.sdkInitialize(this);
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
