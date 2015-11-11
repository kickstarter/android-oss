package com.kickstarter.libs.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

public class ApplicationLifecycleUtil implements  Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
  private static final String TAG = ApplicationLifecycleUtil.class.getSimpleName();
  private static boolean isInBackground = false;

  @Override
  public void onActivityCreated(Activity activity, Bundle bundle) {
  }

  @Override
  public void onActivityStarted(Activity activity) {
  }

  @Override
  public void onActivityResumed(Activity activity) {
    if(isInBackground){
      Log.d(TAG, "app went to foreground");
      isInBackground = false;
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
  }

  @Override
  public void onActivityStopped(Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
  }

  @Override
  public void onConfigurationChanged(Configuration configuration) {
  }

  @Override
  public void onLowMemory() {
  }

  @Override
  public void onTrimMemory(int i) {
    if(i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN){
      Log.d(TAG, "app went to background");
      isInBackground = true;
    }
  }
}
