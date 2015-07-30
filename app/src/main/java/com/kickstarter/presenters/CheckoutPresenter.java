package com.kickstarter.presenters;

import android.content.Intent;

import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Project;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.activities.ThanksActivity;

public class CheckoutPresenter extends Presenter<CheckoutActivity> {
  private Project project;

  public void takeProject(final Project project) {
    this.project = project;
  }

  public void takeLoginSuccess() {
    // In API < 19, can call loadUrl() with string "javascript:fn()"
    view().webView.evaluateJavascript("root.checkout_next();", null);
  }

  public void takeCheckoutThanksUriRequest() {
    final Intent intent = new Intent(view(), ThanksActivity.class);
    intent.putExtra("project", project);
    // TODO: Pass project in intent. Requires storing project.
    view().startActivity(intent);
  }
}
