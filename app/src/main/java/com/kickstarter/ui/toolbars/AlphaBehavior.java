package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;


public class AlphaBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  public AlphaBehavior() {
  }
  public AlphaBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof AppBarLayout;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    AppBarLayout appBarLayout = (AppBarLayout) dependency;

    float translationY = dependency.getY();
    float percentComplete = -translationY / appBarLayout.getTotalScrollRange();

    child.setY(dependency.getBottom());
    child.setAlpha(percentComplete);
    return false;
  }

  @Override
  public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

    /* check if position 0 is visible, check its top compared to the view pagers top? also need top of parent !_!
      * distance btween pager top and paremt top vs position 0 */
  }
}
