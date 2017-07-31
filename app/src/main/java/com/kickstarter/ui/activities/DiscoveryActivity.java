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

import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.fragments.DiscoveryFragment;
import com.kickstarter.ui.toolbars.DiscoveryToolbar;
import com.kickstarter.ui.views.IconButton;
import com.kickstarter.ui.views.SortTabLayout;
import com.kickstarter.viewmodels.DiscoveryViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

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

  protected @Bind(R.id.creator_dashboard_button) IconButton creatorDashboardButton;
  protected @Bind(R.id.discovery_layout) DrawerLayout discoveryLayout;
  protected @Bind(R.id.discovery_toolbar) DiscoveryToolbar discoveryToolbar;
  protected @Bind(R.id.discovery_drawer_recycler_view) RecyclerView drawerRecyclerView;
  protected @Bind(R.id.discovery_tab_layout) SortTabLayout sortTabLayout;
  protected @Bind(R.id.discovery_view_pager) ViewPager sortViewPager;
  protected @Bind(R.id.discovery_sort_app_bar_layout) AppBarLayout sortAppBarLayout;

  protected @BindString(R.string.A_newer_build_is_available) String aNewerBuildIsAvailableString;
  protected @BindString(R.string.Upgrade_app) String upgradeAppString;
  protected @BindString(R.string.Home) String homeString;
  protected @BindString(R.string.Popular) String popularString;
  protected @BindString(R.string.discovery_sort_types_newest) String newestString;
  protected @BindString(R.string.Ending_soon) String endingSoonString;
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

    final List<String> viewPagerTitles = Arrays.asList(
      homeString, popularString, newestString, endingSoonString, mostFundedString
    );

    pagerAdapter = new DiscoveryPagerAdapter(
      getSupportFragmentManager(), createFragments(viewPagerTitles.size()), viewPagerTitles, viewModel.inputs
    );

    sortViewPager.setAdapter(pagerAdapter);
    sortTabLayout.setupWithViewPager(sortViewPager);

    viewModel.outputs.creatorDashboardButtonIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.creatorDashboardButton));

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
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pagerAdapter::takeParams);

    viewModel.outputs.clearPages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pagerAdapter::clearPages);

    viewModel.outputs.rootCategoriesAndPosition()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(cp -> pagerAdapter.takeCategoriesForPosition(cp.first, cp.second));

    viewModel.outputs.showBuildCheckAlert()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showBuildAlert);

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

  private static @NonNull List<DiscoveryFragment> createFragments(final int pages) {
    final List<DiscoveryFragment> fragments = new ArrayList<>(pages);
    for (int position = 0; position <= pages; position++) {
      fragments.add(DiscoveryFragment.newInstance(position));
    }
    return fragments;
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

  private void showBuildAlert(final @NonNull InternalBuildEnvelope envelope) {
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
