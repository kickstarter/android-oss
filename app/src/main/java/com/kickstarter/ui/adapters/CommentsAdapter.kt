package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.CommentInitialLoadErrorLayoutBinding
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.viewholders.InitialCommentLoadErrorViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class CommentsAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : EmptyCommentsViewHolder.Delegate, CommentCardViewHolder.Delegate

    companion object {
        private const val SECTION_INITIAL_LOAD_ERROR = 0
        private const val SECTION_COMMENT_CARD = 1
    }

    init {
        resetList()
    }

    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return when (sectionRow.section()) {
            SECTION_COMMENT_CARD -> R.layout.item_comment_card
            SECTION_INITIAL_LOAD_ERROR -> R.layout.comment_initial_load_error_layout
            else -> 0
        }
    }

    private fun resetList() {
        clearSections()
        insertSection(SECTION_INITIAL_LOAD_ERROR, emptyList<Boolean>())
        insertSection(SECTION_COMMENT_CARD, emptyList<CommentCardData>())
    }

    fun takeData(comments: List<CommentCardData>) {
        resetList()
        setSection(SECTION_COMMENT_CARD, comments)
        submitList(items())
    }

    fun insertPageError() {
        resetList()
        setSection(SECTION_INITIAL_LOAD_ERROR, listOf(true))
        submitList(items())
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_comment_card -> CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
            R.layout.comment_initial_load_error_layout -> InitialCommentLoadErrorViewHolder(
                CommentInitialLoadErrorLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            )
            else -> InitialCommentLoadErrorViewHolder(
                CommentInitialLoadErrorLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            )
        }
    }
}
