package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.databinding.ViewImageWithCaptionBinding
import com.kickstarter.libs.utils.extensions.isGif
import com.kickstarter.ui.extensions.loadGifImage
import com.kickstarter.ui.extensions.loadImage
import com.kickstarter.ui.extensions.makeLinks

class ImageWithCaptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var binding: ViewImageWithCaptionBinding =
        ViewImageWithCaptionBinding.inflate(
            LayoutInflater.from(context),
            this, true
        )

    fun setImage(src: String) {
        if (src.isGif()) {
            binding.imageView.loadGifImage(src, context)
        } else {
            binding.imageView.loadImage(src, context)
        }
    }

    fun setImage(@DrawableRes drawable: Int) {
        ContextCompat.getDrawable(context, drawable)?.let {
            binding.imageView.setImageDrawable(it)
        }
    }

    fun setCaption(caption: String, href: String? = null) {
        binding.imageCaptionTextView.text = caption
        href?.let {
            binding.imageCaptionTextView.makeLinks(
                Pair(
                    caption,
                    OnClickListener {
                        // onCommentCardClickedListener?.onShowCommentClicked(it)
                    }
                ),
                linkColor = R.color.kds_create_700,
                isUnderlineText = false
            )
        }
    }

    fun setCaption(@StringRes caption: Int) {
        binding.imageCaptionTextView.setText(caption)
    }
}
