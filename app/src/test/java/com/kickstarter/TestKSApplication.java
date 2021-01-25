package com.kickstarter;

import com.facebook.FacebookSdk;

public class TestKSApplication extends KSApplication {

  @Override
  public void onCreate() {
    // - LoginToutViewModelTest needs the FacebookSDK initialized
    FacebookSdk.sdkInitialize(this);
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
