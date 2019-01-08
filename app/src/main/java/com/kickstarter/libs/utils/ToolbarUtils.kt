package com.kickstarter.libs.utils

import android.view.View
import android.widget.TextView
import com.google.android.material.appbar.AppBarLayout

object ToolbarUtils {

  /**
   * Adds an `offsetChangedListener` to set the toolbar title view's `alpha` based
   * on vertical scroll position. The result is a toolbar title fade effect as the user
   * expands the CollapsingToolbarLayout.
   */
  fun fadeToolbarTitleOnExpand(appBarLayout: AppBarLayout, toolbarTitleView: View) {
    appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
      toolbarTitleView.alpha = calculateAlphaFromOffset(verticalOffset, appBarLayout)
    })
  }

  /**
   * Adds an `offsetChangedListener` to set the toolbar title view's `alpha` and translateY based
   * on vertical scroll position. The result is a toolbar title fade and slide down effect as the user
   * expands the CollapsingToolbarLayout more than half of its height.
   */
  fun fadeAndTranslateToolbarTitleOnExpand(appBarLayout: AppBarLayout, toolbarTitleView: TextView) {
    appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
      if (Math.abs(verticalOffset) > appBarLayout.totalScrollRange / 2) {
        val alpha = calculateAlphaFromOffset(verticalOffset, appBarLayout)
        toolbarTitleView.alpha = alpha
        toolbarTitleView.translationY = toolbarTitleView.height - alpha * toolbarTitleView.height
      } else {
        toolbarTitleView.alpha = 0f
      }
    })
  }

  private fun calculateAlphaFromOffset(verticalOffset: Int, layout: AppBarLayout) =
    Math.abs(verticalOffset).toFloat() / layout.totalScrollRange
}
