package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.User;
import com.kickstarter.ui.data.Newsletter;
import com.kickstarter.ui.views.IconTextView;
import com.kickstarter.viewmodels.SettingsViewModel;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;
import static com.kickstarter.libs.utils.IntegerUtils.intValueOrZero;

@RequiresActivityViewModel(SettingsViewModel.class)
public final class SettingsActivity extends BaseActivity<SettingsViewModel> {
  protected @Bind(R.id.games_switch) SwitchCompat gamesNewsletterSwitch;
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

  @Inject CurrentUserType currentUser;
  @Inject KSString ksString;
  @Inject Logout logout;
  @Inject Build build;

  private boolean notifyMobileOfFollower;
  private boolean notifyMobileOfFriendActivity;
  private boolean notifyMobileOfUpdates;
  private boolean notifyOfFollower;
  private boolean notifyOfFriendActivity;
  private boolean notifyOfUpdates;
  private AlertDialog logoutConfirmationDialog;

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

    viewModel.outputs.showOptInPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showOptInPrompt);

    viewModel.errors.unableToSavePreferenceError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(this, unableToSaveString));

    RxView.clicks(gamesNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendGamesNewsletter(gamesNewsletterSwitch.isChecked()));

    RxView.clicks(happeningNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendHappeningNewsletter(happeningNewsletterSwitch.isChecked()));

    RxView.clicks(promoNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendPromoNewsletter(promoNewsletterSwitch.isChecked()));

    RxView.clicks(weeklyNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendWeeklyNewsletter(weeklyNewsletterSwitch.isChecked()));

    viewModel.outputs.showConfirmLogoutPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(show -> {
        if (show) {
          lazyLogoutConfirmationDialog().show();
        } else {
          lazyLogoutConfirmationDialog().dismiss();
        }
      });

    viewModel.outputs.logout()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> logout());
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

  @OnClick(R.id.faq)
  public void faqClick() {
    startHelpActivity(HelpActivity.Faq.class);
  }

  @OnClick(R.id.how_kickstarter_works)
  public void howKickstarterWorksClick() {
    startHelpActivity(HelpActivity.HowItWorks.class);
  }

  @OnClick(R.id.log_out_button)
  public void logoutClick() {
    viewModel.inputs.logoutClicked();
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

  @OnClick(R.id.settings_rate_us)
  public void rateUsClick() {
    ViewUtils.openStoreRating(this, getPackageName());
  }

  private void composeContactEmail(final @Nullable User user) {
    final List<String> debugInfo = Arrays.asList(
      user != null ? String.valueOf(user.id()) : loggedOutString,
      build.versionName(),
      android.os.Build.VERSION.RELEASE + " (SDK " + Integer.toString(android.os.Build.VERSION.SDK_INT) + ")",
      android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
    );

    final String body = new StringBuilder()
      .append(supportEmailBodyString)
      .append(TextUtils.join(" | ", debugInfo))
      .toString();

    final Intent intent = new Intent(Intent.ACTION_SENDTO)
      .setData(Uri.parse(mailtoString))
      .putExtra(Intent.EXTRA_SUBJECT, "[Android] " + supportEmailSubjectString)
      .putExtra(Intent.EXTRA_TEXT, body)
      .putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmailString});
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(Intent.createChooser(intent, getString(R.string.support_email_chooser)));
    }
  }

  private void displayPreferences(final @NonNull User user) {
    projectNotificationsCountTextView.setText(String.valueOf(intValueOrZero(user.backedProjectsCount())));

    notifyMobileOfFriendActivity = isTrue(user.notifyMobileOfFriendActivity());
    notifyOfFriendActivity = isTrue(user.notifyOfFriendActivity());
    notifyMobileOfFollower = isTrue(user.notifyMobileOfFollower());
    notifyOfFollower = isTrue(user.notifyOfFollower());
    notifyMobileOfUpdates = isTrue(user.notifyMobileOfUpdates());
    notifyOfUpdates = isTrue(user.notifyOfUpdates());

    toggleIconColor(friendActivityMailIconTextView, false, notifyOfFriendActivity);
    toggleIconColor(friendActivityPhoneIconTextView, true, notifyMobileOfFriendActivity);
    toggleIconColor(newFollowersMailIconTextView, false, notifyOfFollower);
    toggleIconColor(newFollowersPhoneIconTextView, true, notifyMobileOfFollower);
    toggleIconColor(projectUpdatesMailIconTextView, false, notifyOfUpdates);
    toggleIconColor(projectUpdatesPhoneIconTextView, true, notifyMobileOfUpdates);

    SwitchCompatUtils.setCheckedWithoutAnimation(gamesNewsletterSwitch, isTrue(user.gamesNewsletter()));
    SwitchCompatUtils.setCheckedWithoutAnimation(happeningNewsletterSwitch, isTrue(user.happeningNewsletter()));
    SwitchCompatUtils.setCheckedWithoutAnimation(promoNewsletterSwitch, isTrue(user.promoNewsletter()));
    SwitchCompatUtils.setCheckedWithoutAnimation(weeklyNewsletterSwitch, isTrue(user.weeklyNewsletter()));
  }

  /**
   * Lazily creates a logout confirmation dialog and stores it in an instance variable.
   */
  private @NonNull AlertDialog lazyLogoutConfirmationDialog() {
    if (logoutConfirmationDialog == null) {
      logoutConfirmationDialog = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.profile_settings_logout_alert_title))
        .setMessage(getString(R.string.profile_settings_logout_alert_message))
        .setPositiveButton(getString(R.string.profile_settings_logout_alert_confirm_button), (__, ___) -> {
          viewModel.inputs.confirmLogoutClicked();
        })
        .setNegativeButton(getString(R.string.profile_settings_logout_alert_cancel_button), (__, ___) -> {
          viewModel.inputs.closeLogoutConfirmationClicked();
        })
        .setOnCancelListener(__ -> viewModel.inputs.closeLogoutConfirmationClicked())
        .create();
    }
    return logoutConfirmationDialog;
  }

  private void logout() {
    logout.execute();
    ApplicationUtils.startNewDiscoveryActivity(this);
  }

  private @Nullable String newsletterString(final @NonNull Newsletter newsletter) {
    switch (newsletter) {
      case GAMES:
        return gamesNewsletterString;
      case HAPPENING:
        return happeningNewsletterString;
      case PROMO:
        return promoNewsletterString;
      case WEEKLY:
        return weeklyNewsletterString;
      default:
        return null;
    }
  }

  private void showOptInPrompt(final @NonNull Newsletter newsletter) {
    final String string = newsletterString(newsletter);
    if (string == null) {
      return;
    }

    final String optInDialogMessageString = ksString.format(optInMessageString, "newsletter", string);
    ViewUtils.showDialog(this, optInTitleString, optInDialogMessageString);
  }

  private void toggleIconColor(final @NonNull TextView iconTextView, final boolean typeMobile, final boolean enabled) {
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
