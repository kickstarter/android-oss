package com.kickstarter;

import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.kickstarter.libs.utils.Secrets;

import net.danlew.android.joda.JodaTimeAndroid;

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
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class KSApplication extends MultiDexApplication {
  private ApplicationComponent component;
  @Inject protected CookieManager cookieManager;
  @Inject protected PushNotifications pushNotifications;

  @Override
  @CallSuper
  public void onCreate() {
    super.onCreate();

    MultiDex.install(this);

    // Only log for internal builds
    if (BuildConfig.FLAVOR.equals("internal")) {
      Timber.plant(new Timber.DebugTree());
    }

    JodaTimeAndroid.init(this);
    Fabric.with(this, new Crashlytics());

    this.component = DaggerApplicationComponent.builder()
      .applicationModule(new ApplicationModule(this))
      .build();
    component().inject(this);

    FirebaseApp.initializeApp(this);
    FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);

    if (!isInUnitTests()) {
      setVisitorCookie();
    }

    this.pushNotifications.initialize();

    final ApplicationLifecycleUtil appUtil = new ApplicationLifecycleUtil(this);
    registerActivityLifecycleCallbacks(appUtil);
    registerComponentCallbacks(appUtil);
  }

  public ApplicationComponent component() {
    return this.component;
  }

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
