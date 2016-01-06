package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.subjects.PublishSubject;

public abstract class IntentAction {
  protected final PublishSubject<Intent> intent = PublishSubject.create();

  public void intent(final @NonNull Intent intent) {
    this.intent.onNext(intent);
  }

  protected @Nullable Uri uri(final @NonNull Intent intent) {
    final String string = intent.getDataString();
    if (string == null) {
      return null;
    }

    return Uri.parse(string);
  }
}
