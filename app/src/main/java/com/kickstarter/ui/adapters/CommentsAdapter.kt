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

class CommentsAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : EmptyCommentsViewHolder.Delegate, CommentCardViewHolder.Delegate

    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_comment_card
    }

    fun takeData(comments: List<CommentCardData>) {
        clearSections()
        addSection(comments)
        submitList(items())
    }

    fun insertData(comment: CommentCardData, position: Int) {
        val list = currentList.toMutableList()
        list.add(
            position,
            comment
        )
        takeData(list as List<CommentCardData>)
    }

    fun updateItem(comment: CommentCardData, position: Int) {
        val list = currentList.toMutableList()

        list.add(
            position,
            comment
        )

        takeData(list as List<CommentCardData>)
    }
    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return CommentCardViewHolder(ItemCommentCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate)
    }
}
