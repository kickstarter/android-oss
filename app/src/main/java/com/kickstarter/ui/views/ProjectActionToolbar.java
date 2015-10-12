package com.kickstarter.ui.views;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.Button;

import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProjectActionToolbar extends Toolbar {
  @Bind(R.id.back_project_button) Button backProjectButton;
  @Bind(R.id.manage_pledge_button) Button managePledgeButton;
  @Inject CurrentUser currentUser;

  // pass in an observable Project

  public ProjectActionToolbar(@NonNull final Context context) {
    super(context);
  }

  public ProjectActionToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public ProjectActionToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }
}
