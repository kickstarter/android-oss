package com.kickstarter;

import android.app.Application;
import android.content.Context;

import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.views.IonIconTextView;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import javax.inject.Singleton;

import dagger.Component;
import timber.log.Timber;

public class KsrApplication extends Application {
  private ApplicationComponent component;
  private RefWatcher refWatcher;

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

    refWatcher = LeakCanary.install(this);
    JodaTimeAndroid.init(this);

    component = DaggerApplicationComponent.builder()
      .applicationModule(new ApplicationModule(this))
      .build();
  }

  public ApplicationComponent component() {
    return component;
  }

  public static RefWatcher getRefWatcher(final Context context) {
    KsrApplication application = (KsrApplication) context.getApplicationContext();
    return application.refWatcher;
  }
}
