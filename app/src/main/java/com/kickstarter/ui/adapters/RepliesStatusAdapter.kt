package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemShowMoreRepliesBinding
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RepliesStatusCellType
import com.kickstarter.ui.viewholders.RepliesStatusCellViewHolder

class RepliesStatusAdapter(private val delegate: RepliesAdapter.Delegate) : KSListAdapter() {
    interface Delegate :
        RepliesStatusCellViewHolder.ViewListener
    init {
        insertSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
    }

    fun addViewMoreCell(shouldViewMoreRepliesCell: Boolean) {

        if (shouldViewMoreRepliesCell)
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, listOf(RepliesStatusCellType.VIEW_MORE))
        else
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        submitList(items())
    }

    fun addErrorPaginationCell(shouldShowErrorCell: Boolean) {
        if (shouldShowErrorCell)
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, listOf(RepliesStatusCellType.PAGINATION_ERROR))
        else
            setSection(SECTION_SHOW_MORE_REPLIES_PAGINATING, emptyList<RepliesStatusCellType>())
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow): Int = R.layout.item_show_more_replies

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return RepliesStatusCellViewHolder(
            ItemShowMoreRepliesBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), delegate
        )
    }

    companion object {
        private const val SECTION_SHOW_MORE_REPLIES_PAGINATING = 0
    }
}
