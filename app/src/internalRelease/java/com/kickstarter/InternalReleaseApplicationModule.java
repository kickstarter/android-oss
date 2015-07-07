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
  BuildCheck provideBuildCheck() {
    return BuildCheck.DEFAULT;
  }
}
