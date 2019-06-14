package com.kickstarter;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;

import org.robolectric.shadows.gms.ShadowGooglePlayServicesUtil;

public class TestKSApplication extends KSApplication {
  @Override
  public void onCreate() {
    // TODO: Roboelectric is incompatible with Play Services 7.8.0+.
    // See issue: https://github.com/robolectric/robolectric/issues/1995
    // For now we just return that Play Services is disabled.
    ShadowGooglePlayServicesUtil.setIsGooglePlayServicesAvailable(ConnectionResult.SERVICE_DISABLED);
    super.onCreate();

    FacebookSdk.sdkInitialize(this);
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
