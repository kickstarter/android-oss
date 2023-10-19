package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemErrorPaginationBinding
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.PaginationErrorViewHolder

class CommentPaginationErrorAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : PaginationErrorViewHolder.ViewListener

    init {
        insertSection(SECTION_ERROR_PAGINATING, emptyList<Boolean>())
    }

    fun addErrorPaginationCell(shouldShowErrorCell: Boolean) {
        if (shouldShowErrorCell) {
            setSection(SECTION_ERROR_PAGINATING, listOf(true))
        } else {
            setSection(SECTION_ERROR_PAGINATING, emptyList<Boolean>())
        }
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_error_pagination

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return PaginationErrorViewHolder(
            ItemErrorPaginationBinding.inflate(
                LayoutInflater.from(viewGroup.context), viewGroup, false
            ),
            delegate
        )
    }

    companion object {
        private const val SECTION_ERROR_PAGINATING = 0
    }
}
