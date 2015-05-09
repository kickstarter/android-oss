package com.kickstarter;

import android.app.Activity;

import com.kickstarter.presenters.LoginPresenter;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.views.DiscoveryToolbar;
import com.kickstarter.ui.views.IonIconTextView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
  void inject(KsrApplication application);
  void inject(ActivityFeedActivity activity);
  void inject(DiscoveryActivity activity);
  void inject(DiscoveryToolbar toolbar);
  void inject(IonIconTextView view);
  void inject(LoginPresenter presenter);
}
