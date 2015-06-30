package com.kickstarter;

import com.kickstarter.libs.BuildCheck;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public class ExternalDebugApplicationModule {
  @Provides
  BuildCheck provideBuildCheck() {
    return BuildCheck.DEFAULT;
  }
}
