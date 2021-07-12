package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.databinding.ItemRootCommentCardBinding
import com.kickstarter.databinding.ItemShowMoreRepliesBinding
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RepliesStatusCellType
import com.kickstarter.ui.viewholders.RepliesStatusCellViewHolder
import com.kickstarter.ui.viewholders.RootCommentViewHolder

class RepliesAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        CommentCardViewHolder.Delegate,
        RepliesStatusCellViewHolder.ViewListener

    init {
        insertSection(SECTION_COMMENTS, emptyList<CommentCardData>())
        insertSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        insertSection(SECTION_ROOT_COMMENT, emptyList<Comment>())
    }

    fun takeData(replies: List<CommentCardData>, shouldViewMoreRepliesCell: Boolean) {
        if (replies.isNotEmpty()) {
            setSection(SECTION_COMMENTS, replies)
            if (shouldViewMoreRepliesCell)
                setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, listOf(RepliesStatusCellType.VIEW_MORE))
            else
                setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        } else {
            setSection(SECTION_COMMENTS, emptyList<CommentCardData>())
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        }

        submitList(items())
    }

    fun updateRootCommentCell(rootComment: Comment) {
        setSection(SECTION_COMMENTS, emptyList<CommentCardData>())
        setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        setSection(SECTION_ROOT_COMMENT, listOf(rootComment))
        submitList(items())
    }

    fun addErrorPaginationCell(shouldShowErrorCell: Boolean) {
        // - we want to display SECTION_COMMENTS & SECTION_ERROR_PAGINATING at the same time so we should not clean SECTION_COMMENTS
        if (shouldShowErrorCell)
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, listOf(RepliesStatusCellType.PAGINATION_ERROR))
        else
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()) {
        SECTION_ROOT_COMMENT -> R.layout.item_root_comment_card
        SECTION_COMMENTS -> R.layout.item_comment_card
        SECTION_SHOW_MORE_REPLIES_PAGINATING -> R.layout.item_show_more_replies
        else -> 0
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_root_comment_card -> RootCommentViewHolder(ItemRootCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            R.layout.item_show_more_replies -> RepliesStatusCellViewHolder(
                ItemShowMoreRepliesBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate
            )
            else -> CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate, true)
        }
    }

    companion object {
        private const val SECTION_COMMENTS = 0
        private const val SECTION_SHOW_MORE_REPLIES_PAGINATING = 1
        private const val SECTION_ROOT_COMMENT = 2
    }
}
