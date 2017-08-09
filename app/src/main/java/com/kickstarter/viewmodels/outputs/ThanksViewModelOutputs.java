package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public interface ThanksViewModelOutputs {
  /**
   * Emits the backing's project name.
   */
  Observable<String> projectName();

  /**
   * Show a dialog confirming the user will be signed up to the games newsletter. Required for German users.
   */
  Observable<Void> showConfirmGamesNewsletterDialog();

  /**
   * Show a dialog prompting the user to sign-up to the games newsletter.
   */
  Observable<Void> showGamesNewsletterDialog();

  /**
   * Show a dialog prompting the user to rate the app.
   */
  Observable<Void> showRatingDialog();

  /**
   * Show recommended projects and a category tout.
   */
  Observable<Pair<List<Project>, Category>> showRecommendations();

  /**
   * Start a new discovery activity with the emitted params.
   */
  Observable<DiscoveryParams> startDiscovery();

  /**
   * Start a new project activity.
   */
  Observable<Project> startProject();

  /**
   * Share the project using Android's app chooser.
   */
  Observable<Project> startShare();

  /**
   * Share the project on Facebook.
   */
  Observable<Project> startShareOnFacebook();

  /**
   * Share the project on Twitter.
   */
  Observable<Project> startShareOnTwitter();
}
