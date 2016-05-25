package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.support.v4.view.RxViewPager;
import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.toolbars.DiscoveryToolbar;
import com.kickstarter.ui.views.SortTabLayout;
import com.kickstarter.viewmodels.DiscoveryViewModel;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

@RequiresActivityViewModel(DiscoveryViewModel.class)
public final class DiscoveryActivity extends BaseActivity<DiscoveryViewModel> {
  private DiscoveryDrawerAdapter drawerAdapter;
  private LinearLayoutManager drawerLayoutManager;
  private DiscoveryPagerAdapter pagerAdapter;

  protected @Inject ApiClientType client;
  protected @Inject InternalToolsType internalTools;

  protected @Bind(R.id.discovery_layout) DrawerLayout discoveryLayout;
  protected @Bind(R.id.discovery_toolbar) DiscoveryToolbar discoveryToolbar;
  protected @Bind(R.id.discovery_drawer_recycler_view) RecyclerView drawerRecyclerView;
  protected @Bind(R.id.discovery_tab_layout) SortTabLayout sortTabLayout;
  protected @Bind(R.id.discovery_view_pager) ViewPager sortViewPager;
  protected @Bind(R.id.discovery_sort_app_bar_layout) AppBarLayout sortAppBarLayout;

  protected @BindString(R.string.A_newer_build_is_available) String aNewerBuildIsAvailableString;
  protected @BindString(R.string.Upgrade_app) String upgradeAppString;
  protected @BindString(R.string.discovery_sort_types_magic) String magicString;
  protected @BindString(R.string.discovery_sort_types_popularity) String popularityString;
  protected @BindString(R.string.discovery_sort_types_newest) String newestString;
  protected @BindString(R.string.discovery_sort_types_end_date) String endDateString;
  protected @BindString(R.string.discovery_sort_types_most_funded) String mostFundedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.discovery_layout);
    ButterKnife.bind(this);

    ((KSApplication) getApplication()).component().inject(this);

    drawerLayoutManager = new LinearLayoutManager(this);
    drawerRecyclerView.setLayoutManager(drawerLayoutManager);
    drawerAdapter = new DiscoveryDrawerAdapter(viewModel.inputs);
    drawerRecyclerView.setAdapter(drawerAdapter);

    final List<String> viewPagerTitles = Arrays.asList(magicString, popularityString, newestString, endDateString,
      mostFundedString);
    pagerAdapter = new DiscoveryPagerAdapter(getSupportFragmentManager(), viewPagerTitles, viewModel.inputs);
    sortViewPager.setAdapter(pagerAdapter);
    sortTabLayout.setupWithViewPager(sortViewPager);

    RxViewPager.pageSelections(sortViewPager)
      // pagerAdapter is not ready yet - skip the first value
      // RxViewPager emits immediately on subscribe.
      .skip(1)
      .compose(bindToLifecycle())
      .subscribe(viewModel.inputs::pageChanged);

    viewModel.outputs.expandSortTabLayout()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(sortAppBarLayout::setExpanded);

    viewModel.outputs.updateToolbarWithParams()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(discoveryToolbar::loadParams);

    viewModel.outputs.updateParamsForPage()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pp -> pagerAdapter.takeParams(pp.first, pp.second));

    viewModel.outputs.clearPages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pagerAdapter::clearPages);

    viewModel.outputs.showInternalTools()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> internalTools.maybeStartInternalToolsActivity(this));

    viewModel.outputs.showLoginTout()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startLoginToutActivity());

    viewModel.outputs.showProfile()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startProfileActivity());

    viewModel.outputs.showSettings()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startSettingsActivity());

    viewModel.outputs.navigationDrawerData()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(drawerAdapter::takeData);

    viewModel.outputs.drawerIsOpen()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(RxDrawerLayout.open(discoveryLayout, GravityCompat.START));

    RxDrawerLayout.drawerOpen(discoveryLayout, GravityCompat.START)
      .skip(1)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(viewModel.inputs::openDrawer);
  }

  public @NonNull DrawerLayout discoveryLayout() {
    return discoveryLayout;
  }

  private void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }

  private void startProfileActivity() {
    final Intent intent = new Intent(this, ProfileActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startSettingsActivity() {
    final Intent intent = new Intent(this, SettingsActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT);
    startActivity(intent);
  }

  public void showBuildAlert(final @NonNull InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle(upgradeAppString)
      .setMessage(aNewerBuildIsAvailableString)
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        final Intent intent = new Intent(this, DownloadBetaActivity.class)
          .putExtra(IntentKey.INTERNAL_BUILD_ENVELOPE, envelope);
        startActivity(intent);
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }
}
