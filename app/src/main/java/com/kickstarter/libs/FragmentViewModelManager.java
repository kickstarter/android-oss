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

public final class FragmentViewModelManager {
  private static final String VIEW_MODEL_ID_KEY = "fragment_view_model_id";
  private static final String VIEW_MODEL_STATE_KEY = "fragment_view_model_state";

  private static final FragmentViewModelManager instance = new FragmentViewModelManager();
  private Map<String, FragmentViewModel> viewModels = new HashMap<>();

  public static @NonNull FragmentViewModelManager getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends FragmentViewModel> T fetch(final @NonNull Context context, final @NonNull Class<T> viewModelClass,
    final @Nullable Bundle savedInstanceState) {
    final String id = fetchId(savedInstanceState);
    FragmentViewModel viewModel = this.viewModels.get(id);

    if (viewModel == null) {
      viewModel = create(context, viewModelClass, savedInstanceState, id);
    }

    return (T) viewModel;
  }

  public void destroy(final @NonNull FragmentViewModel viewModel) {
    viewModel.onDestroy();

    final Iterator<Map.Entry<String, FragmentViewModel>> iterator = this.viewModels.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, FragmentViewModel> entry = iterator.next();
      if (viewModel.equals(entry.getValue())) {
        iterator.remove();
      }
    }
  }

  public void save(final @NonNull FragmentViewModel viewModel, final @NonNull Bundle envelope) {
    envelope.putString(VIEW_MODEL_ID_KEY, findIdForViewModel(viewModel));

    final Bundle state = new Bundle();
    envelope.putBundle(VIEW_MODEL_STATE_KEY, state);
  }

  private <T extends FragmentViewModel> FragmentViewModel create(final @NonNull Context context, final @NonNull Class<T> viewModelClass,
    final @Nullable Bundle savedInstanceState, final @NonNull String id) {

    final KSApplication application = (KSApplication) context.getApplicationContext();
    final Environment environment = application.component().environment();
    final FragmentViewModel viewModel;

    try {
      final Constructor constructor = viewModelClass.getConstructor(Environment.class);
      viewModel = (FragmentViewModel) constructor.newInstance(environment);
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

    this.viewModels.put(id, viewModel);
    viewModel.onCreate(context, BundleUtils.maybeGetBundle(savedInstanceState, VIEW_MODEL_STATE_KEY));

    return viewModel;
  }

  private String fetchId(final @Nullable Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(VIEW_MODEL_ID_KEY) :
      UUID.randomUUID().toString();
  }

  private String findIdForViewModel(final @NonNull FragmentViewModel viewModel) {
    for (final Map.Entry<String, FragmentViewModel> entry : this.viewModels.entrySet()) {
      if (viewModel.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    throw new RuntimeException("Cannot find view model in map!");
  }
}
