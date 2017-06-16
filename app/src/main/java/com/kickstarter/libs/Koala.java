package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.data.LoginReason;

import java.util.HashMap;
import java.util.Map;

public final class Koala {
  private final @NonNull TrackingClientType client;

  public Koala(final @NonNull TrackingClientType client) {
    this.client = client;
  }

  public @NonNull TrackingClientType client() {
    return client;
  }

  // APPLICATION LIFECYCLE
  public void trackAppOpen() {
    client.track("App Open");
  }

  public void trackAppClose() {
    client.track("App Close");
  }

  public void trackMemoryWarning() {
    client.track("App Memory Warning");
  }

  public void trackOpenedAppBanner() {
    client.track("Opened App Banner");
  }

  // ANDROID PAY
  public void trackShowAndroidPaySheet() {
    client.track("Android Pay Show Sheet");
  }

  public void trackAndroidPayFinished() {
    client.track("Android Pay Finished");
  }

  public void trackAndroidPayCanceled() {
    client.track("Android Pay Canceled");
  }

  // BACKING
  public void trackViewedPledgeInfo(final @NonNull Project project) {
    this.client.track(KoalaEvent.VIEWED_PLEDGE_INFO, KoalaUtils.projectProperties(project));
  }

  // DISCOVERY
  public void trackDiscovery(final @NonNull DiscoveryParams params, final boolean isOnboardingVisible) {
    final Map<String, Object> props = KoalaUtils.discoveryParamsProperties(params);
    props.put("discover_onboarding_is_visible", isOnboardingVisible);
    client.track("Discover List View", props);
  }

  public void trackDiscoveryFilters() {
    client.track("Discover Switch Modal", new HashMap<String, Object>() {
      {
        put("modal_type", "filters");
      }
    });
  }

  public void trackDiscoveryFilterSelected(final @NonNull DiscoveryParams params) {
    client.track("Discover Modal Selected Filter", KoalaUtils.discoveryParamsProperties(params));
  }

  /**
   * Tracks a project show event.
   * @param intentRefTag (nullable) The ref tag present in the activity upon displaying the project.
   * @param cookieRefTag (nullable) The ref tag extracted from the cookie store upon viewing the project.
   */
  public void trackProjectShow(final @NonNull Project project, final @Nullable RefTag intentRefTag, final @Nullable RefTag cookieRefTag) {

    final Map<String, Object> properties = KoalaUtils.projectProperties(project);

    if (intentRefTag != null) {
      properties.put("ref_tag", intentRefTag.tag());
    }

    if (cookieRefTag != null) {
      properties.put("referrer_credit", cookieRefTag.tag());
    }

    // Deprecated event
    client.track(KoalaEvent.PROJECT_PAGE, properties);

    client.track(KoalaEvent.VIEWED_PROJECT_PAGE, properties);
  }

  // PROJECT STAR
  public void trackProjectStar(final @NonNull Project project) {
    final Map<String, Object> props = KoalaUtils.projectProperties(project);

    // Deprecated events
    client.track(project.isStarred() ? KoalaEvent.PROJECT_STAR : KoalaEvent.PROJECT_UNSTAR, props);

    client.track(project.isStarred() ? KoalaEvent.STARRED_PROJECT : KoalaEvent.UNSTARRED_PROJECT, props);
  }

  // COMMENTS
  public void trackLoadedOlderComments(final @NonNull Project project, final @Nullable Update update,
    final @NonNull KoalaContext.Comments context) {

    final Map<String, Object> props = update == null
      ? KoalaUtils.projectProperties(project)
      : KoalaUtils.updateProperties(project, update);
    props.put("context", context.getTrackingString());

    client.track(KoalaEvent.LOADED_OLDER_COMMENTS, props);
  }

  /**
   * @deprecated Use {@link #trackLoadedOlderComments(Project, Update, KoalaContext.Comments)} instead.
   */
  @Deprecated
  public void trackLoadedOlderProjectComments(final @NonNull Project project) {
    client.track(KoalaEvent.PROJECT_COMMENT_LOAD_OLDER, KoalaUtils.projectProperties(project));
  }

  public void trackPostedComment(final @NonNull Project project, final @Nullable Update update,
    final @NonNull KoalaContext.CommentDialog context) {

    final Map<String, Object> props = update == null
      ? KoalaUtils.projectProperties(project)
      : KoalaUtils.updateProperties(project, update);
    props.put("context", context.getTrackingString());

    client.track(KoalaEvent.POSTED_COMMENT, props);
  }

  /**
   * @deprecated Use {@link #trackPostedComment(Project, Update, KoalaContext.CommentDialog)} instead.
   */
  @Deprecated
  public void trackProjectCommentCreate(final @NonNull Project project) {
    client.track(KoalaEvent.PROJECT_COMMENT_CREATE, KoalaUtils.projectProperties(project));
  }

  /**
   * @deprecated Use {@link #trackViewedComments(Project, Update, KoalaContext.Comments)} instead.
   */
  @Deprecated
  public void trackProjectCommentsView(final @NonNull Project project) {
    client.track(KoalaEvent.PROJECT_COMMENT_VIEW, KoalaUtils.projectProperties(project));
  }

  public void trackViewedComments(final @NonNull Project project, final @Nullable Update update,
    final @NonNull KoalaContext.Comments context) {

    final Map<String, Object> props = update == null
      ? KoalaUtils.projectProperties(project)
      : KoalaUtils.updateProperties(project, update);

    props.put("context", context.getTrackingString());
    client.track(KoalaEvent.VIEWED_COMMENTS, props);
  }

  // ACTIVITY
  public void trackActivityView(final int pageCount) {
    if (pageCount == 0) {
      client.track(KoalaEvent.ACTIVITY_VIEW);
    } else {
      client.track(KoalaEvent.ACTIVITY_LOAD_MORE, new HashMap<String, Object>() {
        {
          put("page_count", pageCount);
        }
      });
    }
  }

  // SEARCH
  public void trackSearchView() {
    client.track(KoalaEvent.VIEWED_SEARCH);
    // deprecated
    client.track(KoalaEvent.DISCOVER_SEARCH_LEGACY);
  }

  public void trackSearchResults(final @NonNull String query, final int pageCount) {
    if (pageCount == 1) {
      final Map<String, Object> params = new HashMap<String, Object>() {
        {
          put("search_term", query);
        }
      };
      client.track(KoalaEvent.LOADED_SEARCH_RESULTS, params);
      // deprecated
      client.track(KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY, params);
    } else {
      final Map<String, Object> params = new HashMap<String, Object>() {
        {
          put("search_term", query);
          put("page_count", pageCount);
        }
      };
      client.track(KoalaEvent.LOADED_MORE_SEARCH_RESULTS, params);
      // deprecated
      client.track(KoalaEvent.DISCOVER_SEARCH_RESULTS_LOAD_MORE_LEGACY, params);
    }
  }

  public void trackClearedSearchTerm() {
    client.track(KoalaEvent.CLEARED_SEARCH_TERM);
  }

  public void trackActivityTapped(final @NonNull Activity activity) {
    client.track(KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaUtils.activityProperties(activity));
  }

  // SESSION EVENTS
  public void trackLoginRegisterTout(final @NonNull LoginReason loginReason) {
    client.track("Application Login or Signup", new HashMap<String, Object>() {
      {
        put("intent", loginReason.trackingString());
      }
    });
  }

  public void trackLoginSuccess() {
    client.track("Login");
  }

  public void trackLoginError() {
    client.track("Errored User Login");
  }

  public void trackTwoFactorAuthView() {
    client.track("Two-factor Authentication Confirm View");
  }

  public void trackTwoFactorResendCode() {
    client.track("Two-factor Authentication Resend Code");
  }

  public void trackRegisterFormView() {
    client.track("User Signup");
  }

  public void trackRegisterError() {
    client.track("Errored User Signup");
  }

  public void trackRegisterSuccess() {
    client.track("New User");
  }

  public void trackResetPasswordFormView() {
    client.track("Forgot Password View");
  }

  public void trackResetPasswordSuccess() {
    client.track("Forgot Password Requested");
  }

  public void trackResetPasswordError() {
    client.track("Forgot Password Errored");
  }

  public void trackFacebookConfirmation() {
    client.track("Facebook Confirm");
  }

  public void trackFacebookLoginSuccess() {
    client.track("Facebook Login");
  }

  public void trackFacebookLoginError() {
    client.track("Errored Facebook Login");
  }

  public void trackLogout() {
    client.track("Logout");
  }

  public void trackSignupNewsletterToggle(final boolean sendNewsletters) {
    client.track("Signup Newsletter Toggle", new HashMap<String, Object>() {
      {
        put("send_newsletters", sendNewsletters);
      }
    });
  }

  // SETTINGS
  public void trackContactEmailClicked() {
    client.track("Contact Email Clicked");
  }

  public void trackNewsletterToggle(final boolean sendNewsletter) {
    if (sendNewsletter) {
      client.track("Newsletter Subscribe");
    } else {
      client.track("Newsletter Unsubscribe");
    }
  }

  public void trackSettingsView() {
    client.track("Settings View");
  }

  // CHECKOUT
  public void trackCheckoutNext() { // rewards webview and top nav
    client.track("Checkout Next");
  }
  public void trackCheckoutCancel() {
    client.track("Checkout Cancel");
  }

  public void trackCheckoutLoadFailed() {
    // TODO: set up error props
  }

  public void trackCheckoutShowShareSheet() {
    client.track("Checkout Show Share Sheet");
  }

  public void trackCheckoutCancelShareSheet() {
    client.track("Checkout Cancel Share Sheet");
  }

  public void trackCheckoutShowFacebookShareView() {
    client.track("Checkout Show Share", new HashMap<String, Object>() {
      {
        put("share_type", "facebook");
      }
    });
  }

  public void trackCheckoutShowTwitterShareView() {
    client.track("Checkout Show Share", new HashMap<String, Object>() {
      {
        put("share_type", "twitter");
      }
    });
  }

  public void trackCheckoutShareFinished() {
    client.track("Checkout Share Finished");
  }

  public void trackCheckoutFinishJumpToDiscovery() {
    client.track("Checkout Finished Discover More");
  }

  public void trackCheckoutFinishJumpToProject() {
    client.track("Checkout Finished Discover Open Project");
  }

  // SHARE
  public void trackShowProjectShareSheet() {
    client.track("Project Show Share Sheet");
  }

  public void trackCancelProjectShareSheet() {
    client.track("Project Cancel Share Sheet");
  }

  public void trackShowProjectFacebookShareView() {
    client.track("Project Show Share", new HashMap<String, Object>() {
      {
        put("share_type", "facebook");
      }
    });
  }

  public void trackShowProjectTwitterShareView() {
    client.track("Project Show Share", new HashMap<String, Object>() {
      {
        put("share_type", "twitter");
      }
    });
  }

  public void trackProjectFacebookShare() {
    client.track("Project Share", new HashMap<String, Object>() {
      {
        put("share_type", "facebook");
      }
    });
  }

  public void trackProjectTwitterShare() {
    client.track("Project Share", new HashMap<String, Object>() {
      {
        put("share_type", "twitter");
      }
    });
  }

  // MESSAGES
  public void trackSentMessage(final @NonNull Project project, final @NonNull KoalaContext.Message context) {
    final Map<String, Object> props = KoalaUtils.projectProperties(project);
    props.put("context", context.getTrackingString());

    this.client.track(KoalaEvent.SENT_MESSAGE, props);
  }

  public void trackViewedMessageThread(final @NonNull Project project) {
    this.client.track(KoalaEvent.VIEWED_MESSAGE_THREAD, KoalaUtils.projectProperties(project));
  }

  // PROFILE
  public void trackProfileView() {
    // deprecated
    client.track(KoalaEvent.PROFILE_VIEW_MY);

    client.track(KoalaEvent.VIEWED_PROFILE);
  }

  // RATING
  public void trackAppRatingNow() {
    client.track("Checkout Finished Alert App Store Rating Rate Now");
  }

  public void trackAppRatingRemindLater() {
    client.track("Checkout Finished Alert App Store Rating Remind Later");
  }

  public void trackAppRatingNoThanks() {
    client.track("Checkout Finished Alert App Store Rating No Thanks");
  }

  // VIDEO
  public void trackVideoStart(final @NonNull Project project) {
    client.track("Project Video Start", KoalaUtils.projectProperties(project));
  }

  // PROJECT UPDATES
  public void trackViewedUpdate(final @NonNull Project project, final @NonNull KoalaContext.Update context) {
    final Map<String, Object> props = KoalaUtils.projectProperties(project);
    props.put("context", context.getTrackingString());
    client.track(KoalaEvent.VIEWED_UPDATE, props);
  }

  public void trackViewedUpdates(final @NonNull Project project) {
    client.track(KoalaEvent.VIEWED_UPDATES, KoalaUtils.projectProperties(project));
  }

  // PUSH NOTIFICATIONS
  public void trackPushNotification(final @NonNull PushNotificationEnvelope envelope) {
    final Map<String, Object> properties = new HashMap<String, Object>() {
      {
        put("notification_type", "push");

        if (envelope.activity() != null) {
          put("notification_subject", "activity");
          put("notification_activity_category", envelope.activity().category());
        }
      }
    };

    client.track("Notification Opened", properties);
  }

  // WEBVIEWS
  public void trackOpenedExternalLink(final @NonNull Project project, final @NonNull KoalaContext.ExternalLink context) {
    final Map<String, Object> props = KoalaUtils.projectProperties(project);
    props.put("context", context.getTrackingString());

    client.track(KoalaEvent.OPENED_EXTERNAL_LINK, props);
  }
}
