package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class CommentsAdapter(private val delegate: Delegate) : KSAdapter() {
    interface Delegate : EmptyCommentsViewHolder.Delegate, CommentCardViewHolder.Delegate

    init {
        insertSection(SECTION_COMMENTS_VIEW, emptyList<Any>())
    }
    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_comment_card
    }

    fun takeData(comments: List<CommentCardData>) {
        setSection(SECTION_COMMENTS_VIEW, comments)

        notifyDataSetChanged()
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
    }

    companion object {
        private const val SECTION_COMMENTS_VIEW = 0
    }
}
