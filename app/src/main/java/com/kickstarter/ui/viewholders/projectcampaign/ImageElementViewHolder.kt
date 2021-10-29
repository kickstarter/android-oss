package com.kickstarter.ui.viewholders.projectcampaign

import android.widget.ImageView
import com.kickstarter.databinding.ViewElementImageFromHtmlBinding
import com.kickstarter.libs.htmlparser.ImageViewElement
import com.kickstarter.ui.extensions.loadImage
import com.kickstarter.ui.viewholders.KSViewHolder

class ImageElementViewHolder(
    val binding: ViewElementImageFromHtmlBinding
) : KSViewHolder(binding.root) {
    // TODO: attach ViewModel if necessary
    private val imageView: ImageView = binding.imageView

    private fun configure(element: ImageViewElement) {
        imageView.loadImage(element.src, context())
    }

    override fun bindData(data: Any?) {
        (data as? ImageViewElement).apply {
            this?.let { configure(it) }
        }
    }
}
