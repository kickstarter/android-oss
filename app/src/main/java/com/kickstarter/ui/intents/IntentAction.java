package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.subjects.PublishSubject;

/**
 * A class that can be configured to inspect the data in an intent, and output data that the
 * activity/viewmodel can use to initialize itself. Activities should create instances of this
 * class and feed it any intent it encounters via the `intent()` method.
 */
public abstract class IntentAction {
  protected final PublishSubject<Intent> intent = PublishSubject.create();

  public void intent(final @NonNull Intent intent) {
    this.intent.onNext(intent);
  }

  /**
   * Checks if an intent has any data or extras.
   */
  protected boolean isEmpty(final @NonNull Intent intent) {
    return intent.getData() == null && intent.getExtras() == null;
  }

  /**
   * Extracts the data string from an intent.
   */
  protected @Nullable Uri uri(final @NonNull Intent intent) {
    final String string = intent.getDataString();
    if (string == null) {
      return null;
    }

    return Uri.parse(string);
  }

  protected static @Nullable Uri intentUri(final @NonNull Intent intent) {
    final String string = intent.getDataString();
    if (string == null) {
      return null;
    }

    return Uri.parse(string);
  }
}
