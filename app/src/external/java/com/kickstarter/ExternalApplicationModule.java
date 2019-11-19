package com.kickstarter;

import androidx.annotation.NonNull;

import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.NoopBuildCheck;
import com.kickstarter.libs.NoopInternalTools;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public final class ExternalApplicationModule {
  private ExternalApplicationModule() {}

  @Provides
  @Singleton
  static ApiEndpoint provideApiEndpoint() {
    return ApiEndpoint.PRODUCTION;
  }

  @Provides
  static BuildCheck provideBuildCheck() {
    return new NoopBuildCheck();
  }

  @Provides
  @Singleton
  @NonNull
  static InternalToolsType providesInternalToolsType() {
    return new NoopInternalTools();
  }

  @Provides
  @Singleton
  @NonNull
  static ApiClientType provideApiClientType(final @NonNull ApiService apiService, final @NonNull Gson gson) {
    return Secrets.IS_OSS ? new MockApiClient() : new ApiClient(apiService, gson);
  }

  @Provides
  @Singleton
  @NonNull
  static ApolloClientType provideApolloClientType(final @NonNull ApolloClient apolloClient) {
    return Secrets.IS_OSS ? new MockApolloClient() : new KSApolloClient(apolloClient);
  }
}
