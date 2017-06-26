package com.kickstarter.libs.utils

import android.support.design.widget.AppBarLayout
import android.view.View

object ToolbarUtils {

  /**
   * Adds an `offsetChangedListener` to set the toolbar title view's `alpha` based
   * on vertical scroll position. The result is a toolbar title fade effect as the user
   * expands the CollapsingToolbarLayout.
   */
  fun fadeToolbarTitleOnExpand(appBarLayout: AppBarLayout, toolbarTitleView: View) {
    appBarLayout.addOnOffsetChangedListener { layout, verticalOffset ->
      toolbarTitleView.alpha = Math.abs(verticalOffset).toFloat() / layout.totalScrollRange
    }
  }
}
