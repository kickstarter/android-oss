package com.kickstarter;

import android.content.Context;

import com.kickstarter.ui.DiscoveryActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
  injects = {DiscoveryActivity.class},
  addsTo = AndroidModule.class,
  library = true
)
public class ActivityModule {
  private final BaseActivity activity;

  public ActivityModule(BaseActivity activity) {
    this.activity = activity;
  }

  @Provides @Singleton @ForActivity Context provideActivityContext() {
    return activity;
  }
}
