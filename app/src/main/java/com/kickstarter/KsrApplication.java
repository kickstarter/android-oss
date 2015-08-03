package com.kickstarter;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import java.net.CookieHandler;
import java.net.CookieManager;

import javax.inject.Inject;

import timber.log.Timber;

public class KsrApplication extends Application {
  private ApplicationComponent component;
  private RefWatcher refWatcher;
  @Inject CookieManager cookieManager;

  @Override
  public void onCreate() {
    super.onCreate();

    // Log in debug mode, send to Hockey in production
    if (BuildConfig.DEBUG || isInUnitTests()) {
      Timber.plant(new Timber.DebugTree());
    } else {
      CrashManager.register(this, getResources().getString(R.string.hockey_app_id), new CrashManagerListener() {
        public boolean shouldAutoUploadCrashes() {
          return true;
        }
      });
    }

    if (!isInUnitTests()) {
      refWatcher = LeakCanary.install(this);
    }

    JodaTimeAndroid.init(this);

    component = DaggerApplicationComponent.builder()
      .applicationModule(new ApplicationModule(this))
      .build();
    component().inject(this);

    CookieHandler.setDefault(cookieManager);
  }

  public ApplicationComponent component() {
    return component;
  }

  public static RefWatcher getRefWatcher(final Context context) {
    final KsrApplication application = (KsrApplication) context.getApplicationContext();
    return application.refWatcher;
  }

  protected boolean isInUnitTests() {
    return false;
  }
}
