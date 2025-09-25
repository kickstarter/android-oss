package com.kickstarter;

import android.content.SharedPreferences;

import com.apollographql.apollo3.ApolloClient;
import com.google.gson.Gson;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.InternalTools;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.mock.services.MockApiClientV2;
import com.kickstarter.mock.services.MockApolloClientV2;
import com.kickstarter.services.ApiClientTypeV2;
import com.kickstarter.services.ApiClientV2;
import com.kickstarter.services.ApiServiceV2;
import com.kickstarter.services.ApolloClientTypeV2;
import com.kickstarter.services.KSApolloClientV2;

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
  @Singleton
  @NonNull
  InternalToolsType providesInternalToolsType() {
    return new InternalTools();
  }


  @Provides
  @Singleton
  @NonNull
  static ApolloClientTypeV2 provideApolloClientTypeV2(final @NonNull ApolloClient apolloClient, final @NonNull Gson gson) {
    return Secrets.IS_OSS ? new MockApolloClientV2() : new KSApolloClientV2(apolloClient, gson);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiClientTypeV2 provideApiClientTypeV2(final @NonNull ApiServiceV2 apiService, final @NonNull Gson gson) {
    return Secrets.IS_OSS ? new MockApiClientV2() : new ApiClientV2(apiService, gson);
  }
}
