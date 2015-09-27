package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.AutoParcelAdapterFactory;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.ConfigLoader;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.ForApplication;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.services.KickstarterWebViewClient;

import org.joda.time.DateTime;

import java.net.CookieManager;

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
  @AccessTokenPreference
  StringPreference provideAccessTokenPreference(final SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "access_token");
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return application;
  }

  @Provides
  @Singleton
  ApiClient provideApiClient(final ApiEndpoint apiEndpoint, final Build build, final String clientId, final CurrentUser currentUser, final Gson gson) {
    return new ApiClient(apiEndpoint, build, clientId, currentUser, gson);
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
  Build provideBuild(final PackageInfo packageInfo) {
    return new Build(packageInfo);
  }

  @Provides
  @Singleton
  String provideClientId(final ApiEndpoint apiEndpoint) {
    return apiEndpoint == ApiEndpoint.PRODUCTION ?
      "***REMOVED***" :
      "***REMOVED***";
  }

  @Provides
  @Singleton
  ConfigLoader provideConfigLoader(final AssetManager assetManager) {
    return new ConfigLoader(assetManager);
  }

  @Provides
  @Singleton
  CookieManager provideCookieManager() {
    return new CookieManager();
  }

  @Provides
  @Singleton
  CurrentUser provideCurrentUser(@AccessTokenPreference final StringPreference accessTokenPreference,
    final Gson gson,
    @UserPreference final StringPreference userPreference) {
    return new CurrentUser(accessTokenPreference, gson, userPreference);
  }

  @Provides
  @Singleton
  @WebEndpoint
  String provideWebEndpoint(final ApiEndpoint apiEndpoint) {
    final String url = (apiEndpoint == ApiEndpoint.PRODUCTION) ?
      "https://www.kickstarter.com" :
      apiEndpoint.url.replaceAll("(?<=\\Ahttps?:\\/\\/)api.", "");

    return url;
  }

  @Provides
  @Singleton
  Font provideFont(final AssetManager assetManager) {
    return new Font(assetManager);
  }

  @Provides
  @Singleton
  Gson provideGson(final AssetManager assetManager) {
    return new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .registerTypeAdapterFactory(new AutoParcelAdapterFactory())
      .create();
  }

  @Provides
  @Singleton
  KickstarterClient provideKickstarterClient(final Build build, @WebEndpoint final String webEndpoint) {
    return new KickstarterClient(build, webEndpoint);
  }

  @Provides
  KickstarterWebViewClient provideKickstarterWebViewClient(final Build build,
    final CookieManager cookieManager,
    final CurrentUser currentUser,
    @WebEndpoint final String webEndpoint) {
    return new KickstarterWebViewClient(build, cookieManager, currentUser, webEndpoint);
  }

  @Provides
  @Singleton
  Logout provideLogout(final CookieManager cookieManager, final CurrentUser currentUser) {
    return new Logout(cookieManager, currentUser);
  }

  @Provides
  @Singleton
  Money provideMoney(final ConfigLoader configLoader) {
    return new Money(configLoader);
  }

  @Provides
  @Singleton
  PackageInfo providePackageInfo(final Application application) {
    try {
      return application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  String providePackageName(final Application application) {
    return application.getPackageName();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides
  @Singleton
  @UserPreference
  StringPreference provideUserPreference(final SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "user");
  }
}
