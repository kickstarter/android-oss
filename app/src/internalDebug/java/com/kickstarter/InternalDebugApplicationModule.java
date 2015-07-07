package com.kickstarter;

import com.kickstarter.libs.BuildCheck;
import com.kickstarter.ui.containers.ApplicationContainer;
import com.kickstarter.ui.containers.InternalDebugApplicationContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public class InternalDebugApplicationModule {
  @Singleton
  @Provides
  ApplicationContainer provideApplicationContainer() {
    return new InternalDebugApplicationContainer();
  }

  @Provides
  BuildCheck provideBuildCheck() {
    return BuildCheck.DEFAULT;
  }
}
