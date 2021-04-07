package com.kickstarter;

import android.text.TextUtils;

import com.appboy.AppboyLifecycleCallbackListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.kickstarter.libs.utils.Secrets;

import org.joda.time.DateTime;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import timber.log.Timber;

public class KSApplication extends MultiDexApplication {
  private ApplicationComponent component;
  @Inject protected CookieManager cookieManager;
  @Inject protected PushNotifications pushNotifications;

  @Override
  @CallSuper
  public void onCreate() {
    super.onCreate();

    if (!isInUnitTests()) {
      initApplication();
    }
  }

  private void initApplication() {
    MultiDex.install(this);

    // Only log for internal builds
    if (BuildConfig.FLAVOR.equals("internal")) {
      Timber.plant(new Timber.DebugTree());
    }

    this.component = DaggerApplicationComponent.builder()
            .applicationModule(new ApplicationModule(this))
            .build();
    component().inject(this);

    if (FirebaseApp.getApps(getApplicationContext()).isEmpty()) {
      FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
      FirebaseApp.initializeApp(getApplicationContext());
      FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(true);
    }

    setVisitorCookie();
    this.pushNotifications.initialize();

    final ApplicationLifecycleUtil appUtil = new ApplicationLifecycleUtil(this);
    registerActivityLifecycleCallbacks(appUtil);
    registerComponentCallbacks(appUtil);

    // - Register lifecycle callback for Braze
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener(true, false));
  }

  public ApplicationComponent component() {
    return this.component;
  }

  /**
   * Method override in tha child class for testings purposes
   */
  public boolean isInUnitTests() {
    return false;
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
