package com.kickstarter.viewmodels.inputs;

import com.kickstarter.ui.adapters.ThanksAdapter;

public interface ThanksViewModelInputs extends ThanksAdapter.Delegate {
  /**
   * Generic button to share the backing.
   */
  void shareClick();

  /**
   * Share backing on Facebook.
   */
  void shareOnFacebookClick();

  /**
   * Share backing on Twitter.
   */
  void shareOnTwitterClick();
}
