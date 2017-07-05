package com.kickstarter.viewmodels.outputs;


import rx.Observable;

public interface CreatorDashboardHeaderHolderViewModelOutputs {
  /* localized count of number of backers */
  Observable<String> projectBackersCountText();

  /* percentage funded text */
  Observable<String> percentageFundedTextViewText();

  /* time remaining for latest project (no units) */
  Observable<String> timeRemaining();
}
