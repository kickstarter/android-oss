package com.kickstarter.libs;

import android.os.Bundle;

public class Presenter<ViewType> {
  private ViewType view;

  protected void onCreate(Bundle savedInstanceState) {}
  protected void onDestroy() {}
  protected void onSave(Bundle state) {}
}
