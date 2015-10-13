package com.kickstarter;

import com.kickstarter.ui.views.DebugDrawer;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {InternalReleaseApplicationModule.class})
public interface ApplicationComponent extends ApplicationGraph {
  void inject(DebugDrawer __);
}
