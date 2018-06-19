package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.User;
import com.kickstarter.ui.data.Newsletter;
import com.kickstarter.ui.views.IconTextView;
import com.kickstarter.viewmodels.SettingsViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.BooleanUtils.isFalse;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;
import static com.kickstarter.libs.utils.IntegerUtils.intValueOrZero;

@RequiresActivityViewModel(SettingsViewModel.ViewModel.class)
public final class SettingsActivity extends BaseActivity<SettingsViewModel.ViewModel> {
  protected @Bind(R.id.following_switch) SwitchCompat followingSwitch;
  protected @Bind(R.id.games_switch) SwitchCompat gamesNewsletterSwitch;
  protected @Bind(R.id.happening_now_switch) SwitchCompat happeningNewsletterSwitch;
  protected @Bind(R.id.friend_activity_mail_icon) ImageButton friendActivityMailImageButton;
  protected @Bind(R.id.friend_activity_phone_icon) IconTextView friendActivityPhoneIconTextView;
  protected @Bind(R.id.messages_mail_icon) ImageButton messagesMailImageButton;
  protected @Bind(R.id.messages_phone_icon) IconTextView messagesPhoneIconTextView;
  protected @Bind(R.id.new_followers_mail_icon) ImageButton newFollowersMailImageButton;
  protected @Bind(R.id.new_followers_phone_icon) IconTextView newFollowersPhoneIconTextView;
  protected @Bind(R.id.project_notifications_count) TextView projectNotificationsCountTextView;
  protected @Bind(R.id.project_updates_mail_icon) ImageButton projectUpdatesMailImageButton;
  protected @Bind(R.id.project_updates_phone_icon) IconTextView projectUpdatesPhoneIconTextView;
  protected @Bind(R.id.kickstarter_news_and_events_switch) SwitchCompat promoNewsletterSwitch;
  protected @Bind(R.id.recommendations_switch) SwitchCompat recommendationsSwitch;
  protected @Bind(R.id.private_profile_switch) SwitchCompat privateProfileSwitch;
  protected @Bind(R.id.projects_we_love_switch) SwitchCompat weeklyNewsletterSwitch;
  protected @Bind(R.id.version_name) TextView versionName;

  protected @BindColor(R.color.ksr_green_700) int green;
  protected @BindColor(R.color.ksr_dark_grey_400) int gray;

  protected @BindString(R.string.Cancel) String cancelString;
  protected @BindString(R.string.Following) String followingString;
  protected @BindString(R.string.When_following_is_on_you_can_follow_the_acticity_of_others) String followingInfoString;
  protected @BindString(R.string.Got_it) String gotItString;
  protected @BindString(R.string.profile_settings_newsletter_games) String gamesNewsletterString;
  protected @BindString(R.string.profile_settings_newsletter_happening) String happeningNewsletterString;
  protected @BindString(R.string.mailto) String mailtoString;
  protected @BindString(R.string.Logged_Out) String loggedOutString;
  protected @BindString(R.string.profile_settings_newsletter_weekly) String weeklyNewsletterString;
  protected @BindString(R.string.profile_settings_newsletter_promo) String promoNewsletterString;
  protected @BindString(R.string.profile_settings_newsletter_opt_in_message) String optInMessageString;
  protected @BindString(R.string.profile_settings_newsletter_opt_in_title) String optInTitleString;
  protected @BindString(R.string.profile_settings_accessibility_subscribe_mobile_notifications) String subscribeMobileString;
  protected @BindString(R.string.profile_settings_accessibility_subscribe_notifications) String subscribeString;
  protected @BindString(R.string.support_email_body) String supportEmailBodyString;
  protected @BindString(R.string.support_email_subject) String supportEmailSubjectString;
  protected @BindString(R.string.support_email_to_android) String supportEmailString;
  protected @BindString(R.string.profile_settings_error) String unableToSaveString;
  protected @BindString(R.string.profile_settings_accessibility_unsubscribe_mobile_notifications) String unsubscribeMobileString;
  protected @BindString(R.string.profile_settings_accessibility_unsubscribe_notifications) String unsubscribeString;
  protected @BindString(R.string.Recommendations) String recommendationsString;
  protected @BindString(R.string.We_use_your_activity_internally_to_make_recommendations_for_you) String recommendationsInfo;
  protected @BindString(R.string.Yes_turn_off) String yesTurnOffString;

  private CurrentUserType currentUser;
  private Build build;
  private KSString ksString;
  private Logout logout;

  private boolean notifyMobileOfFollower;
  private boolean notifyMobileOfFriendActivity;
  private boolean notifyMobileOfMessages;
  private boolean notifyMobileOfUpdates;
  private boolean notifyOfFollower;
  private boolean notifyOfFriendActivity;
  private boolean notifyOfMessages;
  private boolean notifyOfUpdates;
  private AlertDialog followingConfirmationDialog;
  private AlertDialog followingInfoDialog;
  private AlertDialog logoutConfirmationDialog;
  private AlertDialog recommendationsInfoDialog;
  private AlertDialog privateProfileInfoDialog;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_layout);
    ButterKnife.bind(this);

    this.build = environment().build();
    this.currentUser = environment().currentUser();
    this.ksString = environment().ksString();
    this.logout = environment().logout();

    setVersionName();

    this.viewModel.outputs.user()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::displayPreferences);

    this.viewModel.outputs.showOptInPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showOptInPrompt);

    this.viewModel.errors.unableToSavePreferenceError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(this, this.unableToSaveString));

    RxView.clicks(this.followingSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.optIntoFollowing(this.followingSwitch.isChecked()));

    this.viewModel.outputs.hideConfirmFollowingOptOutPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> SwitchCompatUtils.setCheckedWithoutAnimation(this.followingSwitch, true));

    this.viewModel.outputs.showConfirmFollowingOptOutPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> lazyFollowingOptOutConfirmationDialog().show());

    this.viewModel.outputs.showFollowingInfo()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> lazyFollowingInfoDialog().show());

    RxView.clicks(this.gamesNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.sendGamesNewsletter(this.gamesNewsletterSwitch.isChecked()));

    RxView.clicks(this.recommendationsSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.optedOutOfRecommendations(this.recommendationsSwitch.isChecked()));

    RxView.clicks(this.happeningNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.sendHappeningNewsletter(this.happeningNewsletterSwitch.isChecked()));

    RxView.clicks(this.promoNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.sendPromoNewsletter(this.promoNewsletterSwitch.isChecked()));

    RxView.clicks(this.weeklyNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.sendWeeklyNewsletter(this.weeklyNewsletterSwitch.isChecked()));

    RxView.clicks(this.privateProfileSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.showPublicProfile(this.privateProfileSwitch.isChecked()));

    this.viewModel.outputs.showConfirmLogoutPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(show -> {
        if (show) {
          lazyLogoutConfirmationDialog().show();
        } else {
          lazyLogoutConfirmationDialog().dismiss();
        }
      });

    this.viewModel.outputs.logout()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> logout());

    this.viewModel.outputs.showRecommendationsInfo()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> lazyRecommendationsInfoDialog().show());
  }

  @OnClick(R.id.contact)
  public void contactClick() {
    this.viewModel.inputs.contactEmailClicked();

    this.currentUser.observable()
      .take(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::composeContactEmail);
  }

  @OnClick(R.id.cookie_policy)
  public void cookiePolicyClick() {
    startHelpActivity(HelpActivity.CookiePolicy.class);
  }

  @OnClick(R.id.following_info)
  public void followingInfoClick() {
    this.viewModel.inputs.followingInfoClicked();
  }

  @OnClick(R.id.help_center)
  public void helpCenterClick() {
    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT));
    startActivity(intent);
  }

  @OnClick(R.id.how_kickstarter_works)
  public void howKickstarterWorksClick() {
    startHelpActivity(HelpActivity.HowItWorks.class);
  }

  @OnClick(R.id.log_out)
  public void logoutClick() {
    this.viewModel.inputs.logoutClicked();
  }

  @OnClick(R.id.manage_project_notifications)
  public void manageProjectNotifications() {
    final Intent intent = new Intent(this, ProjectNotificationSettingsActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.privacy_policy)
  public void privacyPolicyClick() {
    startHelpActivity(HelpActivity.Privacy.class);
  }

  @OnClick(R.id.recommendations_info)
  public void recommendationsInfoClick() {
    this.viewModel.inputs.recommendationsInfoClicked();
  }

  public void startHelpActivity(final @NonNull Class<? extends HelpActivity> helpClass) {
    final Intent intent = new Intent(this, helpClass);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.friend_activity_mail_icon)
  public void toggleNotifyOfFriendActivity() {
    this.viewModel.inputs.notifyOfFriendActivity(!this.notifyOfFriendActivity);
  }

  @OnClick(R.id.friend_activity_phone_icon)
  public void toggleNotifyMobileOfFriendActivity() {
    this.viewModel.inputs.notifyMobileOfFriendActivity(!this.notifyMobileOfFriendActivity);
  }

  @OnClick(R.id.messages_mail_icon)
  public void toggleNotifyOfMessages() {
    this.viewModel.inputs.notifyOfMessages(!this.notifyOfMessages);
  }

  @OnClick(R.id.messages_phone_icon)
  public void toggleNotifyMobileOfMessages() {
    this.viewModel.inputs.notifyMobileOfMessages(!this.notifyMobileOfMessages);
  }

  @OnClick(R.id.new_followers_mail_icon)
  public void toggleNotifyOfNewFollowers() {
    this.viewModel.inputs.notifyOfFollower(!this.notifyOfFollower);
  }

  @OnClick(R.id.new_followers_phone_icon)
  public void toggleNotifyMobileOfNewFollowers() {
    this.viewModel.inputs.notifyMobileOfFollower(!this.notifyMobileOfFollower);
  }

  @OnClick(R.id.project_updates_mail_icon)
  public void toggleNotifyOfUpdates() {
    this.viewModel.inputs.notifyOfUpdates(!this.notifyOfUpdates);
  }

  @OnClick(R.id.project_updates_phone_icon)
  public void toggleNotifyMobileOfUpdates() {
    this.viewModel.inputs.notifyMobileOfUpdates(!this.notifyMobileOfUpdates);
  }

  @OnClick(R.id.terms_of_use)
  public void termsOfUseClick() {
    startHelpActivity(HelpActivity.Terms.class);
  }

  @OnClick(R.id.settings_delete_account)
  public void deleteAccountClick() {
    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.Privacy.DELETE_ACCOUNT));
    startActivity(intent);
  }

  @OnClick(R.id.settings_request_data)
  public void requestDataClick() {
    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.Privacy.REQUEST_DATA));
    startActivity(intent);
  }

  @OnClick(R.id.settings_rate_us)
  public void rateUsClick() {
    ViewUtils.openStoreRating(this, getPackageName());
  }

  private void composeContactEmail(final @Nullable User user) {
    final List<String> debugInfo = Arrays.asList(
      user != null ? String.valueOf(user.id()) : this.loggedOutString,
      this.build.versionName(),
      android.os.Build.VERSION.RELEASE + " (SDK " + Integer.toString(android.os.Build.VERSION.SDK_INT) + ")",
      android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
    );

    final String body = new StringBuilder()
      .append(this.supportEmailBodyString)
      .append(TextUtils.join(" | ", debugInfo))
      .toString();

    final Intent intent = new Intent(Intent.ACTION_SENDTO)
      .setData(Uri.parse(this.mailtoString))
      .putExtra(Intent.EXTRA_SUBJECT, "[Android] " + this.supportEmailSubjectString)
      .putExtra(Intent.EXTRA_TEXT, body)
      .putExtra(Intent.EXTRA_EMAIL, new String[]{this.supportEmailString});

    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(Intent.createChooser(intent, getString(R.string.support_email_chooser)));
    }
  }

  private void displayPreferences(final @NonNull User user) {
    this.projectNotificationsCountTextView.setText(String.valueOf(intValueOrZero(user.backedProjectsCount())));

    this.notifyMobileOfFriendActivity = isTrue(user.notifyMobileOfFriendActivity());
    this.notifyOfFriendActivity = isTrue(user.notifyOfFriendActivity());
    this.notifyMobileOfMessages = isTrue(user.notifyMobileOfMessages());
    this.notifyOfMessages = isTrue(user.notifyOfMessages());
    this.notifyMobileOfFollower = isTrue(user.notifyMobileOfFollower());
    this.notifyOfFollower = isTrue(user.notifyOfFollower());
    this.notifyMobileOfUpdates = isTrue(user.notifyMobileOfUpdates());
    this.notifyOfUpdates = isTrue(user.notifyOfUpdates());

    toggleImageButtonIconColor(this.friendActivityMailImageButton, false, this.notifyOfFriendActivity);
    toggleTextViewIconColor(this.friendActivityPhoneIconTextView, true, this.notifyMobileOfFriendActivity);
    toggleImageButtonIconColor(this.messagesMailImageButton, false, this.notifyOfMessages);
    toggleTextViewIconColor(this.messagesPhoneIconTextView, true, this.notifyMobileOfMessages);
    toggleImageButtonIconColor(this.newFollowersMailImageButton, false, this.notifyOfFollower);
    toggleTextViewIconColor(this.newFollowersPhoneIconTextView, true, this.notifyMobileOfFollower);
    toggleImageButtonIconColor(this.projectUpdatesMailImageButton, false, this.notifyOfUpdates);
    toggleTextViewIconColor(this.projectUpdatesPhoneIconTextView, true, this.notifyMobileOfUpdates);

    SwitchCompatUtils.setCheckedWithoutAnimation(this.followingSwitch, isTrue(user.social()));
    SwitchCompatUtils.setCheckedWithoutAnimation(this.gamesNewsletterSwitch, isTrue(user.gamesNewsletter()));
    SwitchCompatUtils.setCheckedWithoutAnimation(this.recommendationsSwitch, isFalse(user.optedOutOfRecommendations()));
    SwitchCompatUtils.setCheckedWithoutAnimation(this.happeningNewsletterSwitch, isTrue(user.happeningNewsletter()));
    SwitchCompatUtils.setCheckedWithoutAnimation(this.promoNewsletterSwitch, isTrue(user.promoNewsletter()));
    SwitchCompatUtils.setCheckedWithoutAnimation(this.weeklyNewsletterSwitch, isTrue(user.weeklyNewsletter()));
  }

  private @NonNull AlertDialog lazyFollowingInfoDialog() {
    if (this.followingInfoDialog == null) {
      final String capitalizedGotIt = this.gotItString.toUpperCase(Locale.getDefault());
      this.followingInfoDialog = new AlertDialog.Builder(this)
        .setTitle(this.followingString)
        .setMessage(this.followingInfoString)
        .setPositiveButton(capitalizedGotIt, (__, ___) -> this.followingInfoDialog.dismiss())
        .setCancelable(true)
        .create();
    }
    return this.followingInfoDialog;
  }

  private @NonNull AlertDialog lazyFollowingOptOutConfirmationDialog() {
    if (this.followingConfirmationDialog == null) {
      this.followingConfirmationDialog = new AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(getString(R.string.Are_you_sure))
        .setMessage(getString(R.string.If_you_turn_following_off))
        .setNegativeButton(this.cancelString, (__, ___) -> this.viewModel.inputs.optOutOfFollowing(false))
        .setPositiveButton(this.yesTurnOffString, (__, ___) -> this.viewModel.inputs.optOutOfFollowing(true))
        .create();
    }
    return this.followingConfirmationDialog;
  }

  /**
   * Lazily creates a logout confirmation dialog and stores it in an instance variable.
   */
  private @NonNull AlertDialog lazyLogoutConfirmationDialog() {
    if (this.logoutConfirmationDialog == null) {
      this.logoutConfirmationDialog = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.profile_settings_logout_alert_title))
        .setMessage(getString(R.string.profile_settings_logout_alert_message))
        .setPositiveButton(getString(R.string.profile_settings_logout_alert_confirm_button), (__, ___) -> this.viewModel.inputs.confirmLogoutClicked())
        .setNegativeButton(getString(R.string.profile_settings_logout_alert_cancel_button), (__, ___) -> this.viewModel.inputs.closeLogoutConfirmationClicked())
        .setOnCancelListener(__ -> this.viewModel.inputs.closeLogoutConfirmationClicked())
        .create();
    }
    return this.logoutConfirmationDialog;
  }

  private @NonNull AlertDialog lazyRecommendationsInfoDialog() {
    if (this.recommendationsInfoDialog == null) {
      final String capitalizedGotIt = this.gotItString.toUpperCase(Locale.getDefault());
      this.recommendationsInfoDialog = new AlertDialog.Builder(this)
        .setTitle(this.recommendationsString)
        .setMessage(this.recommendationsInfo)
        .setPositiveButton(capitalizedGotIt, (__, ___) -> this.recommendationsInfoDialog.dismiss())
        .setCancelable(true)
        .create();
    }
    return this.recommendationsInfoDialog;
  }

  //TODO - Finish AlertDialog
  private @NonNull AlertDialog lazyPrivateProfileInfoDialog() {
    if (this.privateProfileInfoDialog == null) {
      final String capitalizedGotIt = this.gotItString.toUpperCase(Locale.getDefault());
      this.privateProfileInfoDialog = new AlertDialog.Builder(this)
        .setTitle(this.recommendationsString)
        .setMessage(this.recommendationsInfo)
        .setPositiveButton(capitalizedGotIt, (__, ___) -> this.recommendationsInfoDialog.dismiss())
        .setCancelable(true)
        .create();
    }
    return this.recommendationsInfoDialog;
  }

  private void logout() {
    this.logout.execute();
    ApplicationUtils.startNewDiscoveryActivity(this);
  }

  private @Nullable String newsletterString(final @NonNull Newsletter newsletter) {
    switch (newsletter) {
      case GAMES:
        return this.gamesNewsletterString;
      case HAPPENING:
        return this.happeningNewsletterString;
      case PROMO:
        return this.promoNewsletterString;
      case WEEKLY:
        return this.weeklyNewsletterString;
      default:
        return null;
    }
  }

  private void showOptInPrompt(final @NonNull Newsletter newsletter) {
    final String string = newsletterString(newsletter);
    if (string == null) {
      return;
    }

    final String optInDialogMessageString = this.ksString.format(this.optInMessageString, "newsletter", string);
    ViewUtils.showDialog(this, this.optInTitleString, optInDialogMessageString);
  }

  private void toggleTextViewIconColor(final @NonNull TextView iconTextView, final boolean typeMobile, final boolean enabled) {
    final int color = getEnabledColor(enabled);
    iconTextView.setTextColor(color);

    setContentDescription(iconTextView, typeMobile, enabled);
  }

  private void toggleImageButtonIconColor(final @NonNull ImageButton imageButton, final boolean typeMobile, final boolean enabled) {
    final int color = getEnabledColor(enabled);
    imageButton.setColorFilter(color);

    setContentDescription(imageButton, typeMobile, enabled);
  }

  private int getEnabledColor(final boolean enabled) {
    return enabled ? this.green : this.gray;
  }

  private void setContentDescription(final @NonNull View view, final boolean typeMobile, final boolean enabled) {
    String contentDescription = "";
    if (typeMobile && enabled) {
      contentDescription = this.unsubscribeMobileString;
    }
    if (typeMobile && !enabled) {
      contentDescription = this.subscribeMobileString;
    }
    if (!typeMobile && enabled) {
      contentDescription = this.unsubscribeString;
    }
    if (!typeMobile && !enabled) {
      contentDescription = this.subscribeString;
    }
    view.setContentDescription(contentDescription);
  }

  private void setVersionName() {
    this.versionName.setText(this.build.versionName());
  }
}
