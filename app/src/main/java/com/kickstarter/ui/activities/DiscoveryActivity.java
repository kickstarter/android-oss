package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.ui.intents.DiscoveryIntentAction;
import com.kickstarter.ui.toolbars.DiscoveryToolbar;
import com.kickstarter.viewmodels.DiscoveryViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(DiscoveryViewModel.class)
public final class DiscoveryActivity extends BaseActivity<DiscoveryViewModel> {
  private DiscoveryAdapter adapter;
  private DiscoveryIntentAction intentAction;
  private LinearLayoutManager layoutManager;
  private DiscoveryDrawerAdapter drawerAdapter;
  private LinearLayoutManager drawerLayoutManager;
  private RecyclerViewPaginator recyclerViewPaginator;

  protected @Inject ApiClientType client;
  protected @Inject InternalToolsType internalTools;

  protected @Bind(R.id.discovery_layout) DrawerLayout discoveryLayout;
  protected @Bind(R.id.discovery_toolbar) DiscoveryToolbar discoveryToolbar;
  protected @Bind(R.id.recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.discovery_drawer_recycler_view) RecyclerView drawerRecyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.discovery_layout);
    ButterKnife.bind(this);

    ((KSApplication) getApplication()).component().inject(this);

    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new DiscoveryAdapter(this.viewModel.inputs);
    recyclerView.setAdapter(adapter);

    drawerLayoutManager = new LinearLayoutManager(this); // TODO: Can we reuse the other layout manager?
    drawerRecyclerView.setLayoutManager(drawerLayoutManager);
    drawerAdapter = new DiscoveryDrawerAdapter(viewModel.inputs);
    drawerRecyclerView.setAdapter(drawerAdapter);


    intentAction = new DiscoveryIntentAction(viewModel.inputs::initializer, lifecycle(), client);
    intentAction.intent(getIntent());

    recyclerViewPaginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);

    viewModel.outputs.shouldShowOnboarding()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::setShouldShowOnboardingView);

    viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::takeProjects);

    viewModel.outputs.params()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadParams);

    viewModel.outputs.showInternalTools()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> internalTools.maybeStartInternalToolsActivity(this));

    viewModel.outputs.showLogin()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.startLoginActivity());

    viewModel.outputs.showProfile()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.startProfileActivity());

    viewModel.outputs.showProject()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));

    viewModel.outputs.showSettings()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.startSettingsActivity());

    viewModel.outputs.navigationDrawerData()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(drawerAdapter::takeData);

    viewModel.outputs.openDrawer()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(RxDrawerLayout.open(discoveryLayout, GravityCompat.START));
  }

  @Override
  protected void onNewIntent(final @NonNull Intent intent) {
    intentAction.intent(intent);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    recyclerViewPaginator.stop();
  }

  public @NonNull DrawerLayout discoveryLayout() {
    return discoveryLayout;
  }

  private void loadParams(final @NonNull DiscoveryParams params) {
    discoveryToolbar.loadParams(params);
  }

  private void startLoginActivity() {
    final Intent intent = new Intent(this, LoginActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startProfileActivity() {
    final Intent intent = new Intent(this, ProfileActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startSettingsActivity() {
    final Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @NonNull Intent intent) {
    if (requestCode != ActivityRequestCodes.DISCOVERY_ACTIVITY_DISCOVERY_FILTER_ACTIVITY_SELECT_FILTER) {
      return;
    }

    if (resultCode != RESULT_OK) {
      return;
    }

    intentAction.intent(intent);
  }

  public void showBuildAlert(final @NonNull InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle(getString(R.string.Upgrade_app))
      .setMessage(getString(R.string.A_newer_build_is_available))
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        Intent intent = new Intent(this, DownloadBetaActivity.class)
          .putExtra(IntentKey.INTERNAL_BUILD_ENVELOPE, envelope);
        startActivity(intent);
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }
}
