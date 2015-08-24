package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.CommentsActivity;

import javax.inject.Inject;

@RequiresPresenter(CommentsPresenter.class)
public class CommentsPresenter extends Presenter<CommentsActivity> {
  @Inject ApiClient client;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
//    ((KsrApplication) context.getApplicationContext()).component().inject(this);


  }
}
