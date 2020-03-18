package com.kickstarter;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.kickstarter.services.KoalaWorker;
import com.kickstarter.services.LakeWorker;
import com.kickstarter.services.firebase.MessageService;
import com.kickstarter.services.firebase.RegisterService;
import com.kickstarter.ui.views.AppRatingDialog;
import com.kickstarter.ui.views.IconTextView;
import com.kickstarter.ui.views.KSWebView;

public interface ApplicationGraph {
  Environment environment();
  void inject(ApplicationLifecycleUtil __);
  void inject(AppRatingDialog __);
  void inject(IconTextView __);
  void inject(KoalaWorker __);
  void inject(KSWebView __);
  void inject(KSApplication __);
  void inject(LakeWorker __);
  void inject(MessageService __);
  void inject(RegisterService __);
}
