package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class CommentsAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : CommentCardViewHolder.Delegate

    init {
        insertSection(SECTION_COMMENTS, emptyList<CommentCardData>())
    }

    fun takeData(comments: List<CommentCardData>) {
        if (comments.isNotEmpty()) {
            setSection(SECTION_COMMENTS, comments)
        }
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_comment_card

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
    }

    companion object {
        private const val SECTION_COMMENTS = 0
    }
}
