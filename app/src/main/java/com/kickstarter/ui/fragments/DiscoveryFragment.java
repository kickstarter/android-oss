package com.kickstarter.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseFragment;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.ArgumentsKey;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.activities.WebViewActivity;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.DiscoveryFragmentViewModel;

import java.util.List;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

@RequiresFragmentViewModel(DiscoveryFragmentViewModel.class)
public final class DiscoveryFragment extends BaseFragment<DiscoveryFragmentViewModel> {
  private RecyclerView recyclerView;
  private RecyclerViewPaginator recyclerViewPaginator;

  public DiscoveryFragment() {}

  @NonNull
  public static DiscoveryFragment newInstance(final int position) {
    final DiscoveryFragment fragment = new DiscoveryFragment();
    final Bundle bundle = new Bundle();
    bundle.putInt(ArgumentsKey.DISCOVERY_SORT_POSITION, position);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container,
    final @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    recyclerView = (RecyclerView) inflater.inflate(R.layout.discovery_recycler_view, container, false);
    final DiscoveryAdapter adapter = new DiscoveryAdapter(viewModel.inputs);
    recyclerView.setAdapter(adapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerViewPaginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);

    viewModel.outputs.activity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::takeActivity);

    viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::takeProjects);

    viewModel.outputs.shouldShowOnboardingView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::setShouldShowOnboardingView);

    viewModel.outputs.showActivityFeed()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> startActivityFeedActivity());

    viewModel.outputs.showActivityUpdate()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startActivityUpdateActivity);

    viewModel.outputs.showProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));

    viewModel.outputs.showLoginTout()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startLoginToutActivity());

    return recyclerView;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    if (recyclerView != null) {
      recyclerView.setAdapter(null);
    }

    if (recyclerViewPaginator != null) {
      recyclerViewPaginator.stop();
    }
  }

  private void startActivityUpdateActivity(final @NonNull Activity activity) {
    final Intent intent = new Intent(getActivity(), WebViewActivity.class)
      .putExtra(IntentKey.URL, activity.projectUpdateUrl());
    startActivity(intent);
    transition(getActivity(), slideInFromRight());
  }

  private void startActivityFeedActivity() {
    startActivity(new Intent(getActivity(), ActivityFeedActivity.class));
  }

  private void startLoginToutActivity() {
    final Intent intent = new Intent(getActivity(), LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(getActivity(), slideInFromRight());
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(getActivity(), ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    startActivity(intent);
    transition(getActivity(), slideInFromRight());
  }

  public void takeCategories(final @NonNull List<Category> categories) {
    viewModel.inputs.rootCategories(categories);
  }

  public void updateParams(final @NonNull DiscoveryParams params) {
    viewModel.inputs.paramsFromActivity(params);
  }

  public void clearPage() {
    viewModel.inputs.clearPage();
  }
}
