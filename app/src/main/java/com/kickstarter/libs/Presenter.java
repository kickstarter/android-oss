package com.kickstarter.libs;

import android.os.Bundle;

public class Presenter<ViewType> {
  private ViewType view;

  protected void onCreate(Bundle savedInstanceState) {}
  protected void onDestroy() {}

  /*
   * This should be overridden for presenters that have state to persist.
   */
  public Bundle saveState() {
    Bundle bundle = new Bundle();
    return bundle;
  }
}
