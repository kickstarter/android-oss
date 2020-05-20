package com.kickstarter;

import com.facebook.FacebookSdk;
import com.google.firebase.analytics.FirebaseAnalytics;

public class TestKSApplication extends KSApplication {

  @Override
  public void onCreate() {
    super.onCreate();

    //FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
    FacebookSdk.sdkInitialize(this);
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
