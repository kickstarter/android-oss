package com.kickstarter;

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
  }

  @Override
  protected void createComponent() {
    final ApplicationComponent component = DaggerApplicationComponent.builder()
      .applicationModule(new TestApplicationModule(this))
      .build();
    component(component);
    component.inject(this);
  }

  @Override
  protected boolean isInUnitTests() {
    return true;
  }
}
