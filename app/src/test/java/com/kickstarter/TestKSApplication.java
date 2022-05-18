package com.kickstarter;

public class TestKSApplication extends KSApplication {

  @Override
  public ApplicationComponent getComponent() {
    final ApplicationComponent component = DaggerApplicationComponent.builder()
            .applicationModule(new TestApplicationModule(this))
            .build();

    return component;
  }

  @Override
  public void onCreate() {
    // - LoginToutViewModelTest needs the FacebookSDK initialized
   // FacebookSdk.sdkInitialize(this);
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}

