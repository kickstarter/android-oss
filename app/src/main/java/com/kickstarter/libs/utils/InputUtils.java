package com.kickstarter.libs.utils;

import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class InputUtils {
  private InputUtils() {}

  public static void hideKeyboard(final @NonNull Context context, final @Nullable View view) {
    final InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    final IBinder windowToken = view != null ? view.getWindowToken() : null;
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
  }
}
