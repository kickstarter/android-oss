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

   const val ALPHA = "alpha"
   const val INITIAL_SCALE = 1.0f
   const val MAX_SCALE = 1.3f
   const val SCALE_X = "scaleX"
   const val SCALE_Y = "scaleY"

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
    val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 0f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 0f, 1f)
    val alpha = PropertyValuesHolder.ofFloat(ALPHA, 0f, 1f)
    val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha)
    animator.duration = duration
    animator.startDelay = startDelay
    animator.interpolator = interpolator
    return animator
  }

  @JvmOverloads
  fun fadeOutAndScale(view: View, duration: Long = 500L, startDelay: Long = 500L,
                      interpolator: Interpolator = AccelerateDecelerateInterpolator()): ObjectAnimator {
    val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1f, 0f)
    val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1f, 0f)
    val alpha = PropertyValuesHolder.ofFloat(ALPHA, 1f, 0f)
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

  fun notificationBounceAnimation(view: View, secondView: View) {
    val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, INITIAL_SCALE, MAX_SCALE, INITIAL_SCALE)
    val phvY = PropertyValuesHolder.ofFloat(View.SCALE_Y, INITIAL_SCALE, MAX_SCALE, INITIAL_SCALE)
    val phoneScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, phvY).setDuration(200)
    phoneScaleAnimation.interpolator = AccelerateDecelerateInterpolator()
    val mailScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(secondView, pvhX, phvY).setDuration(200)
    mailScaleAnimation.interpolator = AccelerateDecelerateInterpolator()
    val animatorSet = AnimatorSet()
    animatorSet.play(phoneScaleAnimation)
    animatorSet.play(mailScaleAnimation).after(100)
    animatorSet.start()
  }
}
