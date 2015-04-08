package com.kickstarter;

import android.app.Application;

import com.kickstarter.views.IonIconTextView;

import javax.inject.Singleton;

import dagger.Component;

public class KsrApplication extends Application {
  @Singleton
  @Component(modules = KsrApplicationModule.class)
  public interface ApplicationComponent {
    void inject(KsrApplication application);
    void inject(DiscoveryActivity activity);
    void inject(IonIconTextView view);
  }

  private ApplicationComponent component;

  @Override public void onCreate() {
    super.onCreate();

    component = DaggerKsrApplication_ApplicationComponent.builder()
      .ksrApplicationModule(new KsrApplicationModule(this))
      .build();
    component().inject(this);
  }

  public ApplicationComponent component() {
    return component;
  }
}
