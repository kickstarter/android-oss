package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.utils.BundleUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityViewModelManager {
  private static final String VIEW_MODEL_ID_KEY = "view_model_id";
  private static final String VIEW_MODEL_STATE_KEY = "view_model_state";

  private static final ActivityViewModelManager instance = new ActivityViewModelManager();
  private Map<String, ActivityViewModel> viewModels = new HashMap<>();

  public static @NonNull ActivityViewModelManager getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends ActivityViewModel> T fetch(final @NonNull Context context, final @NonNull Class<T> viewModelClass,
    final @Nullable Bundle savedInstanceState) {
    final String id = fetchId(savedInstanceState);
    ActivityViewModel activityViewModel = this.viewModels.get(id);

    if (activityViewModel == null) {
      activityViewModel = create(context, viewModelClass, savedInstanceState, id);
    }

    return (T) activityViewModel;
  }

  public void destroy(final @NonNull ActivityViewModel activityViewModel) {
    activityViewModel.onDestroy();

    final Iterator<Map.Entry<String, ActivityViewModel>> iterator = this.viewModels.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, ActivityViewModel> entry = iterator.next();
      if (activityViewModel.equals(entry.getValue())) {
        iterator.remove();
      }
    }
  }

  public void save(final @NonNull ActivityViewModel activityViewModel, final @NonNull Bundle envelope) {
    envelope.putString(VIEW_MODEL_ID_KEY, findIdForViewModel(activityViewModel));

    final Bundle state = new Bundle();
    envelope.putBundle(VIEW_MODEL_STATE_KEY, state);
  }

  private <T extends ActivityViewModel> ActivityViewModel create(final @NonNull Context context, final @NonNull Class<T> viewModelClass,
    final @Nullable Bundle savedInstanceState, final @NonNull String id) {

    final KSApplication application = (KSApplication) context.getApplicationContext();
    final Environment environment = application.component().environment();
    final ActivityViewModel activityViewModel;

    try {
      final Constructor constructor = viewModelClass.getConstructor(Environment.class);
      activityViewModel = (ActivityViewModel) constructor.newInstance(environment);

      // Need to catch these exceptions separately, otherwise the compiler turns them into `ReflectiveOperationException`.
      // That exception is only available in API19+
    } catch (IllegalAccessException exception) {
      throw new RuntimeException(exception);
    } catch (InvocationTargetException exception) {
      throw new RuntimeException(exception);
    } catch (InstantiationException exception) {
      throw new RuntimeException(exception);
    } catch (NoSuchMethodException exception) {
      throw new RuntimeException(exception);
    }

    this.viewModels.put(id, activityViewModel);
    activityViewModel.onCreate(context, BundleUtils.maybeGetBundle(savedInstanceState, VIEW_MODEL_STATE_KEY));

    return activityViewModel;
  }

  private String fetchId(final @Nullable Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(VIEW_MODEL_ID_KEY) :
      UUID.randomUUID().toString();
  }

  private String findIdForViewModel(final @NonNull ActivityViewModel activityViewModel) {
    for (final Map.Entry<String, ActivityViewModel> entry : this.viewModels.entrySet()) {
      if (activityViewModel.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    throw new RuntimeException("Cannot find view model in map!");
  }
}
