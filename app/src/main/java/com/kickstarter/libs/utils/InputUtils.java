package com.kickstarter.libs.utils;

import android.content.Context;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputUtils {
  private InputUtils() {}

  public static void hideKeyboard(final @NonNull Context context, final @Nullable View view) {
    final InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    final IBinder windowToken = view != null ? view.getWindowToken() : null;
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
  }
}
