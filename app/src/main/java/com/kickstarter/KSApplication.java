package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.FacebookSdk;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import java.net.CookieHandler;
import java.net.CookieManager;

import javax.inject.Inject;

import timber.log.Timber;

public class KSApplication extends Application {
  private ApplicationComponent component;
  private RefWatcher refWatcher;
  @Inject CookieManager cookieManager;

  @Override
  public void onCreate() {
    super.onCreate();

    // Only log for internal builds
    if (BuildConfig.FLAVOR.equals("internal")) {
      Timber.plant(new Timber.DebugTree());
    }

    // Send crash reports in release builds
    if (!BuildConfig.DEBUG && !isInUnitTests()) {
      checkForCrashes();
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

    FacebookSdk.sdkInitialize(this);
  }

  public ApplicationComponent component() {
    return component;
  }

  public static RefWatcher getRefWatcher(@NonNull final Context context) {
    final KSApplication application = (KSApplication) context.getApplicationContext();
    return application.refWatcher;
  }

  /**
   * Tests subclass KSApplication and override this method.
   */
  protected boolean isInUnitTests() {
    return false;
  }

  private void checkForCrashes() {
    CrashManager.register(this, getString(R.string.hockey_app_id), new CrashManagerListener() {
      public boolean shouldAutoUploadCrashes() {
        return true;
      }
    });
  }
}
