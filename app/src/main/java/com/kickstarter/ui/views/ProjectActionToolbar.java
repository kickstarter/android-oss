package com.kickstarter.ui.views;


import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.models.Project;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

public class ProjectActionToolbar extends Toolbar {
  @InjectView(R.id.back_project_button) Button backProjectButton;
  @InjectView(R.id.manage_pledge_button) Button managePledgeButton;
  @Inject CurrentUser currentUser;

  // pass in an observable Project

  public ProjectActionToolbar(final Context context) {
    super(context);
  }

  public ProjectActionToolbar(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public ProjectActionToolbar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.inject(this);
  }
}
