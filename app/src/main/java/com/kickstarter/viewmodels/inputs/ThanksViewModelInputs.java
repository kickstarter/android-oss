package com.kickstarter.viewmodels.inputs;

import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder;

public interface ThanksViewModelInputs extends ProjectCardViewHolder.Delegate, ThanksCategoryViewHolder.Delegate,
  ThanksAdapter.Delegate {

  /**
   * Call when the user accepts the prompt to signup to the Games newsletter.
   */
  void signupToGamesNewsletterClick();
}
