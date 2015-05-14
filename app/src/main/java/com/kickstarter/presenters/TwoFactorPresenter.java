package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.libs.Presenter;
import com.kickstarter.ui.activities.TwoFactorActivity;

import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.subjects.PublishSubject;

public class TwoFactorPresenter extends Presenter<TwoFactorActivity> {
  private final PublishSubject<Void> submit = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    final Observable<OnTextChangeEvent> code = viewSubject
      .filter(v -> v != null)
      .flatMap(v -> WidgetObservable.text(v.code));

    final Observable<Boolean> isValid = code
      .map(c -> TwoFactorPresenter.isValid(c.text().toString()));

    subscribeTo(isValid, valid -> view().setSubmitEnabled(valid));
  }

  private static boolean isValid(String code) {
    return code.length() > 0; // TODO: Should this be >= 4?
  }

  public void submit() {
  }
}
