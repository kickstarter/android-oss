package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.databinding.ItemErrorPaginationBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class CommentsAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : EmptyCommentsViewHolder.Delegate, CommentCardViewHolder.Delegate

    init {
        insertSection(SECTION_COMENTS, emptyList<CommentCardData>())
        insertSection(SECCTION_ERROR_PAGINATING, emptyList<Throwable>())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()) {
        SECTION_COMENTS -> R.layout.item_comment_card
        SECCTION_ERROR_PAGINATING -> R.layout.item_error_pagination
        else -> 0
    }

    fun takeData(comments: List<CommentCardData>) {
        setSection(SECTION_COMENTS, comments)
        // - Clean every section different from the comments section if adding more comments
        setSection(SECCTION_ERROR_PAGINATING, emptyList<Boolean>())
        submitList(items())
    }

    fun addErrorPaginationCell() {
        // - adding 1 element no clearing previous sections
        setSection(SECCTION_ERROR_PAGINATING, listOf(true))
        submitList(items())
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_comment_card -> CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
            R.layout.item_error_pagination -> EmptyViewHolder(ItemErrorPaginationBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    companion object {
        private const val SECTION_COMENTS = 0
        private const val SECCTION_ERROR_PAGINATING = 1
    }
}
