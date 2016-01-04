package com.kickstarter.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.StatusBarUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.containers.ApplicationContainer;
import com.kickstarter.ui.toolbars.DiscoveryToolbar;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.viewmodels.DiscoveryViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(DiscoveryViewModel.class)
public final class DiscoveryActivity extends BaseActivity<DiscoveryViewModel> implements DiscoveryAdapter.Delegate {
  DiscoveryAdapter adapter;
  LinearLayoutManager layoutManager;
  private RecyclerViewPaginator recyclerViewPaginator;

  @Inject ApplicationContainer applicationContainer;
  @Inject CurrentUser currentUser;

  @BindDrawable(R.drawable.dark_blue_gradient) Drawable darkBlueGradientDrawable;
  @Bind(R.id.discovery_layout) LinearLayout discoveryLayout;
  @Bind(R.id.discovery_toolbar) DiscoveryToolbar discoveryToolbar;
  public @Bind(R.id.recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KSApplication) getApplication()).component().inject(this);
    final ViewGroup container = applicationContainer.bind(this);
    final LayoutInflater layoutInflater = getLayoutInflater();

    layoutInflater.inflate(R.layout.discovery_layout, container);
    ButterKnife.bind(this, container);

    layoutManager = new LinearLayoutManager(this);
    adapter = new DiscoveryAdapter(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    final DiscoveryParams params = getIntent().getParcelableExtra(getString(R.string.intent_discovery_params));
    if (params != null) {
      viewModel.takeParams(params);
    }

    recyclerViewPaginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);

    viewModel.outputs.user()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::takeUser);

    viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::takeProjects);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    recyclerViewPaginator.stop();
  }

  public void projectCardClick(@NonNull final ProjectCardViewHolder viewHolder, @NonNull final Project project) {
    viewModel.inputs.projectClick(project);
  }

  public void signupLoginClick(final @NonNull DiscoveryOnboardingViewHolder viewHolder) {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_login_type), LoginToutActivity.REASON_GENERIC);
    startActivity(intent);
  }

  public void loadParams(@NonNull final DiscoveryParams params) {
    discoveryToolbar.loadParams(params);

    if (ApiCapabilities.canSetStatusBarColor() && ApiCapabilities.canSetDarkStatusBarIcons()) {
      StatusBarUtils.apply(this, DiscoveryUtils.primaryColor(this, params), DiscoveryUtils.overlayShouldBeLight(params));
    }
  }

  public void startDiscoveryFilterActivity(@NonNull final DiscoveryParams params) {
    final Intent intent = new Intent(this, DiscoveryFilterActivity.class)
      .putExtra(getString(R.string.intent_discovery_params), params);

    startActivityForResult(intent, ActivityRequestCodes.DISCOVERY_ACTIVITY_DISCOVERY_FILTER_ACTIVITY_SELECT_FILTER);
  }

  public void startProjectActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.DISCOVERY_ACTIVITY_DISCOVERY_FILTER_ACTIVITY_SELECT_FILTER) {
      return;
    }

    if (resultCode != RESULT_OK) {
      return;
    }

    final DiscoveryParams params = intent.getExtras().getParcelable(getString(R.string.intent_discovery_params));
    viewModel.takeParams(params);
  }

  public void showBuildAlert(@NonNull final InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle(getString(R.string.___Upgrade_app))
      .setMessage(getString(R.string.___A_newer_build_is_available))
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        Intent intent = new Intent(this, DownloadBetaActivity.class)
          .putExtra(getString(R.string.intent_internal_build_envelope), envelope);
        startActivity(intent);
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }
}
