package com.kickstarter.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.aghajari.zoomhelper.ZoomHelper
import com.kickstarter.R
import com.kickstarter.databinding.ViewImageWithCaptionBinding
import com.kickstarter.libs.utils.extensions.isGif
import com.kickstarter.ui.extensions.loadGifImage
import com.kickstarter.ui.extensions.loadImage
import com.kickstarter.ui.extensions.makeLinks

@SuppressLint("ClickableViewAccessibility")
class ImageWithCaptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var onImageWithCaptionClickedListener: OnImageWithCaptionClickedListener? = null

    private var binding: ViewImageWithCaptionBinding =
        ViewImageWithCaptionBinding.inflate(
            LayoutInflater.from(context),
            this, true
        )

    fun setImage(src: String) {
        if (src.isGif()) {
            binding.imageView.loadGifImage(src, context)
        } else {
            binding.imageView.loadImage(src, context, binding.imageViewPlaceholder)
            ZoomHelper.addZoomableView(binding.imageView)
            ZoomHelper.removeZoomableView(binding.imageViewPlaceholder)
        }
    }

    fun setImageWithCaptionClickedListener(onImageWithCaptionClickedListener: OnImageWithCaptionClickedListener?) {
        this.onImageWithCaptionClickedListener = onImageWithCaptionClickedListener
    }

    fun setCaption(caption: String, href: String? = null) {
        if (caption.isNotEmpty()) {
            binding.imageCaptionTextView.isVisible = true
            binding.imageCaptionTextView.text = caption
            href?.let {
                binding.imageCaptionTextView.makeLinks(
                    Pair(
                        caption,
                        OnClickListener {
                            onImageWithCaptionClickedListener?.onImageWithCaptionClicked(it)
                        }
                    ),
                    linkColor = R.color.kds_create_700,
                    isUnderlineText = true
                )
            }
        } else binding.imageCaptionTextView.isVisible = false
    }

    fun setLinkOnImage(href: String?) {
        href?.takeIf { it.isNotEmpty() }?.let {
            binding.imageView.isClickable = true
            binding.imageView.setOnClickListener {
                onImageWithCaptionClickedListener?.onImageWithCaptionClicked(it)
            }
        }
    }
}

interface OnImageWithCaptionClickedListener {
    fun onImageWithCaptionClicked(view: View)
}
