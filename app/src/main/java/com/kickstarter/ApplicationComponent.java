package com.kickstarter;

import com.kickstarter.models.CurrentUser;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.presenters.LoginPresenter;
import com.kickstarter.presenters.ProjectDetailPresenter;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.views.DiscoveryToolbar;
import com.kickstarter.ui.views.IonIconTextView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
  void inject(CurrentUser current_user);
  void inject(ActivityFeedActivity activity);
  void inject(DiscoveryActivity activity);
  void inject(DiscoveryToolbar toolbar);
  void inject(DiscoveryPresenter presenter);
  void inject(IonIconTextView view);
  void inject(KsrApplication application);
  void inject(LoginPresenter presenter);
  void inject(ProjectDetailPresenter presenter);
}
