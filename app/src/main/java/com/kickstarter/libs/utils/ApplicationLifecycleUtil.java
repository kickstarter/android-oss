package com.kickstarter.libs.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kickstarter.libs.Koala;

import javax.inject.Inject;

import timber.log.Timber;

public final class ApplicationLifecycleUtil implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
  @Inject Koala koala;

  private static boolean isInBackground = false;

  @Override
  public void onActivityCreated(@NonNull final Activity activity, @NonNull final Bundle bundle) {
  }

  @Override
  public void onActivityStarted(@NonNull final Activity activity) {
  }

  @Override
  public void onActivityResumed(@NonNull final Activity activity) {
    if(isInBackground){
      Timber.d("App went to foreground");
      isInBackground = false;
      koala.trackAppOpen();
    }
  }

  @Override
  public void onActivityPaused(@NonNull final Activity activity) {
  }

  @Override
  public void onActivityStopped(@NonNull final Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull final Activity activity, Bundle bundle) {
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

  @Override
  public void onTrimMemory(final int i) {
    if(i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
      Timber.d("App went to background");
      koala.trackAppClose();
    }
  }
}
