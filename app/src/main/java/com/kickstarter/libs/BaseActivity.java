package com.kickstarter.libs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;

public class BaseActivity<PresenterType extends Presenter> extends AppCompatActivity {
  protected PresenterType presenter;
  private static final String PRESENTER_KEY = "presenter";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("onCreate %s", this.toString());

    fetchPresenter(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Timber.d("onResume %s", this.toString());

    fetchPresenter(null);
    if (presenter != null) {
      presenter.onResume(this);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    Timber.d("onPause %s", this.toString());

    if (presenter != null) {
      presenter.onPause();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Timber.d("onDestroy %s", this.toString());

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
    Timber.d("onSaveInstanceState %s", this.toString());

    Bundle presenterEnvelope = new Bundle();
    if (presenter != null) {
      Presenters.getInstance().save(presenter, presenterEnvelope);
    }

    outState.putBundle(PRESENTER_KEY, presenterEnvelope);
  }

  private final void fetchPresenter(Bundle presenterEnvelope) {
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
