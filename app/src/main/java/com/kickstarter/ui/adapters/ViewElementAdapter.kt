package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.libs.EmbeddedLinkViewElement
import com.kickstarter.libs.ImageViewElement
import com.kickstarter.libs.TextViewElement
import com.kickstarter.libs.VideoViewElement
import com.kickstarter.libs.ViewElement
import com.kickstarter.ui.viewholders.EmptyViewHolder
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
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.view_element_text_from_html, viewGroup, false)
                return TextElementViewHolder(view)
            }
            ElementViewHolderType.IMAGE.ordinal -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.view_element_image_from_html, viewGroup, false)
                return ImageElementViewHolder(view)
            }
//            ElementViewHolderType.VIDEO.ordinal -> {
//                val view = LayoutInflater.from(viewGroup.context)
//                    .inflate(R.layout.video_row_item, viewGroup, false)
//                return ImageElementViewHolder(view)
//            }
//            ElementViewHolderType.EMBEDDED.ordinal -> {
//                val view = LayoutInflater.from(viewGroup.context)
//                    .inflate(R.layout.embedded_row_item, viewGroup, false)
//                return ImageElementViewHolder(view)
//            }
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val element = elements.currentList[position]

        (element as? TextViewElement)?.let { element ->
            (viewHolder as? TextElementViewHolder)?.let { viewHolder ->
                viewHolder.configure(element)
            }
        }

        (element as? ImageViewElement)?.let {
            (viewHolder as? ImageElementViewHolder)?.let {
                viewHolder.configure(element)
            }
        }
    }
}

// ViewHolders
class TextElementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val textView: TextView = view.findViewById(R.id.text_view)

    fun configure(element: TextViewElement) {
        textView.text = element.attributedText
    }
}

class ImageElementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val imageView: ImageView = view.findViewById(R.id.image_view)

    fun configure(element: ImageViewElement) {
        Picasso.get().load(element.src).into(imageView)
    }
}

enum class ElementViewHolderType {
    TEXT,
    IMAGE,
    VIDEO,
    EMBEDDED,
}
