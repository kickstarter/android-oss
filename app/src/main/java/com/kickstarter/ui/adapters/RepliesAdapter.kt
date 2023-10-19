package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

/** Replies list adapter to show replies list **/
class RepliesAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        CommentCardViewHolder.Delegate

    init {
        insertSection(SECTION_COMMENTS, emptyList<CommentCardData>())
    }

    fun takeData(replies: List<CommentCardData>) {
        if (replies.isNotEmpty()) {
            setSection(SECTION_COMMENTS, replies)
        } else {
            setSection(SECTION_COMMENTS, emptyList<CommentCardData>())
        }

        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_comment_card

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate, true)
    }

    companion object {
        private const val SECTION_COMMENTS = 0
    }
}
