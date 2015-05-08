package com.kickstarter;

import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.views.IonIconTextView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
  void inject(KsrApplication application);
  void inject(DiscoveryActivity activity);
  void inject(IonIconTextView view);
}
