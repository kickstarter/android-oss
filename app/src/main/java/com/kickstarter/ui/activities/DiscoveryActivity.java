package com.kickstarter.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout;
import com.kickstarter.BuildConfig;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.QualtricsIntercept;
import com.kickstarter.models.QualtricsResult;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.fragments.DiscoveryFragment;
import com.kickstarter.ui.toolbars.DiscoveryToolbar;
import com.kickstarter.ui.views.SortTabLayout;
import com.kickstarter.viewmodels.DiscoveryViewModel;
import com.qualtrics.digital.Qualtrics;
import com.qualtrics.digital.QualtricsSurveyActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

@RequiresActivityViewModel(DiscoveryViewModel.ViewModel.class)
public final class DiscoveryActivity extends BaseActivity<DiscoveryViewModel.ViewModel> {
  private DiscoveryDrawerAdapter drawerAdapter;
  private LinearLayoutManager drawerLayoutManager;
  private DiscoveryPagerAdapter pagerAdapter;
  private InternalToolsType internalTools;

  protected @Bind(R.id.discovery_layout) DrawerLayout discoveryLayout;
  protected @Bind(R.id.discovery_toolbar) DiscoveryToolbar discoveryToolbar;
  protected @Bind(R.id.discovery_drawer_recycler_view) RecyclerView drawerRecyclerView;
  protected @Bind(R.id.menu_button) ImageButton menuImageButton;
  protected @Bind(R.id.discovery_tab_layout) SortTabLayout sortTabLayout;
  protected @Bind(R.id.discovery_view_pager) ViewPager sortViewPager;
  protected @Bind(R.id.discovery_sort_app_bar_layout) AppBarLayout sortAppBarLayout;
  protected @Bind(R.id.qualtrics_prompt) View qualtricsPrompt;

  protected @BindString(R.string.A_newer_build_is_available) String aNewerBuildIsAvailableString;
  protected @BindString(R.string.Upgrade_app) String upgradeAppString;
  protected @BindString(R.string.discovery_sort_types_magic) String magicString;
  protected @BindString(R.string.Popular) String popularString;
  protected @BindString(R.string.discovery_sort_types_newest) String newestString;
  protected @BindString(R.string.Ending_soon) String endingSoonString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.discovery_layout);
    ButterKnife.bind(this);

    this.internalTools = environment().internalTools();

    this.drawerLayoutManager = new LinearLayoutManager(this);
    this.drawerRecyclerView.setLayoutManager(this.drawerLayoutManager);
    this.drawerAdapter = new DiscoveryDrawerAdapter(this.viewModel.inputs);
    this.drawerRecyclerView.setAdapter(this.drawerAdapter);

    final List<String> viewPagerTitles = Arrays.asList(
      this.magicString, this.popularString, this.newestString, this.endingSoonString);

    this.pagerAdapter = new DiscoveryPagerAdapter(
      getSupportFragmentManager(), createFragments(viewPagerTitles.size()), viewPagerTitles, this.viewModel.inputs
    );

    this.sortViewPager.setAdapter(this.pagerAdapter);
    this.sortTabLayout.setupWithViewPager(this.sortViewPager);
    addTabSelectedListenerToTabLayout();

    this.viewModel.outputs.expandSortTabLayout()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.sortAppBarLayout::setExpanded);

    this.viewModel.outputs.updateToolbarWithParams()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.discoveryToolbar::loadParams);

    this.viewModel.outputs.updateParamsForPage()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.pagerAdapter::takeParams);

    this.viewModel.outputs.clearPages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.pagerAdapter::clearPages);

    this.viewModel.outputs.rootCategoriesAndPosition()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(cp -> this.pagerAdapter.takeCategoriesForPosition(cp.first, cp.second));

    this.viewModel.outputs.showBuildCheckAlert()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showBuildAlert);

    this.viewModel.outputs.showActivityFeed()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startActivityFeedActivity());

    this.viewModel.outputs.showCreatorDashboard()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startCreatorDashboardActivity());

    this.viewModel.outputs.showHelp()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startHelpSettingsActivity());

    this.viewModel.outputs.showInternalTools()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.internalTools.maybeStartInternalToolsActivity(this));

    this.viewModel.outputs.showLoginTout()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startLoginToutActivity());

    this.viewModel.outputs.showMessages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startMessageThreadsActivity());

    this.viewModel.outputs.showProfile()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startProfileActivity());

    this.viewModel.outputs.showSettings()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startSettingsActivity());

    this.viewModel.outputs.navigationDrawerData()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.drawerAdapter::takeData);

    this.viewModel.outputs.drawerIsOpen()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(RxDrawerLayout.open(this.discoveryLayout, GravityCompat.START));

    this.viewModel.outputs.showMenuIconWithIndicator()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showMenuIconWithIndicator);

    //region Qualtrics
    this.viewModel.outputs.qualtricsPromptIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setInvisible(this.qualtricsPrompt));

    this.viewModel.outputs.setUpQualtrics()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setUpQualtrics);

    this.viewModel.outputs.showQualtricsSurvey()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showQualtricsSurvey);

    this.viewModel.outputs.updateImpressionCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::updateImpressionCount);
    //endregion

    RxDrawerLayout.drawerOpen(this.discoveryLayout, GravityCompat.START)
      .skip(1)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.viewModel.inputs::openDrawer);
  }

  private static @NonNull List<DiscoveryFragment> createFragments(final int pages) {
    final List<DiscoveryFragment> fragments = new ArrayList<>(pages);
    for (int position = 0; position <= pages; position++) {
      fragments.add(DiscoveryFragment.newInstance(position));
    }
    return fragments;
  }

  public @NonNull DrawerLayout discoveryLayout() {
    return this.discoveryLayout;
  }

  @Override
  public void onNetworkConnectionChanged(final boolean isConnected) {
    if (this.qualtricsPrompt.getVisibility() != View.VISIBLE) {
      super.onNetworkConnectionChanged(isConnected);
    }
  }

  @OnClick(R.id.qualtrics_confirm)
  public void qualtricsConfirmClicked() {
    this.viewModel.inputs.qualtricsConfirmClicked();
  }

  @OnClick(R.id.qualtrics_dismiss)
  public void qualtricsDismissClicked() {
    this.viewModel.inputs.qualtricsDismissClicked();
  }

  private void addTabSelectedListenerToTabLayout() {
    this.sortTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(final TabLayout.Tab tab) {

      }
      @Override
      public void onTabUnselected(final TabLayout.Tab tab) {

      }
      @Override
      public void onTabReselected(final TabLayout.Tab tab) {
        DiscoveryActivity.this.pagerAdapter.scrollToTop(tab.getPosition());
      }
    });
  }

  private void setUpQualtrics(final boolean firstAppSession) {
    Qualtrics.instance().initialize(Secrets.Qualtrics.BRAND_ID,
      Secrets.Qualtrics.ZONE_ID,
      QualtricsIntercept.NATIVE_APP_FEEDBACK.id(BuildConfig.APPLICATION_ID),
      this,
      initializationResult -> {
        if (initializationResult.passed()) {
          updateQualtricsProperties(firstAppSession);

          Qualtrics.instance().evaluateTargetingLogic(targetingResult -> DiscoveryActivity.this.viewModel.inputs.qualtricsResult(new QualtricsResult(targetingResult)));
        }
      });
  }

  private void showMenuIconWithIndicator(final boolean withIndicator) {
    if (withIndicator) {
      this.menuImageButton.setImageResource(R.drawable.ic_menu_indicator);
      final Animatable menuDrawable = (Animatable) this.menuImageButton.getDrawable();
      menuDrawable.start();
    } else {
      this.menuImageButton.setImageResource(R.drawable.ic_menu);
    }
  }

  protected void startActivityFeedActivity() {
    startActivity(new Intent(this, ActivityFeedActivity.class));
  }

  protected void startCreatorDashboardActivity() {
    startActivity(new Intent(this, CreatorDashboardActivity.class));
  }

  protected void startHelpSettingsActivity() {
    startActivity(new Intent(this, HelpSettingsActivity.class));
  }

  private void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }

  private void startMessageThreadsActivity() {
    final Intent intent = new Intent(this, MessageThreadsActivity.class)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.DRAWER);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startProfileActivity() {
    final Intent intent = new Intent(this, ProfileActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void showQualtricsSurvey(final String surveyUrl) {
    final Intent surveyIntent = new Intent(this, QualtricsSurveyActivity.class)
      .putExtra("targetURL", surveyUrl);

    startActivity(surveyIntent);
  }

  private void startSettingsActivity() {
    final Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
    overridePendingTransition(0, 0);
  }

  private void showBuildAlert(final @NonNull InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle(this.upgradeAppString)
      .setMessage(this.aNewerBuildIsAvailableString)
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

  private void updateImpressionCount(final @NonNull QualtricsIntercept qualtricsIntercept) {
    final String key = qualtricsIntercept.impressionCountKey(BuildConfig.APPLICATION_ID);

    final double initialCount = ObjectUtils.coalesce(Qualtrics.instance().properties.getNumber(key), 0.0);
    Qualtrics.instance().properties.setNumber(key, initialCount + 1.0);
  }

  private void updateQualtricsProperties(final boolean firstAppSession) {
    Qualtrics.instance().properties.setString("first_app_session", Boolean.toString(firstAppSession));
    Qualtrics.instance().properties.setString("language", Locale.getDefault().getLanguage());
    Qualtrics.instance().properties.setString("package_name", BuildConfig.APPLICATION_ID);
  }

}
