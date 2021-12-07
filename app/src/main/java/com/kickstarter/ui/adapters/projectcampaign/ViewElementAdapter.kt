package com.kickstarter.ui.adapters.projectcampaign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ViewElementExternalSourceFromHtmlBinding
import com.kickstarter.databinding.ViewElementImageFromHtmlBinding
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.databinding.ViewElementVideoFromHtmlBinding
import com.kickstarter.libs.htmlparser.ExternalSourceViewElement
import com.kickstarter.libs.htmlparser.ImageViewElement
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.htmlparser.ViewElement
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.ExternalViewViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.ImageElementViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.TextElementViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.VideoElementViewHolder

/**
 * Adapter Specific to hold a list of ViewElements from the HTML Parser
 */
class ViewElementAdapter(val requireActivity: FragmentActivity) : RecyclerView
.Adapter<RecyclerView
.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<ViewElement>() {
        override fun areItemsTheSame(oldItem: ViewElement, newItem: ViewElement): Boolean {
            return areTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: ViewElement, newItem: ViewElement): Boolean {
            return areTheSame(oldItem, newItem)
        }

        private fun areTheSame(oldItem: ViewElement, newItem: ViewElement): Boolean {
            // TODO refactor, can be moved to equals operator in each ViewElement class
            (oldItem as? TextViewElement)?.let {
                val isSameType = newItem is TextViewElement
                return if (isSameType) newItem == oldItem
                else false
            }

            (oldItem as? ImageViewElement)?.let {
                val isSameType = newItem is ImageViewElement
                return if (isSameType) newItem == oldItem
                else false
            }

            (oldItem as? VideoViewElement)?.let {
                val isSameType = newItem is VideoViewElement
                return if (isSameType) newItem == oldItem
                else false
            }

            (oldItem as? ExternalSourceViewElement)?.let {
                val isSameType = newItem is ExternalSourceViewElement
                return if (isSameType) newItem == oldItem
                else false
            }

            return false
        }
    }

    private val elements: AsyncListDiffer<ViewElement> =
        AsyncListDiffer<ViewElement>(this, diffCallback)

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

        (element as? ExternalSourceViewElement)?.let {
            return ElementViewHolderType.EXTERNAL_SOURCES.ordinal
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
            ElementViewHolderType.VIDEO.ordinal -> {
                return VideoElementViewHolder(
                    ViewElementVideoFromHtmlBinding.inflate(
                        LayoutInflater.from(
                            viewGroup
                                .context
                        ),
                        viewGroup,
                        false
                    ),
                    playersMap,
                    requireActivity
                )
            }
            ElementViewHolderType.EXTERNAL_SOURCES.ordinal -> {
                return ExternalViewViewHolder(
                    ViewElementExternalSourceFromHtmlBinding.inflate(
                        LayoutInflater.from(
                            viewGroup
                                .context
                        ),
                        viewGroup,
                        false
                    ),
                    requireActivity
                )
            }
            else -> EmptyViewHolder(
                EmptyViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )
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

        (element as? VideoViewElement)?.let { videoElement ->
            (viewHolder as? VideoElementViewHolder)?.let {
                viewHolder.bindData(videoElement)
            }
        }

        (element as? ExternalSourceViewElement)?.let { externalSourceViewElement ->
            (viewHolder as? ExternalViewViewHolder)?.let {
                viewHolder.bindData(externalSourceViewElement)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {

        (holder as? VideoElementViewHolder)?.let { videoElementViewHolder ->
            videoElementViewHolder.releasePlayer(index = videoElementViewHolder.bindingAdapterPosition)
        }

        super.onViewRecycled(holder)
    }

    private enum class ElementViewHolderType {
        TEXT,
        IMAGE,
        VIDEO,
        EMBEDDED,
        EXTERNAL_SOURCES
    }

    // for hold all players generated
    private var playersMap: MutableMap<Int, SimpleExoPlayer?> = mutableMapOf()

    // for hold current player
    private var currentPlayingVideo: Pair<Int, SimpleExoPlayer?>? = null

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        releaseAllPlayers()
    }

    fun releaseAllPlayers() {
        playersMap.onEachIndexed { index, item ->
            item.value?.release()
            playersMap[index] = null
        }
        playersMap.clear()
        playersMap = mutableMapOf()
        currentPlayingVideo?.second?.release()
        currentPlayingVideo = null
    }

    // call when scroll to pause any playing player
    private fun pauseCurrentPlayingVideo() {
        if (currentPlayingVideo != null) {
            currentPlayingVideo?.second?.playWhenReady = false
        }
    }

    fun playIndexThenPausePreviousPlayer(index: Int) {
        if (playersMap[index]?.playWhenReady == false) {
            pauseCurrentPlayingVideo()
            playersMap[index]?.currentPosition?.let {
                playersMap[index]?.isCurrentWindowSeekable
                if (it != 0L)
                    playersMap[index]?.playWhenReady = true
            }

            currentPlayingVideo = Pair(index, playersMap[index])
        }
    }
}
