package com.kickstarter.libs;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class BaseActivity<PresenterType extends Presenter> extends Activity {
  private PresenterType presenter;
  private static final String PRESENTER_KEY = "presenter";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    RequiresPresenter annotation = getClass().getAnnotation(RequiresPresenter.class);
    Class<PresenterType> presenterClass = annotation == null ? null : (Class<PresenterType>) annotation.value();
    if (presenterClass != null) {
      presenter = PresenterManager.getInstance().fetch(presenterClass,
        savedInstanceState == null ? null : savedInstanceState.getBundle(PRESENTER_KEY));
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (isFinishing()) {
      if (presenter != null) {
        PresenterManager.getInstance().destroy(presenter);
        presenter = null;
      }
    }
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    Bundle bundle = presenter == null ? null : PresenterManager.getInstance().save(presenter);
    outState.putBundle(PRESENTER_KEY, bundle);

  }
}
