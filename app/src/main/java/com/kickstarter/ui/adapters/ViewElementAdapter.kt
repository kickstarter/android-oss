package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ViewElementImageFromHtmlBinding
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.libs.EmbeddedLinkViewElement
import com.kickstarter.libs.ImageViewElement
import com.kickstarter.libs.TextViewElement
import com.kickstarter.libs.VideoViewElement
import com.kickstarter.libs.ViewElement
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.squareup.picasso.Picasso

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

        (element as? TextViewElement)?.let { element ->
            (viewHolder as? TextElementViewHolder)?.let { viewHolder ->
                viewHolder.bindData(element)
            }
        }

        (element as? ImageViewElement)?.let {
            (viewHolder as? ImageElementViewHolder)?.let {
                viewHolder.bindData(element)
            }
        }
    }
}

// ViewHolders
class TextElementViewHolder(
    val binding: ViewElementTextFromHtmlBinding
) : KSViewHolder(binding.root) {
    // TODO: attach ViewModel if necessary
    private val textView: TextView = binding.textView

    fun configure(element: TextViewElement) {
        textView.text = element.attributedText
    }

    override fun bindData(data: Any?) {
        (data as? TextViewElement).apply {
            textView.text = this?.attributedText ?: ""
        }
    }
}

class ImageElementViewHolder(
    val binding: ViewElementImageFromHtmlBinding
) : KSViewHolder(binding.root) {
    // TODO: attach ViewModel if necessary
    private val imageView: ImageView = binding.imageView

    private fun configure(element: ImageViewElement) {
        Picasso.get().load(element.src).into(imageView)
    }

    override fun bindData(data: Any?) {
        (data as? ImageViewElement).apply {
            this?.let { configure(it) }
        }
    }
}

enum class ElementViewHolderType {
    TEXT,
    IMAGE,
    VIDEO,
    EMBEDDED,
}
