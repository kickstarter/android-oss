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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseFragment;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.ArgumentsKey;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.activities.UpdateActivity;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.DiscoveryFragmentViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

@RequiresFragmentViewModel(DiscoveryFragmentViewModel.ViewModel.class)
public final class DiscoveryFragment extends BaseFragment<DiscoveryFragmentViewModel.ViewModel> {
  private RecyclerViewPaginator recyclerViewPaginator;

  protected @Bind(R.id.discovery_empty_heart_filled) ImageView heartFilled;
  protected @Bind(R.id.discovery_empty_heart_outline) ImageView heartOutline;
  protected @Bind(R.id.discovery_empty_view) View emptyView;
  protected @Bind(R.id.discovery_recycler_view) RecyclerView recyclerView;

  public DiscoveryFragment() {}

  public static @NonNull DiscoveryFragment newInstance(final int position) {
    final DiscoveryFragment fragment = new DiscoveryFragment();
    final Bundle bundle = new Bundle();
    bundle.putInt(ArgumentsKey.DISCOVERY_SORT_POSITION, position);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public @Nullable View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container,
    final @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    final View view = inflater.inflate(R.layout.discovery_recycler_view, container, false);
    ButterKnife.bind(this, view);

    final DiscoveryAdapter adapter = new DiscoveryAdapter(this.viewModel.inputs);
    this.recyclerView.setAdapter(adapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this.recyclerView.getContext());
    this.recyclerView.setLayoutManager(layoutManager);
    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage);

    this.viewModel.outputs.activity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::takeActivity);

    this.viewModel.outputs.projectList()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::takeProjects);

    this.viewModel.outputs.shouldShowEmptySavedView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(show -> ViewUtils.setGone(emptyView, !show));

    this.viewModel.outputs.animateHearts()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> animateHearts());

    this.viewModel.outputs.shouldShowOnboardingView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::setShouldShowOnboardingView);

    this.viewModel.outputs.showActivityFeed()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> startActivityFeedActivity());

    this.viewModel.outputs.startUpdateActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startUpdateActivity);

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));

    this.viewModel.outputs.showLoginTout()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startLoginToutActivity());

    return view;
  }

  private void animateHearts() {
    this.heartFilled.setAlpha(0f);
    this.heartFilled.setScaleX(0f);
    this.heartFilled.setScaleY(0f);
    this.heartOutline.setAlpha(1f);
    this.heartOutline.setScaleX(1f);
    this.heartOutline.setScaleY(1f);
    this.heartFilled.animate().alpha(1).scaleX(1).scaleY(1).setInterpolator(new AccelerateDecelerateInterpolator()).setStartDelay(500).start();
    this.heartOutline.animate().alpha(0).scaleX(0).scaleY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setStartDelay(500).start();
  }

  @Override
  public void onDetach() {
    super.onDetach();

    if (this.recyclerView != null) {
      this.recyclerView.setAdapter(null);
    }

    if (this.recyclerViewPaginator != null) {
      this.recyclerViewPaginator.stop();
    }
  }

  public boolean isAttached() {
    return this.viewModel != null;
  }

  public boolean isInstantiated() {
    return this.recyclerView != null;
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

  private void startUpdateActivity(final @NonNull Activity activity) {
    final Intent intent = new Intent(getActivity(), UpdateActivity.class)
      .putExtra(IntentKey.PROJECT, activity.project())
      .putExtra(IntentKey.UPDATE, activity.update());
    startActivity(intent);
    transition(getActivity(), slideInFromRight());
  }

  public void takeCategories(final @NonNull List<Category> categories) {
    this.viewModel.inputs.rootCategories(categories);
  }

  public void updateParams(final @NonNull DiscoveryParams params) {
    this.viewModel.inputs.paramsFromActivity(params);
  }

  public void clearPage() {
    this.viewModel.inputs.clearPage();
  }
}
