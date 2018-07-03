package com.kickstarter;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.kickstarter.libs.utils.Secrets;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import org.joda.time.DateTime;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.UUID;

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
      this.refWatcher = LeakCanary.install(this);
    }

    JodaTimeAndroid.init(this);

    this.component = DaggerApplicationComponent.builder()
      .applicationModule(new ApplicationModule(this))
      .build();
    component().inject(this);

    if (!isInUnitTests()) {
      setVisitorCookie();
    }

    FacebookSdk.sdkInitialize(this);

    this.pushNotifications.initialize();

    final ApplicationLifecycleUtil appUtil = new ApplicationLifecycleUtil(this);
    registerActivityLifecycleCallbacks(appUtil);
    registerComponentCallbacks(appUtil);
  }

  public ApplicationComponent component() {
    return this.component;
  }

  public static RefWatcher getRefWatcher(final @NonNull Context context) {
    final KSApplication application = (KSApplication) context.getApplicationContext();
    return application.refWatcher;
  }

  public boolean isInUnitTests() {
    return false;
  }

  private void checkForCrashes() {
    final String appId = Build.isExternal()
      ? Secrets.HockeyAppId.EXTERNAL
      : Secrets.HockeyAppId.INTERNAL;

    CrashManager.register(this, appId, new CrashManagerListener() {
      public boolean shouldAutoUploadCrashes() {
        return true;
      }
    });
  }


  private void setVisitorCookie() {
    final String deviceId = FirebaseInstanceId.getInstance().getId();
    final String uniqueIdentifier = TextUtils.isEmpty(deviceId) ? UUID.randomUUID().toString() : deviceId;
    final HttpCookie cookie = new HttpCookie("vis", uniqueIdentifier);
    cookie.setMaxAge(DateTime.now().plusYears(100).getMillis());
    cookie.setSecure(true);
    final URI webUri = URI.create(Secrets.WebEndpoint.PRODUCTION);
    final URI apiUri = URI.create(ApiEndpoint.PRODUCTION.url());
    this.cookieManager.getCookieStore().add(webUri, cookie);
    this.cookieManager.getCookieStore().add(apiUri, cookie);
    CookieHandler.setDefault(this.cookieManager);
  }
}
