package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.data.ThanksData;

import java.util.List;

import rx.Observable;

public interface ThanksViewModelOutputs {
  /** Emits the data to configure the adapter with. */
  Observable<ThanksData> adapterData();

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
  Observable<Pair<List<Project>, Category>> showRecommendedProjects();

  /**
   * Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}.
   */
  Observable<DiscoveryParams> startDiscoveryActivity();

  /**
   * Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}.
   */
  Observable<Pair<Project, RefTag>> startProjectActivity();
}
