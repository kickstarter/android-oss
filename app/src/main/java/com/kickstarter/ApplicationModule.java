package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import com.kickstarter.libs.ConfigLoader;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.ForApplication;
import com.kickstarter.libs.Money;
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
  Context provideApplicationContext() {
    return application;
  }

  @Provides
  @Singleton
  AssetManager provideAssetManager() {
    return application.getAssets();
  }

  @Provides
  @Singleton
  ConfigLoader provideConfigLoader(final AssetManager assetManager, final SharedPreferences sharedPreferences) {
    return new ConfigLoader(assetManager, sharedPreferences);
  }

  @Provides
  @Singleton
  Font provideFont(final AssetManager assetManager) {
    return new Font(assetManager);
  }

  @Provides
  @Singleton
  Money provideMoney(final ConfigLoader configLoader) {
    return new Money(configLoader);
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
