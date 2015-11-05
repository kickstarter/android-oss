package com.kickstarter.ui.toolbars;


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

public class ProjectActionToolbar extends KSToolbar {
  public ProjectActionToolbar(@NonNull final Context context) {
    super(context);
  }

  public ProjectActionToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public ProjectActionToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
