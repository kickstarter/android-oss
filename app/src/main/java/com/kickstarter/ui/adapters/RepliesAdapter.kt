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

    fun takeData(replies: List<CommentCardData>) {
        addSection(replies)
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_comment_card
    }

    override fun viewHolder(layout: Int, view: ViewGroup): KSViewHolder {
        return CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(view.context), view, false), delegate)
    }
}
