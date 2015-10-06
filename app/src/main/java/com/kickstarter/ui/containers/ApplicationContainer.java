package com.kickstarter.ui.containers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import static butterknife.ButterKnife.findById;

/*
 * An indirection that allows controlling the root container used for each activity.
 * See https://speakerdeck.com/mattprecious/debug-builds-a-new-hope-droidcon-mtl-2015 for more info.
 */
public interface ApplicationContainer {
  // The root ViewGroup into which the activity should place its contents.
  ViewGroup bind(@NonNull final Activity activity);

  // An ApplicationContainer that returns the normal activity content view.
  ApplicationContainer DEFAULT = new ApplicationContainer() {
    @Override public ViewGroup bind(@NonNull final Activity activity) {
      return findById(activity, android.R.id.content);
    }
  };
}
