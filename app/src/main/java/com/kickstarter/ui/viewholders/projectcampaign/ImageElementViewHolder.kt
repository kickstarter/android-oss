package com.kickstarter.ui.viewholders.projectcampaign

import android.view.View
import com.kickstarter.databinding.ViewElementImageFromHtmlBinding
import com.kickstarter.libs.htmlparser.ImageViewElement
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.views.OnImageWithCaptionClickedListener

class ImageElementViewHolder(
    val binding: ViewElementImageFromHtmlBinding
) : KSViewHolder(binding.root) {
    private val imageView = binding.imageView

    private fun configure(element: ImageViewElement) {
        imageView.setImage(element.src)
        imageView.setLinkOnImage(element.href)
        element.caption?.let { caption ->
            if (element.href.isNullOrEmpty()) {
                imageView.setCaption(caption)
            } else {
                imageView.setCaption(caption, element.href)
            }
        }

        imageView.setImageWithCaptionClickedListener(object : OnImageWithCaptionClickedListener {
            override fun onImageWithCaptionClicked(view: View) {
                element.href?.let { ApplicationUtils.openUrlExternally(context(), it) }
            }
        })
    }

    override fun bindData(data: Any?) {
        (data as? ImageViewElement).apply {
            this?.let {
                configure(it)
            }
        }
    }
}
