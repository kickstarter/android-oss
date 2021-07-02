package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class RepliesAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : CommentCardViewHolder.Delegate

    init {
        insertSection(SECTION_REPLIES, emptyList<CommentCardData>())
    }
    fun takeData(replies: List<CommentCardData>) {
        setSection(SECTION_REPLIES, replies)
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_comment_card
    }

    override fun viewHolder(layout: Int, view: ViewGroup): KSViewHolder {
        return CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(view.context), view, false), delegate)
    }

    companion object {
        private const val SECTION_REPLIES = 0
    }
}
