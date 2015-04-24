package com.kickstarter;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.views.IonIconTextView;

import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Singleton;

import dagger.Component;
import timber.log.Timber;

public class KsrApplication extends Application {
  @Singleton
  @Component(modules = KsrApplicationModule.class)
  public interface ApplicationComponent {
    void inject(KsrApplication application);
    void inject(DiscoveryActivity activity);
    void inject(IonIconTextView view);
  }

  private ApplicationComponent component;

  @Override
  public void onCreate() {
    super.onCreate();

    // Log in debug mode, send to Crashlytics in production
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      Fabric.with(this, new Crashlytics());
    }

    JodaTimeAndroid.init(this);

    component = DaggerKsrApplication_ApplicationComponent.builder()
      .ksrApplicationModule(new KsrApplicationModule(this))
      .build();
    component().inject(this);
  }

  public ApplicationComponent component() {
    return component;
  }
}
