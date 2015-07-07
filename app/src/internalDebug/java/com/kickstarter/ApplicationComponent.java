package com.kickstarter;

import com.kickstarter.ui.views.DebugView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {InternalDebugApplicationModule.class})
public interface ApplicationComponent extends ApplicationGraph {
  void inject(DebugView view);
}
