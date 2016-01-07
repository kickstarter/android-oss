package com.kickstarter.libs.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;

import javax.inject.Inject;

public final class ApplicationLifecycleUtil implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
  @Inject Koala koala;
  @Inject ApiClientType client;
  @Inject CurrentConfig config;

  private boolean isInBackground = true;

  public ApplicationLifecycleUtil(@NonNull final KSApplication application) {
    application.component().inject(this);
  }

  @Override
  public void onActivityCreated(@NonNull final Activity activity, @Nullable final Bundle bundle) {
  }

  @Override
  public void onActivityStarted(@NonNull final Activity activity) {
  }

  @Override
  public void onActivityResumed(@NonNull final Activity activity) {
    if(isInBackground){
      koala.trackAppOpen();

      // Facebook: logs 'install' and 'app activate' App Events.
      AppEventsLogger.activateApp(activity);

      // Refresh the config file
      this.client.config()
        .compose(Transformers.neverError())
        .subscribe(this.config::refresh);

      isInBackground = false;
    }
  }

  @Override
  public void onActivityPaused(@NonNull final Activity activity) {
    // Facebook: logs 'app deactivate' App Event.
    AppEventsLogger.deactivateApp(activity);
  }

  @Override
  public void onActivityStopped(@NonNull final Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull final Activity activity, @Nullable final Bundle bundle) {
  }

  @Override
  public void onActivityDestroyed(@NonNull final Activity activity) {
  }

  @Override
  public void onConfigurationChanged(@NonNull final Configuration configuration) {
  }

  @Override
  public void onLowMemory() {
    koala.trackMemoryWarning();
  }

  /**
   * Memory availability callback. TRIM_MEMORY_UI_HIDDEN means the app's UI is no longer visible.
   * This is triggered when the user navigates out of the app and primarily used to free resources used by the UI.
   * http://developer.android.com/training/articles/memory.html
   */
  @Override
  public void onTrimMemory(final int i) {
    if(i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
      koala.trackAppClose();
      isInBackground = true;
    }
  }
}
