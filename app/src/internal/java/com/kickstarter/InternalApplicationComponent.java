package com.kickstarter;

import com.kickstarter.ui.views.DebugDrawer;
import com.kickstarter.ui.views.DebugPushNotificationsView;

public interface InternalApplicationComponent extends ApplicationGraph {
  void inject(DebugDrawer __);
  void inject(DebugPushNotificationsView __);
}
