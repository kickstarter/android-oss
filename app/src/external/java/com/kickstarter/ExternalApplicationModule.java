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
}
