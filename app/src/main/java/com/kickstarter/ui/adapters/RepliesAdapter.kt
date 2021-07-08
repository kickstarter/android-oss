package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.databinding.ItemErrorPaginationBinding
import com.kickstarter.databinding.ItemShowMoreRepliesBinding
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.PaginationErrorViewHolder
import com.kickstarter.ui.viewholders.PaginationViewMoreRepliesViewHolder
import com.kickstarter.ui.viewholders.RootCommentViewHolder

class RepliesAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        CommentCardViewHolder.Delegate,
        PaginationViewMoreRepliesViewHolder.ViewListener,
        PaginationErrorViewHolder.ViewListener

    init {
        insertSection(SECTION_COMMENTS, emptyList<CommentCardData>())
        insertSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<Boolean>())
        insertSection(SECTION_ERROR_PAGINATING, emptyList<Boolean>())
        insertSection(SECTION_ROOT_COMMENT, emptyList<Comment>())
    }

    fun takeData(replies: List<CommentCardData>, shouldViewMoreRepliesCell: Boolean) {
        if (replies.isNotEmpty()) {
            setSection(SECTION_COMMENTS, replies)
            if (shouldViewMoreRepliesCell)
                setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, listOf(shouldViewMoreRepliesCell))
            else
                setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<Boolean>())
        }
        setSection(SECTION_ERROR_PAGINATING, emptyList<Boolean>())

        submitList(items())
    }

    fun updateRootCommentCell(rootComment: Comment) {
        setSection(SECTION_ROOT_COMMENT, listOf(rootComment))
        submitList(items())
    }

    fun addErrorPaginationCell(shouldShowErrorCell: Boolean) {
        setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, listOf(false))
        // - we want to display SECTION_COMMENTS & SECTION_ERROR_PAGINATING at the same time so we should not clean SECTION_COMMENTS
        setSection(SECTION_ERROR_PAGINATING, listOf(shouldShowErrorCell))
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()) {
        SECTION_ROOT_COMMENT -> R.layout.item_comment_card
        SECTION_ERROR_PAGINATING -> R.layout.item_error_pagination
        SECTION_SHOW_MORE_REPLIES_PAGINATING -> R.layout.item_show_more_replies
        else -> 0
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_comment_card -> RootCommentViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            R.layout.item_error_pagination -> PaginationErrorViewHolder(ItemErrorPaginationBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate, true)
            R.layout.item_show_more_replies -> PaginationViewMoreRepliesViewHolder(
                ItemShowMoreRepliesBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate
            )
            else -> CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate, true)
        }
    }

    companion object {
        private const val SECTION_COMMENTS = 0
        private const val SECTION_SHOW_MORE_REPLIES_PAGINATING = 1
        private const val SECTION_ERROR_PAGINATING = 2
        private const val SECTION_ROOT_COMMENT = 3
    }
}
