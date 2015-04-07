package com.kickstarter;


import android.content.Context;
import android.location.LocationManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.LOCATION_SERVICE;

// A module for Android-specific dependencies which require a Context or
// Application to create.
@Module(library = true)
public class AndroidModule {
  private final KsrApplication application;

  public AndroidModule(KsrApplication application) {
    this.application = application;
  }

  // Allow the application context to be injected but required that it be
  // annotated to explicitly differentiate it from an activity context.
  @Provides @Singleton @ForApplication Context provideApplicationContext() {
    return application;
  }

  @Provides @Singleton LocationManager provideLocationManager() {
    return (LocationManager) application.getSystemService(LOCATION_SERVICE);
  }
}
