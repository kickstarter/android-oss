package com.kickstarter;

import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;

public class TestKSApplication extends KSApplication {

  @Override
  public void onCreate() {
    FacebookSdk.sdkInitialize(this);
    FirebaseApp.initializeApp(this);
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
