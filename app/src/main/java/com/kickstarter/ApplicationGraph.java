package com.kickstarter;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.KoalaTrackingClient;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.kickstarter.services.gcm.MessageService;
import com.kickstarter.services.gcm.RegisterService;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.HelpActivity;
import com.kickstarter.ui.toolbars.DiscoveryToolbar;
import com.kickstarter.ui.views.AppRatingDialog;
import com.kickstarter.ui.views.IconTextView;
import com.kickstarter.ui.views.KSWebView;

public interface ApplicationGraph {
  Environment environment();
  void inject(ApplicationLifecycleUtil __);
  void inject(AppRatingDialog __);
  void inject(Koala __);
  void inject(DiscoveryActivity __);
  void inject(DiscoveryToolbar __);
  void inject(HelpActivity __);
  void inject(IconTextView __);
  void inject(KoalaTrackingClient __);
  void inject(KSWebView __);
  void inject(KSApplication __);
  void inject(MessageService __);
  void inject(KSCurrency __);
  void inject(RegisterService __);
}
