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

   const val SCALE_X = "scaleX"
   const val SCALE_Y = "scaleY"
   const val ALPHA = "alpha"

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
    val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.2f, 1.0f)
    val phvY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.2f, 1.0f)
    val scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, phvY).setDuration(100)
    scaleAnimation.interpolator = AccelerateDecelerateInterpolator()
    val scaleAnimationMail = ObjectAnimator.ofPropertyValuesHolder(secondView, pvhX, phvY).setDuration(100)
    scaleAnimationMail.interpolator = AccelerateDecelerateInterpolator()
    val animatorSet = AnimatorSet()
    animatorSet.play(scaleAnimationMail).after(50).after(scaleAnimation)
    animatorSet.start()
  }
}
