package com.kickstarter;

import com.kickstarter.libs.BuildCheck;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public class ExternalReleaseApplicationModule {
  @Provides
  BuildCheck provideBuildCheck() {
    return new NoopBuildCheck();
  }
}
