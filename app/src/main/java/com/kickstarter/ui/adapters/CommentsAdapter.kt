package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.CommentInitialLoadErrorLayoutBinding
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.PaginationErrorViewHolder

class CommentsAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : CommentCardViewHolder.Delegate, PaginationErrorViewHolder.ViewListener

    init {
        insertSection(SECTION_INITIAL_LOAD_ERROR, emptyList<Boolean>())
        insertSection(SECTION_COMMENTS, emptyList<CommentCardData>())
    }

    fun takeData(comments: List<CommentCardData>) {
        setSection(SECTION_INITIAL_LOAD_ERROR, emptyList<Boolean>())
        if (comments.isNotEmpty()) {
            setSection(SECTION_COMMENTS, comments)
        }

        submitList(items())
    }

    fun insertPageError() {
        setSection(SECTION_INITIAL_LOAD_ERROR, listOf(true))
        setSection(SECTION_COMMENTS, emptyList<CommentCardData>())

        submitList(items())
    }

    override fun layout(sectionRow: SectionRow): Int = when (sectionRow.section()) {
        SECTION_COMMENTS -> R.layout.item_comment_card
        SECTION_INITIAL_LOAD_ERROR -> R.layout.comment_initial_load_error_layout
        else -> R.layout.empty_view
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_comment_card -> CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
            R.layout.comment_initial_load_error_layout -> EmptyViewHolder(CommentInitialLoadErrorLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    companion object {
        private const val SECTION_INITIAL_LOAD_ERROR = 0
        private const val SECTION_COMMENTS = 1
    }
}
