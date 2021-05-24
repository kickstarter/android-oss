package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyCommentsLayoutBinding
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.models.Comment
import com.kickstarter.models.Empty
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class CommentsAdapter(private val delegate: Delegate) : KSAdapter() {
    interface Delegate : EmptyCommentsViewHolder.Delegate, CommentCardViewHolder.Delegate

    init {
        insertSection(SECTION_EMPTY_VIEW, emptyList<Any>())
        insertSection(SECTION_COMMENTS_VIEW, emptyList<Any>())
    }
    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return if (sectionRow.section() == SECTION_COMMENTS_VIEW) {
            R.layout.item_comment_card
        } else {
            R.layout.empty_comments_layout
        }
    }

    fun takeData(comments: List<Comment>) {

        if (comments.isEmpty()) {
            setSection(SECTION_EMPTY_VIEW, listOf(Empty.get()))
        } else {
            setSection(SECTION_COMMENTS_VIEW, comments)
        }

        notifyDataSetChanged()
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return if (layout == R.layout.item_comment_card) {
            CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
        } else {
            EmptyCommentsViewHolder(EmptyCommentsLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
        }
    }

    companion object {
        private const val SECTION_EMPTY_VIEW = 0
        private const val SECTION_COMMENTS_VIEW = 1
    }
}
