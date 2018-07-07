package com.kickstarter.libs.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Interpolator

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

  @JvmOverloads
  fun fadeInAndScale(view: View, duration: Long = 500L, startDelay: Long = 500L,
                     interpolator: Interpolator = AccelerateDecelerateInterpolator()): ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat("scaleY", 0f, 1f)
    val alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
    val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha)
    animator.duration = duration
    animator.startDelay = startDelay
    animator.interpolator = interpolator
    return animator
  }

  @JvmOverloads
  fun fadeOutAndScale(view: View, duration: Long = 500L, startDelay: Long = 500L,
                      interpolator: Interpolator = AccelerateDecelerateInterpolator()): ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f)
    val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f)
    val alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
    val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha)
    animator.duration = duration
    animator.startDelay = startDelay
    animator.interpolator = interpolator
    return animator
  }

  @JvmOverloads
  fun crossFade(visibleView: View, hiddenView: View, crossDuration: Long = 500L, startDelay: Long = 500L,
                      interpolator: Interpolator = AccelerateDecelerateInterpolator()): AnimatorSet {
    val crossFadeAnimatorSet = AnimatorSet()
    val fadeOutAndScale = fadeOutAndScale(visibleView, crossDuration, startDelay, interpolator)
    val fadeInAndScale = fadeInAndScale(hiddenView, crossDuration, startDelay, interpolator)
    
    crossFadeAnimatorSet.playTogether(fadeOutAndScale, fadeInAndScale)

    return crossFadeAnimatorSet
  }

  @JvmOverloads
  fun crossFadeAndReverse(visibleView: View, hiddenView: View, crossDuration: Long = 500L, startDelay: Long = 500L,
                      interpolator: Interpolator = AccelerateDecelerateInterpolator()): AnimatorSet {
    val crossFadeAndReverseAnimatorSet = AnimatorSet()
    val startAnimation = crossFade(visibleView, hiddenView, crossDuration, startDelay, interpolator)
    val endAnimation = crossFade(hiddenView, visibleView, crossDuration, startDelay, interpolator)
    crossFadeAndReverseAnimatorSet.playSequentially(startAnimation, endAnimation)
    return crossFadeAndReverseAnimatorSet
  }
}
