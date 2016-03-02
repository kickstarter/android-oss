package com.kickstarter.viewmodels.inputs;

import com.kickstarter.ui.adapters.ThanksAdapter;

public interface ThanksViewModelInputs extends ThanksAdapter.Delegate {
  /**
   * Call when the share button is clicked.
   */
  void shareClick();

  /**
   * Call when the share on Facebook button is clicked.
   */
  void shareOnFacebookClick();

  /**
   * Call when the share on Twitter button is clicked.
   */
  void shareOnTwitterClick();
}
