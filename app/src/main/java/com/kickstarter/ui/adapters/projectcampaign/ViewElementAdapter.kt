package com.kickstarter.ui.adapters.projectcampaign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ViewElementImageFromHtmlBinding
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.htmlparser.EmbeddedLinkViewElement
import com.kickstarter.libs.htmlparser.ImageViewElement
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.htmlparser.ViewElement
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.ImageElementViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.TextElementViewHolder

/**
 * Adapter Specific to hold a list of ViewElements from the HTML Parser
 */
class ViewElementAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<ViewElement>() {
        override fun areItemsTheSame(oldItem: ViewElement, newItem: ViewElement): Boolean {
            return areTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: ViewElement, newItem: ViewElement): Boolean {
            return areTheSame(oldItem, newItem)
        }

        private fun areTheSame(oldItem: ViewElement, newItem: ViewElement): Boolean {
            (oldItem as? TextViewElement)?.let {
                return newItem is TextViewElement
            }

            (oldItem as? ImageViewElement)?.let {
                return newItem is ImageViewElement
            }

            (oldItem as? VideoViewElement)?.let {
                return newItem is VideoViewElement
            }

            (oldItem as? EmbeddedLinkViewElement)?.let {
                return newItem is EmbeddedLinkViewElement
            }

            return false
        }
    }

    private val elements: AsyncListDiffer<ViewElement> = AsyncListDiffer<ViewElement>(this, diffCallback)

    override fun getItemCount() = elements.currentList.size

    fun submitList(list: List<ViewElement>) {
        elements.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        val element = elements.currentList[position]

        (element as? TextViewElement)?.let {
            return ElementViewHolderType.TEXT.ordinal
        }

        (element as? ImageViewElement)?.let {
            return ElementViewHolderType.IMAGE.ordinal
        }

        (element as? VideoViewElement)?.let {
            return ElementViewHolderType.VIDEO.ordinal
        }

        (element as? EmbeddedLinkViewElement)?.let {
            return ElementViewHolderType.EMBEDDED.ordinal
        }

        return 0
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ElementViewHolderType.TEXT.ordinal -> {
                return TextElementViewHolder(
                    ViewElementTextFromHtmlBinding.inflate(
                        LayoutInflater.from(
                            viewGroup
                                .context
                        ),
                        viewGroup,
                        false
                    )
                )
            }
            ElementViewHolderType.IMAGE.ordinal -> {
                return ImageElementViewHolder(
                    ViewElementImageFromHtmlBinding.inflate(
                        LayoutInflater.from(
                            viewGroup
                                .context
                        ),
                        viewGroup,
                        false
                    )
                )
            }
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val element = elements.currentList[position]

        (element as? TextViewElement)?.let { textElement ->
            (viewHolder as? TextElementViewHolder)?.let { viewHolder ->
                viewHolder.bindData(textElement)
            }
        }

        (element as? ImageViewElement)?.let { imageElement ->
            (viewHolder as? ImageElementViewHolder)?.let {
                viewHolder.bindData(imageElement)
            }
        }
    }

    private enum class ElementViewHolderType {
        TEXT,
        IMAGE,
        VIDEO,
        EMBEDDED,
    }
}
