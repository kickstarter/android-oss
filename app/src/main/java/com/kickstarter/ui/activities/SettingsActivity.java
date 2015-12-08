package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.User;
import com.kickstarter.ui.views.IconTextView;
import com.kickstarter.viewmodels.SettingsViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(SettingsViewModel.class)
public final class SettingsActivity extends BaseActivity<SettingsViewModel> {
  protected @Bind(R.id.happening_now_switch) SwitchCompat happeningNewsletterSwitch;
  protected @Bind(R.id.friend_activity_mail_icon) IconTextView friendActivityMailIconTextView;
  protected @Bind(R.id.friend_activity_phone_icon) IconTextView friendActivityPhoneIconTextView;
  protected @Bind(R.id.new_followers_mail_icon) IconTextView newFollowersMailIconTextView;
  protected @Bind(R.id.new_followers_phone_icon) IconTextView newFollowersPhoneIconTextView;
  protected @Bind(R.id.project_notifications_count) TextView projectNotificationsCountTextView;
  protected @Bind(R.id.project_updates_mail_icon) IconTextView projectUpdatesMailIconTextView;
  protected @Bind(R.id.project_updates_phone_icon) IconTextView projectUpdatesPhoneIconTextView;
  protected @Bind(R.id.kickstarter_news_and_events_switch) SwitchCompat promoNewsletterSwitch;
  protected @Bind(R.id.projects_we_love_switch) SwitchCompat weeklyNewsletterSwitch;

  protected @BindColor(R.color.green) int green;
  protected @BindColor(R.color.gray) int gray;

  protected @BindString(R.string.Not_implemented_yet) String notImplemetedYetString;

  @Inject Logout logout;

  private boolean notifyMobileOfFollower;
  private boolean notifyMobileOfFriendActivity;
  private boolean notifyMobileOfUpdates;
  private boolean notifyOfFollower;
  private boolean notifyOfFriendActivity;
  private boolean notifyOfUpdates;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    viewModel.outputs.user()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::displayPreferences);

    viewModel.unableToSavePreferenceError()
      .compose(bindToLifecycle())
      .subscribe(__ -> ViewUtils.showToast(this, "Unable to save your preferences."));  // todo: string
  }

  @OnClick(R.id.contact)
  public void contactClick() {
    // todo: set up contact view
    ViewUtils.showToast(this, notImplemetedYetString);
  }

  @OnClick(R.id.cookie_policy)
  public void cookiePolicyClick() {
    startHelpActivity(HelpActivity.HELP_TYPE_COOKIE_POLICY);
  }

  public void displayPreferences(final @NonNull User user) {
    projectNotificationsCountTextView.setText(user.backedProjectsCount().toString());

    notifyMobileOfFriendActivity = user.notifyMobileOfFriendActivity();
    notifyOfFriendActivity = user.notifyOfFriendActivity();
    notifyMobileOfFollower = user.notifyMobileOfFollower();
    notifyOfFollower = user.notifyOfFollower();
    notifyMobileOfUpdates = user.notifyMobileOfUpdates();
    notifyOfUpdates = user.notifyOfUpdates();

    toggleIconColor(friendActivityMailIconTextView, notifyOfFriendActivity);
    toggleIconColor(friendActivityPhoneIconTextView, notifyMobileOfFriendActivity);
    toggleIconColor(newFollowersMailIconTextView, notifyOfFollower);
    toggleIconColor(newFollowersPhoneIconTextView, notifyMobileOfFollower);
    toggleIconColor(projectUpdatesMailIconTextView, notifyOfUpdates);
    toggleIconColor(projectUpdatesPhoneIconTextView, notifyMobileOfUpdates);

    // set these toggles with initial values
    RxCompoundButton.checked(happeningNewsletterSwitch)
      .call(user.happeningNewsletter());

    // todo: investigate why this doesn't work
    RxCompoundButton.checkedChanges(happeningNewsletterSwitch)
      .last()
      .subscribe(viewModel.inputs::sendHappeningNewsletter);

    RxCompoundButton.checked(promoNewsletterSwitch)
      .call(user.promoNewsletter());

    RxCompoundButton.checked(weeklyNewsletterSwitch)
      .call(user.weeklyNewsletter());
  }

  @OnClick(R.id.faq)
  public void faqClick() {
    startHelpActivity(HelpActivity.HELP_TYPE_FAQ);
  }

  @OnClick(R.id.how_kickstarter_works)
  public void howKickstarterWorksClick() {
    startHelpActivity(HelpActivity.HELP_TYPE_HOW_IT_WORKS);
  }

  @OnClick(R.id.log_out_button)
  public void logout() {
    logout.execute();
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  @OnClick(R.id.manage_project_notifications)
  public void manageProjectNotifications() {
    final Intent intent = new Intent(this, ManageProjectNotificationActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.privacy_policy)
  public void privacyPolicyClick() {
    startHelpActivity(HelpActivity.HELP_TYPE_PRIVACY);
  }

  @OnClick(R.id.send_feedback)
  public void sendFeedbackClick() {
    // todo: set up feedback form
    ViewUtils.showToast(this, notImplemetedYetString);
  }

  public void startHelpActivity(final int helpType) {
    final Intent intent = new Intent(this, HelpActivity.class)
      .putExtra(getString(R.string.intent_help_type), helpType);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.friend_activity_mail_icon)
  public void toggleNotifyOfFriendActivity() {
    viewModel.inputs.notifyOfFriendActivity(!notifyOfFriendActivity);
  }

  @OnClick(R.id.friend_activity_phone_icon)
  public void toggleNotifyMobileOfFriendActivity() {
    viewModel.inputs.notifyMobileOfFriendActivity(!notifyMobileOfFriendActivity);
  }

  @OnClick(R.id.new_followers_mail_icon)
  public void toggleNotifyOfNewFollowers() {
    viewModel.inputs.notifyOfFollower(!notifyOfFollower);
  }

  @OnClick(R.id.new_followers_phone_icon)
  public void toggleNotifyMobileOfNewFollowers() {
    viewModel.inputs.notifyMobileOfFollower(!notifyMobileOfFollower);
  }

  @OnClick(R.id.project_updates_mail_icon)
  public void toggleNotifyOfUpdates() {
    viewModel.inputs.notifyOfUpdates(!notifyOfUpdates);
  }

  @OnClick(R.id.project_updates_phone_icon)
  public void toggleNotifyMobileOfUpdates() {
    viewModel.inputs.notifyMobileOfUpdates(!notifyMobileOfUpdates);
  }

  @OnClick(R.id.terms_of_use)
  public void termsOfUseClick() {
    startHelpActivity(HelpActivity.HELP_TYPE_TERMS);
  }

  public void toggleIconColor(final @NonNull TextView iconTextView, final boolean enabled) {
    final int color = enabled ? green : gray;
    iconTextView.setTextColor(color);
  }
}
