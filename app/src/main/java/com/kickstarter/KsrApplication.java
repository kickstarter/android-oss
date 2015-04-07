package com.kickstarter;

import android.app.Application;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public final class KsrApplication extends Application {
  private ObjectGraph applicationGraph;

  @Override public void onCreate() {
    super.onCreate();

    applicationGraph = ObjectGraph.create(getModules().toArray());
  }

  // We could use this to add additional modules in development or test
  // versions of the application
  protected List<Object> getModules() {
    return Arrays.asList(
      new AndroidModule(this)
    );
  }

  ObjectGraph getApplicationGraph() {
    return applicationGraph;
  }
}
