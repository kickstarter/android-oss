package com.kickstarter.ui.containers;

import android.app.Activity;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.ui.views.DebugView;

import static butterknife.ButterKnife.findById;

public class InternalDebugApplicationContainer implements ApplicationContainer {

  @Override
  public ViewGroup bind(final Activity activity) {
    activity.setContentView(R.layout.debug_drawer_layout);

    final ViewGroup drawer = findById(activity, R.id.debug_drawer);
    final DebugView debugView = new DebugView(activity);
    drawer.addView(debugView);

    return findById(activity, R.id.debug_content);
  }
}
