package com.kickstarter.ui.views

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter

class WebChromeVideoFullScreenClient(
    val requireActivity: FragmentActivity,
    private val fullScreenDelegate: ViewElementAdapter.FullScreenDelegate? = null,
    private val elementIndex: Int? = null
) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalOrientation = requireActivity.requestedOrientation
    private var originalSystemUiVisibility = requireActivity.window.decorView.systemUiVisibility

    override fun getDefaultVideoPoster(): Bitmap? {
        return if (customView == null) {
            null
        } else BitmapFactory.decodeResource(
            requireActivity.applicationContext?.resources,
            2130837573
        )
    }

    override fun onHideCustomView() {
        (requireActivity.window.decorView as FrameLayout).removeView(customView)
        customView = null
        requireActivity.window.decorView.systemUiVisibility = originalSystemUiVisibility
        requireActivity.requestedOrientation = originalOrientation
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }

    override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
        if (customView != null) {
            onHideCustomView()
            return
        }
        customView = paramView
        originalSystemUiVisibility = requireActivity.window.decorView.systemUiVisibility
        originalOrientation = requireActivity.requestedOrientation
        requireActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        customViewCallback = paramCustomViewCallback
        (requireActivity.window.decorView as FrameLayout).addView(customView, FrameLayout.LayoutParams(-1, -1))
        requireActivity.window.decorView.systemUiVisibility = 3846
    }
}
