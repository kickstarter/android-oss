package com.kickstarter;

import com.kickstarter.ui.activities.FeatureFlagsActivity;
import com.kickstarter.ui.activities.InternalToolsActivity;
import com.kickstarter.ui.views.DebugPushNotificationsView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {InternalApplicationModule.class})
public interface ApplicationComponent extends ApplicationGraph {
  void inject(DebugPushNotificationsView __);
  void inject(InternalToolsActivity __);
  void inject(FeatureFlagsActivity __);
}
