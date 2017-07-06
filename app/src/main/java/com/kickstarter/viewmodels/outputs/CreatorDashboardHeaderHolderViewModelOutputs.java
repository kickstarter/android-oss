package com.kickstarter.viewmodels.outputs;


import android.util.Pair;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;

import rx.Observable;

public interface CreatorDashboardHeaderHolderViewModelOutputs {
  /* string number with the percentage of a projects funding */
  Observable<String> percentageFunded();

  /* localized count of number of backers */
  Observable<String> projectBackersCountText();

  /* current project's name */
  Observable<String> projectNameTextViewText();

  /* project that is currently being viewed */
  Observable<Project> currentProject();

  /* time remaining for latest project (no units) */
  Observable<String> timeRemainingText();

  /* call when button is clicked to view individual project page */
  Observable<Pair<Project, RefTag>> startProjectActivity();
}
