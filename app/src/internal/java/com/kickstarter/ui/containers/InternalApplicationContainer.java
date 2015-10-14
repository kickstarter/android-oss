package com.kickstarter.ui.containers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.ui.views.DebugDrawer;

import static butterknife.ButterKnife.findById;

public class InternalApplicationContainer implements ApplicationContainer {

  @Override
  public ViewGroup bind(@NonNull final Activity activity) {
    activity.setContentView(R.layout.container_layout);

    final ViewGroup viewGroup = findById(activity, R.id.debug_drawer);
    final DebugDrawer debugDrawer = new DebugDrawer(activity);
    viewGroup.addView(debugDrawer);

    return findById(activity, R.id.debug_content);
  }
}
