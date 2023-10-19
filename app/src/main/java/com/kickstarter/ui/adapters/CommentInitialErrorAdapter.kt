package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.CommentInitialLoadErrorLayoutBinding
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class CommentInitialErrorAdapter : KSListAdapter() {

    init {
        insertSection(SECTION_INITIAL_LOAD_ERROR, emptyList<Boolean>())
    }

    fun insertPageError(shouldShowErrorCell: Boolean) {
        if (shouldShowErrorCell) {
            setSection(SECTION_INITIAL_LOAD_ERROR, listOf(true))
        } else {
            setSection(SECTION_INITIAL_LOAD_ERROR, emptyList<Boolean>())
        }
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.comment_initial_load_error_layout

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return EmptyViewHolder(CommentInitialLoadErrorLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    companion object {
        private const val SECTION_INITIAL_LOAD_ERROR = 0
    }
}
