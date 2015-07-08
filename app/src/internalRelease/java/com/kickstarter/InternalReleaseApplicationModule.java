package com.kickstarter;

import com.kickstarter.libs.BuildCheck;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public class InternalReleaseApplicationModule {
  @Singleton
  @Provides
  ApplicationContainer provideApplicationContainer() {
    return ApplicationContainer.DEFAULT;
  }

  @Provides
  @Singleton
  ApiEndpoint provideApiEndpoint() {
    return ApiEndpoint.PRODUCTION;
  }

  @Provides
  BuildCheck provideBuildCheck() {
    return BuildCheck.DEFAULT;
  }
}
