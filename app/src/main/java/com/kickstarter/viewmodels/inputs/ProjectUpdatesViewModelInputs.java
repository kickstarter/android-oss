package com.kickstarter.viewmodels.inputs;

import okhttp3.Request;

public interface ProjectUpdatesViewModelInputs {
  /**
   * Call when the web view page url has been intercepted.
   */
  void pageInterceptedUrl(String url);

  /**
   * Call when a project update comments uri request has been made.
   */
  void updateCommentsRequest(Request request);
}
