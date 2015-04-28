package com.kickstarter.libs;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity<PresenterType extends Presenter> extends AppCompatActivity {
  protected PresenterType presenter;
  private static final String PRESENTER_KEY = "presenter";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fetchPresenter(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();

    fetchPresenter(null);
    if (presenter != null) {
      presenter.onResume(this);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (presenter != null) {
      presenter.onPause();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (isFinishing()) {
      if (presenter != null) {
        Presenters.getInstance().destroy(presenter);
        presenter = null;
      }
    }
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    Bundle presenterEnvelope = new Bundle();
    if (presenter != null) {
      Presenters.getInstance().save(presenter, presenterEnvelope);
    }

    outState.putBundle(PRESENTER_KEY, presenterEnvelope);
  }

  private void fetchPresenter(Bundle presenterEnvelope) {
    if (presenter == null) {
      RequiresPresenter annotation = getClass().getAnnotation(RequiresPresenter.class);
      Class<PresenterType> presenterClass = annotation == null ? null : (Class<PresenterType>) annotation.value();
      if (presenterClass != null) {
        presenter = Presenters.getInstance().fetch(presenterClass,
          presenterEnvelope == null ? null : presenterEnvelope.getBundle(PRESENTER_KEY));
      }
    }
  }
}
