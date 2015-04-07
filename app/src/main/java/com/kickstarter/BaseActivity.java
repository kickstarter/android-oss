package com.kickstarter;

import android.app.Activity;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public abstract class BaseActivity extends Activity {
  private ObjectGraph activityGraph;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create the activity graph by .plus-ing our modules onto the application graph
    KsrApplication application = (KsrApplication) getApplication();
    activityGraph = application.getApplicationGraph().plus(getModules().toArray());

    activityGraph.inject(this);
  }

  @Override protected void onDestroy() {
    // Eagerly clear the reference to the activity graph to allow it to be
    // garbage collected as soon as possible
    activityGraph = null;

    super.onDestroy();
  }

  // A list of modules to use for the individual activity graph. Subclasses
  // can override this method to provide additional modules provided they call
  // and include the modules returned by calling super.getModules()
  protected List<Object> getModules() {
    return Arrays.<Object>asList(new ActivityModule(this));
  }

  // Inject the supplied object using the activity-specific graph
  public void inject(Object object) {
    activityGraph.inject(object);
  }
}
