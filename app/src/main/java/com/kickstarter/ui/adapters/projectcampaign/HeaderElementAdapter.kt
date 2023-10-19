package com.kickstarter.ui.adapters.projectcampaign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementHeaderBinding
import com.kickstarter.ui.adapters.KSListAdapter
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.projectcampaign.HeaderElementViewHolder

class HeaderElementAdapter : KSListAdapter() {
    fun updateTitle(title: String) {
        addSection(listOf(title))
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.view_element_header

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return HeaderElementViewHolder(
            ViewElementHeaderBinding.inflate(
                LayoutInflater.from(viewGroup.context), viewGroup, false
            )
        )
    }
}
