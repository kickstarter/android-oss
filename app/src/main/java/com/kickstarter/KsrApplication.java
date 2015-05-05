package com.kickstarter;

import android.app.Application;

import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.views.IonIconTextView;

import net.danlew.android.joda.JodaTimeAndroid;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

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

    // Log in debug mode, send to Hockey in production
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      CrashManager.register(this, getResources().getString(R.string.hockey_app_id), new CrashManagerListener() {
        public boolean shouldAutoUploadCrashes() {
          return true;
        }
      });
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
