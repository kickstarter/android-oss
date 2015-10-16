package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.BundleUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Presenters {
  private static final String PRESENTER_ID_KEY = "presenter_id";
  private static final String PRESENTER_STATE_KEY = "presenter_state";

  private static final Presenters instance = new Presenters();
  private HashMap<String, Presenter> presenters = new HashMap<>();

  public static Presenters getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends Presenter> T fetch(@NonNull final Context context, @NonNull final Class<T> presenterClass,
    @Nullable final Bundle savedInstanceState) {
    final String id = fetchId(savedInstanceState);
    Presenter presenter = presenters.get(id);

    if (presenter == null) {
      try {
        presenter = presenterClass.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      presenters.put(id, presenter);
      presenter.onCreate(context, BundleUtils.maybeGetBundle(savedInstanceState, PRESENTER_STATE_KEY));
    }

    return (T) presenter;
  }

  public void destroy(@NonNull final Presenter presenter) {
    presenter.onDestroy();

    Iterator<Map.Entry<String, Presenter>> iterator = presenters.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, Presenter> entry = iterator.next();
      if (presenter.equals(entry.getValue())) {
        iterator.remove();
      }
    }
  }

  public void save(@NonNull final Presenter presenter, @NonNull final Bundle envelope) {
    envelope.putString(PRESENTER_ID_KEY, findIdForPresenter(presenter));

    final Bundle state = new Bundle();
    presenter.save(state);
    envelope.putBundle(PRESENTER_STATE_KEY, state);
  }

  private String fetchId(@Nullable final Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(PRESENTER_ID_KEY) :
      UUID.randomUUID().toString();
  }

  private String findIdForPresenter(@NonNull final Presenter presenter) {
    for (final Map.Entry<String, Presenter> entry : presenters.entrySet()) {
      if (presenter.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    throw new RuntimeException("Cannot find presenter in map!");
  }
}
