package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.utils.BundleUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ViewModels {
  private static final String VIEW_MODEL_ID_KEY = "view_model_id";
  private static final String VIEW_MODEL_STATE_KEY = "view_model_state";

  private static final ViewModels instance = new ViewModels();
  private Map<String, ViewModel> viewModels = new HashMap<>();

  public static ViewModels getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T fetch(final @NonNull Context context, final @NonNull Class<T> viewModelClass,
    final @Nullable Bundle savedInstanceState) {
    final String id = fetchId(savedInstanceState);
    ViewModel viewModel = viewModels.get(id);

    if (viewModel == null) {
      viewModel = create(context, viewModelClass, savedInstanceState, id);
    }

    return (T) viewModel;
  }

  public void destroy(final @NonNull ViewModel viewModel) {
    viewModel.onDestroy();

    final Iterator<Map.Entry<String, ViewModel>> iterator = viewModels.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, ViewModel> entry = iterator.next();
      if (viewModel.equals(entry.getValue())) {
        iterator.remove();
      }
    }
  }

  public void save(final @NonNull ViewModel viewModel, final @NonNull Bundle envelope) {
    envelope.putString(VIEW_MODEL_ID_KEY, findIdForViewModel(viewModel));

    final Bundle state = new Bundle();
    viewModel.save(state);
    envelope.putBundle(VIEW_MODEL_STATE_KEY, state);
  }

  private <T extends ViewModel> ViewModel create(final @NonNull Context context, final @NonNull Class<T> viewModelClass,
    final @Nullable Bundle savedInstanceState, final @NonNull String id) {

    final KSApplication application = (KSApplication) context.getApplicationContext();
    final Environment environment = application.component().environment();
    final ViewModel viewModel;

    try {
      final Constructor constructor = viewModelClass.getConstructor(Environment.class);
      viewModel = (ViewModel) constructor.newInstance(environment);
    } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException exception) {
      throw new RuntimeException(exception);
    }

    viewModels.put(id, viewModel);
    viewModel.onCreate(context, BundleUtils.maybeGetBundle(savedInstanceState, VIEW_MODEL_STATE_KEY));

    return viewModel;
  }

  private String fetchId(final @Nullable Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(VIEW_MODEL_ID_KEY) :
      UUID.randomUUID().toString();
  }

  private String findIdForViewModel(final @NonNull ViewModel viewModel) {
    for (final Map.Entry<String, ViewModel> entry : viewModels.entrySet()) {
      if (viewModel.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    throw new RuntimeException("Cannot find view model in map!");
  }
}
