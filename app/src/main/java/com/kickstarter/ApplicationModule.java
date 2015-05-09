package com.kickstarter;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kickstarter.libs.Font;
import com.kickstarter.libs.ForApplication;
import com.kickstarter.models.CurrentUser;
import com.kickstarter.services.KickstarterClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
  private final Application application;

  public ApplicationModule(final Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  @ForApplication
  Application application() {
    return application;
  }

  @Provides
  @Singleton
  Font provideFont() {
    return new Font(application);
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides
  @Singleton
  CurrentUser provideCurrentUser(final SharedPreferences sharedPreferences) {
    return new CurrentUser(sharedPreferences);
  }

  @Provides
  @Singleton
  KickstarterClient provideKickstarterClient(final CurrentUser currentUser) {
    return new KickstarterClient(currentUser);
  }
}
