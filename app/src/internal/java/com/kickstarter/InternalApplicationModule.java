package com.kickstarter;

import android.content.SharedPreferences;

import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.InternalTools;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public final class InternalApplicationModule {
  @Provides
  @Singleton
  ApiEndpoint provideApiEndpoint(@ApiEndpointPreference final @NonNull StringPreferenceType apiEndpointPreference) {
    return ApiEndpoint.from(apiEndpointPreference.get());
  }

  @Provides
  @Singleton
  @ApiEndpointPreference
  @NonNull StringPreferenceType provideApiEndpointPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "debug_api_endpoint", ApiEndpoint.PRODUCTION.url());
  }

  @Provides
  BuildCheck provideBuildCheck() {
    return BuildCheck.DEFAULT;
  }

  @Provides
  @Singleton
  @NonNull
  InternalToolsType providesInternalToolsType() {
    return new InternalTools();
  }

  @Provides
  @Singleton
  @NonNull
  static ApolloClientType provideApolloClientType(final @NonNull ApolloClient apolloClient) {
    return Secrets.IS_OSS ? new MockApolloClient() : new KSApolloClient(apolloClient);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiClientType provideApiClientType(final @NonNull ApiService apiService, final @NonNull Gson gson) {
    return Secrets.IS_OSS ? new MockApiClient() : new ApiClient(apiService, gson);
  }
}
