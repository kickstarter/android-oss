package com.kickstarter;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import java.net.CookieHandler;
import java.net.CookieManager;

import javax.inject.Inject;

import timber.log.Timber;

public class KSApplication extends MultiDexApplication {
  private ApplicationComponent component;
  private RefWatcher refWatcher;
  @Inject protected CookieManager cookieManager;
  @Inject protected PushNotifications pushNotifications;

  @Override
  @CallSuper
  public void onCreate() {
    super.onCreate();

    // Send crash reports in release builds
    if (!BuildConfig.DEBUG && !isInUnitTests()) {
      checkForCrashes();
    }

    MultiDex.install(this);

    // Only log for internal builds
    if (BuildConfig.FLAVOR_AUDIENCE.equals("internal")) {
      Timber.plant(new Timber.DebugTree());
    }

    if (!isInUnitTests() && ApiCapabilities.canDetectMemoryLeaks()) {
      refWatcher = LeakCanary.install(this);
    }

    JodaTimeAndroid.init(this);

    component = DaggerApplicationComponent.builder()
      .applicationModule(new ApplicationModule(this))
      .build();
    component().inject(this);

    CookieHandler.setDefault(cookieManager);

    FacebookSdk.sdkInitialize(this);

    pushNotifications.initialize();

    final ApplicationLifecycleUtil appUtil = new ApplicationLifecycleUtil(this);
    registerActivityLifecycleCallbacks(appUtil);
    registerComponentCallbacks(appUtil);
  }

  public ApplicationComponent component() {
    return component;
  }

  public static RefWatcher getRefWatcher(final @NonNull Context context) {
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
