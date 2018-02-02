package com.kickstarter.libs.utils

import android.support.design.widget.AppBarLayout
import android.view.View
import android.widget.TextView

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

  /**
   * Adds an `offsetChangedListener` to set the toolbar title view's `alpha` and translateY based
   * on vertical scroll position. The result is a toolbar title fade and slide down effect as the user
   * expands the CollapsingToolbarLayout.
   */
  fun fadeAndTranslateToolbarTitleOnExpand(appBarLayout: AppBarLayout, toolbarTitleView: TextView) {
    appBarLayout.addOnOffsetChangedListener { layout, verticalOffset ->
      val alpha = calculateAlphaFromOffset(verticalOffset, layout)
      toolbarTitleView.alpha = alpha
      toolbarTitleView.translationY = toolbarTitleView.height - alpha * toolbarTitleView.height
    }
  }

  private fun calculateAlphaFromOffset(verticalOffset: Int, layout: AppBarLayout) =
    Math.abs(verticalOffset).toFloat() / layout.totalScrollRange
}
