package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel;
import com.kickstarter.libs.utils.BundleUtils;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.components.FragmentLifecycleProvider;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class BaseFragment<ViewModelType extends FragmentViewModel> extends Fragment implements FragmentLifecycleProvider,
  FragmentLifecycleType {

  private final BehaviorSubject<FragmentEvent> lifecycle = BehaviorSubject.create();
  private static final String VIEW_MODEL_KEY = "FragmentViewModel";
  protected ViewModelType viewModel;

  /**
   * Returns an observable of the fragment's lifecycle events.
   */
  @Override
  public final @NonNull Observable<FragmentEvent> lifecycle() {
    return lifecycle.asObservable();
  }

  /**
   * Completes an observable when an {@link FragmentEvent} occurs in the fragment's lifecycle.
   */
  @Override
  public final @NonNull <T> Observable.Transformer<T, T> bindUntilEvent(final @NonNull FragmentEvent event) {
    return RxLifecycle.bindUntilFragmentEvent(lifecycle, event);
  }

  /**
   * Completes an observable when the lifecycle event opposing the current lifecyle event is emitted.
   * For example, if a subscription is made during {@link FragmentEvent#CREATE}, the observable will be completed
   * in {@link FragmentEvent#DESTROY}.
   */
  @Override
  public final @NonNull <T> Observable.Transformer<T, T> bindToLifecycle() {
    return RxLifecycle.bindFragment(lifecycle);
  }

  /**
   * Called before `onCreate`, when a fragment is attached to its context.
   */
  @CallSuper
  @Override
  public void onAttach(final @NonNull Context context) {
    super.onAttach(context);
    Timber.d("onAttach %s", this.toString());
    lifecycle.onNext(FragmentEvent.ATTACH);
  }

  @CallSuper
  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("onCreate %s", this.toString());

    lifecycle.onNext(FragmentEvent.CREATE);

    assignViewModel(savedInstanceState);

    viewModel.arguments(getArguments());
  }

  /**
   * Called when a fragment instantiates its user interface view, between `onCreate` and `onActivityCreated`.
   * Can return null for non-graphical fragments.
   */
  @CallSuper
  @Override
  public @Nullable View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container,
    final @Nullable Bundle savedInstanceState) {
    final View view = super.onCreateView(inflater, container, savedInstanceState);
    Timber.d("onCreateView %s", this.toString());
    lifecycle.onNext(FragmentEvent.CREATE_VIEW);
    return view;
  }

  @CallSuper
  @Override
  public void onStart() {
    super.onStart();
    Timber.d("onStart %s", this.toString());
    lifecycle.onNext(FragmentEvent.START);
  }

  @CallSuper
  @Override
  public void onResume() {
    super.onResume();
    Timber.d("onResume %s", this.toString());
    lifecycle.onNext(FragmentEvent.RESUME);

    assignViewModel(null);
    if (viewModel != null) {
      viewModel.onResume(this);
    }
  }

  @CallSuper
  @Override
  public void onPause() {
    lifecycle.onNext(FragmentEvent.PAUSE);
    super.onPause();
    Timber.d("onPause %s", this.toString());

    if (viewModel != null) {
      viewModel.onPause();
    }
  }

  @CallSuper
  @Override
  public void onStop() {
    lifecycle.onNext(FragmentEvent.STOP);
    super.onStop();
    Timber.d("onStop %s", this.toString());
  }

  /**
   * Called when the view created by `onCreateView` has been detached from the fragment.
   * The lifecycle subject must be pinged before it is destroyed by the fragment.
   */
  @CallSuper
  @Override
  public void onDestroyView() {
    lifecycle.onNext(FragmentEvent.DESTROY_VIEW);
    super.onDestroyView();
  }

  @CallSuper
  @Override
  public void onDestroy() {
    lifecycle.onNext(FragmentEvent.DESTROY);
    super.onDestroy();
    Timber.d("onDestroy %s", this.toString());

    if (viewModel != null) {
      viewModel.onDestroy();
    }
  }

  /**
   * Called after `onDestroy` when the fragment is no longer attached to its activity.
   */
  @CallSuper
  @Override
  public void onDetach() {
    Timber.d("onDetach %s", this.toString());
    super.onDetach();

    if (getActivity().isFinishing()) {
      if (viewModel != null) {
        // Order of the next two lines is important: the lifecycle should update before we
        // complete the view publish subject in the view model.
        lifecycle.onNext(FragmentEvent.DETACH);
        viewModel.onDetach();

        FragmentViewModelManager.getInstance().destroy(viewModel);
        viewModel = null;
      }
    }
  }

  @CallSuper
  @Override
  public void onSaveInstanceState(final @NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    final Bundle viewModelEnvelope = new Bundle();
    if (viewModel != null) {
      FragmentViewModelManager.getInstance().save(viewModel, viewModelEnvelope);
    }

    outState.putBundle(VIEW_MODEL_KEY, viewModelEnvelope);
  }

  private void assignViewModel(final @Nullable Bundle viewModelEnvelope) {
    if (viewModel == null) {
      final RequiresFragmentViewModel annotation = getClass().getAnnotation(RequiresFragmentViewModel.class);
      final Class<ViewModelType> viewModelClass = annotation == null ? null : (Class<ViewModelType>) annotation.value();
      if (viewModelClass != null) {
        viewModel = FragmentViewModelManager.getInstance().fetch(getContext(),
          viewModelClass,
          BundleUtils.maybeGetBundle(viewModelEnvelope, VIEW_MODEL_KEY));
      }
    }
  }
}
