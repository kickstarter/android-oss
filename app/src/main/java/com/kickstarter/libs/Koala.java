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
}
