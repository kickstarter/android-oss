package com.kickstarter;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.MockWebClient;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.WebClientType;

import java.net.CookieManager;

import javax.inject.Singleton;

import dagger.Provides;

public final class TestApplicationModule extends ApplicationModule {
  public TestApplicationModule(final @NonNull TestKSApplication application) {
    super(application);
  }

  @Provides
  @Singleton
  Environment provideEnvironment(final @NonNull @ActivitySamplePreference IntPreference activitySamplePreference,
    final @NonNull ApiClientType apiClient,
    final @NonNull BuildCheck buildCheck,
    final @NonNull CookieManager cookieManager,
    final @NonNull CurrentConfig currentConfig,
    final @NonNull CurrentUser currentUser,
    final @NonNull Koala koala,
    final @NonNull SharedPreferences sharedPreferences,
    final @NonNull WebClient webClient) {

    return Environment.builder()
      .activitySamplePreference(activitySamplePreference)
      .apiClient(apiClient)
      .buildCheck(buildCheck)
      .cookieManager(cookieManager)
      .currentConfig(currentConfig)
      .currentUser(currentUser)
      .koala(koala)
      .sharedPreferences(sharedPreferences)
      .webClient(webClient)
      .build();
  }

  @Provides
  @Singleton
  ApiClientType provideApiClientType() {
    return new MockApiClient();
  }

  @Provides
  @Singleton
  WebClientType provideWebClientType() {
    return new MockWebClient();
  }
}
