package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.Release;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.User;
import com.kickstarter.ui.views.IconTextView;
import com.kickstarter.viewmodels.SettingsViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

  protected @BindString(R.string.profile_settings_accessibility_subscribe_mobile_notifications) String subscribeMobileString;
  protected @BindString(R.string.profile_settings_accessibility_subscribe_notifications) String subscribeString;
  protected @BindString(R.string.support_email_body) String supportEmailBodyString;
  protected @BindString(R.string.support_email_subject) String supportEmailSubjectString;
  protected @BindString(R.string.support_email_to) String supportEmailString;
  protected @BindString(R.string.profile_settings_error) String unableToSaveString;
  protected @BindString(R.string.profile_settings_accessibility_unsubscribe_mobile_notifications) String unsubscribeMobileString;
  protected @BindString(R.string.profile_settings_accessibility_unsubscribe_notifications) String unsubscribeString;

  @Inject CurrentUser currentUser;
  @Inject Logout logout;
  @Inject Release release;

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

    viewModel.errors.unableToSavePreferenceError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(this, unableToSaveString));

    RxView.clicks(happeningNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendHappeningNewsletter(this.happeningNewsletterSwitch.isChecked()));

    RxView.clicks(promoNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendPromoNewsletter(this.promoNewsletterSwitch.isChecked()));

    RxView.clicks(weeklyNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendWeeklyNewsletter(this.weeklyNewsletterSwitch.isChecked()));
  }

  protected void composeContactEmail(final @Nullable User user) {
    final List<String> debugInfo = Arrays.asList(
      (user != null ? user.name() : "Logged Out"),
      release.variant(),
      release.versionName(),
      release.versionCode().toString(),
      release.sha(),
      Integer.toString(Build.VERSION.SDK_INT),
      Build.MANUFACTURER + " " + Build.MODEL,
      Locale.getDefault().getLanguage()
    );

    final String body = new StringBuilder()
      .append(supportEmailBodyString)
      .append(TextUtils.join(" | ", debugInfo))
      .toString();

    final Intent intent = new Intent(Intent.ACTION_SENDTO)
      .setData(Uri.parse("mailto:"))
      .putExtra(Intent.EXTRA_SUBJECT, supportEmailSubjectString)
      .putExtra(Intent.EXTRA_TEXT, body)
      .putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmailString});
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(Intent.createChooser(intent, getString(R.string.Select_email_application)));
    }
  }

  @OnClick(R.id.contact)
  public void contactClick() {
    viewModel.inputs.contactEmailClicked();

    currentUser.observable()
      .take(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::composeContactEmail);
  }

  @OnClick(R.id.cookie_policy)
  public void cookiePolicyClick() {
    startHelpActivity(HelpActivity.CookiePolicy.class);
  }

  public void displayPreferences(final @NonNull User user) {
    projectNotificationsCountTextView.setText(user.backedProjectsCount().toString());

    notifyMobileOfFriendActivity = user.notifyMobileOfFriendActivity();
    notifyOfFriendActivity = user.notifyOfFriendActivity();
    notifyMobileOfFollower = user.notifyMobileOfFollower();
    notifyOfFollower = user.notifyOfFollower();
    notifyMobileOfUpdates = user.notifyMobileOfUpdates();
    notifyOfUpdates = user.notifyOfUpdates();

    toggleIconColor(friendActivityMailIconTextView, false, notifyOfFriendActivity);
    toggleIconColor(friendActivityPhoneIconTextView, true, notifyMobileOfFriendActivity);
    toggleIconColor(newFollowersMailIconTextView, false, notifyOfFollower);
    toggleIconColor(newFollowersPhoneIconTextView, true, notifyMobileOfFollower);
    toggleIconColor(projectUpdatesMailIconTextView, false, notifyOfUpdates);
    toggleIconColor(projectUpdatesPhoneIconTextView, true, notifyMobileOfUpdates);

    SwitchCompatUtils.setCheckedWithoutAnimation(happeningNewsletterSwitch, user.happeningNewsletter());
    SwitchCompatUtils.setCheckedWithoutAnimation(promoNewsletterSwitch, user.promoNewsletter());
    SwitchCompatUtils.setCheckedWithoutAnimation(weeklyNewsletterSwitch, user.weeklyNewsletter());
  }

  @OnClick(R.id.faq)
  public void faqClick() {
    startHelpActivity(HelpActivity.Faq.class);
  }

  @OnClick(R.id.how_kickstarter_works)
  public void howKickstarterWorksClick() {
    startHelpActivity(HelpActivity.HowItWorks.class);
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
    final Intent intent = new Intent(this, ManageNotificationActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.privacy_policy)
  public void privacyPolicyClick() {
    startHelpActivity(HelpActivity.Privacy.class);
  }

  public void startHelpActivity(final @NonNull Class<? extends HelpActivity> helpClass) {
    final Intent intent = new Intent(this, helpClass);
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
    startHelpActivity(HelpActivity.Terms.class);
  }

  public void toggleIconColor(final @NonNull TextView iconTextView, final boolean typeMobile, final boolean enabled) {
    final int color = enabled ? green : gray;
    iconTextView.setTextColor(color);

    String contentDescription = "";
    if (typeMobile && enabled) {
      contentDescription = unsubscribeMobileString;
    }
    if (typeMobile && !enabled) {
      contentDescription = subscribeMobileString;
    }
    if (!typeMobile && enabled) {
      contentDescription = unsubscribeString;
    }
    if (!typeMobile && !enabled) {
      contentDescription = subscribeString;
    }
    iconTextView.setContentDescription(contentDescription);
  }
}
