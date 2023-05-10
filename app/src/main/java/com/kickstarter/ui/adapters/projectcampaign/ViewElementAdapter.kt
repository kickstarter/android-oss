package com.kickstarter.ui.adapters.projectcampaign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ViewElementAudioFromHtmlBinding
import com.kickstarter.databinding.ViewElementExternalSourceFromHtmlBinding
import com.kickstarter.databinding.ViewElementImageFromHtmlBinding
import com.kickstarter.databinding.ViewElementTextFromHtmlBinding
import com.kickstarter.databinding.ViewElementVideoFromHtmlBinding
import com.kickstarter.libs.KSLifecycleEvent
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.htmlparser.ExternalSourceViewElement
import com.kickstarter.libs.htmlparser.ImageViewElement
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.htmlparser.ViewElement
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.AudioElementViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.ExternalViewViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.ImageElementViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.TextElementViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.VideoElementViewHolder
import io.reactivex.subjects.BehaviorSubject

/**
 * Adapter Specific to hold a list of ViewElements from the HTML Parser
 */
class ViewElementAdapter(
    val requireActivity: FragmentActivity,
    private val fullScreenDelegate: FullScreenDelegate,
    private val lifecycleObservable: BehaviorSubject<KSLifecycleEvent> = BehaviorSubject.create()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DefaultLifecycleObserver {

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

            (oldItem as? AudioViewElement)?.let {
                val isSameType = newItem is AudioViewElement
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

        (element as? AudioViewElement)?.let {
            return ElementViewHolderType.AUDIO.ordinal
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
                    fullScreenDelegate,
                    requireActivity
                )
            }
            ElementViewHolderType.AUDIO.ordinal -> {
                return AudioElementViewHolder(
                    ViewElementAudioFromHtmlBinding.inflate(
                        LayoutInflater.from(
                            viewGroup
                                .context
                        ),
                        viewGroup,
                        false
                    ),
                    lifecycleObservable
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
                    fullScreenDelegate,
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

        (element as? AudioViewElement)?.let { audioElement ->
            (viewHolder as? AudioElementViewHolder)?.let {
                viewHolder.setIsRecyclable(false)
                viewHolder.bindData(audioElement)
            }
        }

        (element as? ExternalSourceViewElement)?.let { externalSourceViewElement ->
            (viewHolder as? ExternalViewViewHolder)?.let {
                viewHolder.bindData(externalSourceViewElement)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as? AudioElementViewHolder)?.setIsRecyclable(false)
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as? AudioElementViewHolder)?.let {
            it.setIsRecyclable(false)
            it.pausePlayer()
        }

        (holder as? KSViewHolder)?.let { it.destroy() }

        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? VideoElementViewHolder)?.let { videoElementViewHolder ->
            videoElementViewHolder.releasePlayer(index = videoElementViewHolder.bindingAdapterPosition)
        }
        (holder as? ImageElementViewHolder)?.let {
            it.binding.imageView.setImage("")
            it.binding.imageView.setCaption("")
        }

        super.onViewRecycled(holder)
    }

    private enum class ElementViewHolderType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        EMBEDDED,
        EXTERNAL_SOURCES
    }

    fun playIndexThenPausePreviousPlayer(index: Int) {
        VideoElementViewHolder.playIndexThenPausePreviousPlayer(index)
    }

    fun releaseAllPlayers() {
        VideoElementViewHolder.releaseAllPlayers()
    }

    fun releasePlayersOnPause() {
        VideoElementViewHolder.releasePlayersOnPause()
    }

    fun setPlayerSeekPosition(index: Int, seekPosition: Long) {
        VideoElementViewHolder.setPlayerSeekPosition(index, seekPosition)
    }

    override fun onPause(owner: LifecycleOwner) {
        return lifecycleObservable.onNext(KSLifecycleEvent.PAUSE)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        return lifecycleObservable.onNext(KSLifecycleEvent.DESTROY)
    }

    interface FullScreenDelegate {
        fun onFullScreenOpened(index: Int, source: String, seekPosition: Long = 0L)
    }
}
