package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.KoalaUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.HashMap;

import static com.kickstarter.libs.utils.KoalaUtils.activityProperties;
import static com.kickstarter.libs.utils.KoalaUtils.discoveryParamsProperties;

public final class Koala {
  private @NonNull final TrackingType client;

  public Koala(@NonNull final TrackingType client) {
    this.client = client;
  }

  // DISCOVERY
  public void trackDiscovery(@NonNull final DiscoveryParams params) {
    client.track("Discover List View", discoveryParamsProperties(params));
  }

  public void trackDiscoveryFilters() {
    client.track("Discover Switch Modal", new HashMap<String, Object>() {{
      put("modal_type", "filters");
    }});
  }

  public void trackDiscoveryFilterSelected(@NonNull final DiscoveryParams params) {
    client.track("Discover Modal Selected Filter", discoveryParamsProperties(params));
  }

  // PROJECT
  public void trackProjectShow() {
    client.track("Project Page");
  }

  // PROJECT STAR
  public void trackProjectStar(@NonNull final Project project) {
    if (project.isStarred()) {
      client.track("Project Star");
    } else {
      client.track("Project Unstar");
    }
  }

  // COMMENTING
  public void trackProjectCommentCreate(@NonNull final Project project, @NonNull final Comment comment) {
    client.track("Project Comment Create", KoalaUtils.projectProperties(project));
  }

  public void trackProjectCommentsView(@NonNull final Project project) {
    client.track("Project Comment View");
  }

  // SESSION EVENTS
  public void trackLoginRegisterTout(@Nullable final String intent) {
    client.track("Application Login or Signup", new HashMap<String, Object>() {{
      put("intent", intent);
    }});
  }

  // ACTIVITY
  public void trackActivityView(final int pageCount) {
    if (pageCount == 0) {
      client.track("Activity View");
    } else {
      client.track("Activity Load More", new HashMap<String, Object>() {{
        put("page_count", pageCount);
      }});
    }
  }

  // SEARCH
  public void trackSearchView() {
    client.track("Discover Search");
  }

  public void trackSearchResults(@NonNull final String query, final int pageCount) {
    if (pageCount == 1) {
      client.track("Discover Search Results", new HashMap<String, Object>() {{
        put("search_term", query);
      }});
    } else {
      client.track("Discover Search Results Load More", new HashMap<String, Object>() {{
        put("search_term", query);
        put("page_count", pageCount);
      }});
    }
  }

  public void trackActivityTapped(@NonNull final Activity activity) {
    client.track("Activity View Item", activityProperties(activity));
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

  public void trackForgotPasswordFormView() {
    client.track("Forgot Password View");
  }

  public void trackForgotPasswordRequestSuccess() {
    client.track("Forgot Password Requested");
  }

  public void trackForgotPasswordRequestFailed() {
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
    client.track("Signup Newsletter Toggle", new HashMap<String, Object>() {{
      put("send_newsletters", sendNewsletters);
    }});
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

  public void trackCheckoutShowShareView(@Nullable final String type) {
    client.track("Checkout Show Share", new HashMap<String, Object>() {{
      put("share_type", type);
    }});
  }

  public void trackCheckoutCanceledShareView(@Nullable final String type) {
    client.track("Checkout Cancel Share", new HashMap<String, Object>() {{
      put("share_type", type);
    }});
  }

  public void trackCheckoutShareFinishedWithShareTypes() {
    client.track("Checkout Share Finished"); // 99% sure we aren't actually ever sending "shareTypes", are we?
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

  public void trackShowProjectShareView(@Nullable final String type) {
    client.track("Project Show Share", new HashMap<String, Object>() {{
      put("share_type", type);
    }});
  }

  public void trackCancelProjectShareView(@Nullable final String type) {
    client.track("Project Cancel Share", new HashMap<String, Object>() {{
      put("share_type", type);
    }});
  }

  public void trackProjectShare(@Nullable final String type) {
    client.track("Project Share", new HashMap<String, Object>() {{
      put("share_type", type);
    }});
  }
}
