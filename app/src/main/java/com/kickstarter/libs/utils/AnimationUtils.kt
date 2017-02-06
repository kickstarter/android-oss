package com.kickstarter.libs.utils

import android.view.animation.AlphaAnimation
import android.view.animation.Animation

object AnimationUtils {

  @JvmOverloads
  fun disappearAnimation(duration: Long = 300L): Animation {
    val animation = AlphaAnimation(1.0f, 0.0f)
    animation.duration = duration
    animation.fillAfter = true
    return animation
  }

  @JvmOverloads
  fun appearAnimation(duration: Long = 300L): Animation {
    val animation = AlphaAnimation(0.0f, 1.0f)
    animation.duration = duration
    animation.fillAfter = true
    return animation
  }
}
