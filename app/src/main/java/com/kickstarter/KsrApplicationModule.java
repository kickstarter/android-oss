package com.kickstarter;

import android.app.Application;

import com.kickstarter.libs.Font;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class KsrApplicationModule {
  private final Application application;

  public KsrApplicationModule(Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  Application application() {
    return application;
  }

  @Provides
  @Singleton
  Font provideFont() {
    return new Font(application);
  }
}
