package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemRootCommentCardBinding
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RootCommentViewHolder

/** Replies Root comment cell adapter **/
class RootCommentAdapter : KSListAdapter() {

    fun updateRootCommentCell(rootComment: CommentCardData) {
        addSection(listOf(rootComment))
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_root_comment_card

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return RootCommentViewHolder(
            ItemRootCommentCardBinding.inflate(
                LayoutInflater.from(viewGroup.context), viewGroup, false
            )
        )
    }
}
