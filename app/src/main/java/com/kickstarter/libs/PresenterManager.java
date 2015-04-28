package com.kickstarter.libs;

import android.os.Bundle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.UUID;

public class PresenterManager {
  private static final String PRESENTER_ID_KEY = "presenter_id";
  private static final String PRESENTER_STATE_KEY = "presenter_state";

  private static PresenterManager instance = new PresenterManager();
  private BiMap<String, Presenter> presenters = HashBiMap.create();

  public static PresenterManager getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends Presenter> T fetch(Class<T> presenterClass, Bundle savedInstanceState) {
    String id = fetchId(savedInstanceState);
    Presenter presenter = presenters.get(id);

    if (presenter == null) {
      try {
        presenter = presenterClass.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      presenters.put(id, presenter);
      presenter.onCreate(savedInstanceState == null ? null : savedInstanceState.getBundle(PRESENTER_STATE_KEY));
    }

    return (T) presenter;
  }

  public void destroy(Presenter presenter) {
    presenter.onDestroy();
    presenters.inverse().remove(presenter);
  }

  public Bundle save(Presenter presenter) {
    // TODO: Refactor this snot
    Bundle bundle = new Bundle();
    bundle.putString(PRESENTER_ID_KEY, presenters.inverse().get(presenter));
    Bundle presenterState = new Bundle();
    presenter.onSave(presenterState);
    bundle.putBundle(PRESENTER_STATE_KEY, presenterState);
    return bundle;
  }

  protected String fetchId(Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(PRESENTER_ID_KEY) :
      UUID.randomUUID().toString();
  }
}
