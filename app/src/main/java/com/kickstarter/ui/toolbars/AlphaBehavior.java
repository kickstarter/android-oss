package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class AlphaBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  public AlphaBehavior() {
  }

  public AlphaBehavior(final @NonNull Context context, final @NonNull AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(final CoordinatorLayout parent, final View child, final View dependency) {
    return dependency instanceof AppBarLayout;
  }

  @Override
  public boolean onDependentViewChanged(final CoordinatorLayout parent, final View child, final View dependency) {
    final AppBarLayout appBarLayout = (AppBarLayout) dependency;

    final float translationY = dependency.getY();
    final float percentComplete = -translationY / appBarLayout.getTotalScrollRange();

    child.setY(dependency.getBottom());
    child.setAlpha(percentComplete);
    return false;
  }
}
