package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.MediaHeaderBinding
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.toVisibility
import com.squareup.picasso.Picasso
import rx.Observable
import rx.subjects.PublishSubject

class MediaHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout (context, attrs, defStyleAttr) {
    private var binding: MediaHeaderBinding = MediaHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    private var playButtonClicks = PublishSubject.create<Void>()

    interface Inputs {
        /**
         * Sets the visibility of the play button
         */
        fun setProjectPhoto(photo: String)

        /**
         * Sets the visibility of the play button
         */
        fun setPlayButtonVisibility(isVisible: Boolean)
    }

    interface Outputs {

        fun playButtonClicks(): Observable<Void>
    }

    val inputs: Inputs = object : Inputs {
        override fun setPlayButtonVisibility(isVisible: Boolean) {
            binding.videoPlayButtonOverlay.visibility = isVisible.toVisibility()
        }

        override fun setProjectPhoto(photo: String) {
            val targetImageWidth = (ViewUtils.getScreenWidthDp(context) * ViewUtils.getScreenDensity(context)).toInt() - context.resources.getDimension(
                R.dimen.grid_2).toInt() * 2
            val targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth)
            binding.videoProjectPhoto.maxHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth)

            ResourcesCompat.getDrawable(context.resources, R.drawable.gray_gradient, null)?.let {
                Picasso.get()
                    .load(photo)
                    .resize(targetImageWidth, targetImageHeight)
                    .centerCrop()
                    .placeholder(it)
                    .into(binding.videoProjectPhoto)
            }
        }
    }

    val outputs: Outputs = object : Outputs {
        override fun playButtonClicks(): Observable<Void> = playButtonClicks
    }

    init {
        binding.videoPlayButtonOverlay.setOnClickListener {
            playButtonClicks.onNext(null)
        }
    }
}