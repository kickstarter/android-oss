package com.kickstarter.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale.Companion.FillWidth
import androidx.core.view.isVisible
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.kickstarter.R
import com.kickstarter.databinding.ViewImageWithCaptionBinding
import com.kickstarter.libs.utils.extensions.isGif
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isWebp
import com.kickstarter.ui.extensions.loadGifImage
import com.kickstarter.ui.extensions.loadWebp
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
        if (src.isEmpty() || src.isBlank()) {
            binding.imageView.setImageDrawable(null)
            binding.composeViewImage.visibility = GONE
        } else {
            when {
                src.isWebp() -> {
                    binding.imageView.loadWebp(src, context)
                    binding.imageView.visibility = VISIBLE
                    binding.composeViewImage.visibility = GONE
                }

                src.isGif() -> {
                    binding.imageView.visibility = VISIBLE
                    binding.imageView.loadGifImage(src, context)
                    binding.composeViewImage.visibility = GONE
                }

                else -> {
                    binding.composeViewImage.visibility = VISIBLE
                    binding.imageView.visibility = GONE
                    binding.composeViewImage.setContent {
                        if (src.isNotNull()) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SubcomposeAsyncImage(
                                    model = src,
                                    contentDescription = "null",
                                    contentScale = FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val state = painter.state
                                    if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                                        LinearProgressIndicator()
                                    } else {
                                        SubcomposeAsyncImageContent()
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
