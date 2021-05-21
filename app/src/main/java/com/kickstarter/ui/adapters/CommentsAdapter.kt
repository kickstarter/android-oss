package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyCommentsLayoutBinding
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.models.Comment
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import rx.Observable

class CommentsAdapter(private val delegate: Delegate) : KSAdapter() {
    interface Delegate : EmptyCommentsViewHolder.Delegate, CommentCardViewHolder.Delegate

    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return if (sectionRow.section() == 0) {
            R.layout.item_comment_card
        } else {
            R.layout.empty_comments_layout
        }
    }

    fun takeData(comments: List<Comment>) {
        sections().clear()
        addSection(Observable.from(comments).toList().toBlocking().single())

        if (comments.isEmpty()) {
            sections().add(emptyList())
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
}
