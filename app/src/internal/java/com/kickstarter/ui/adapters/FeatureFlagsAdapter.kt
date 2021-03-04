package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class FeatureFlagsAdapter : KSAdapter() {

    override fun layout(sectionRow: SectionRow): Int = R.layout.item_feature_flag

    override fun viewHolder(layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_feature_flag -> FeatureFlagViewHolder(viewGroup)
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    fun takeFlags(flags: List<Pair<String, Boolean>>) {
        sections().clear()
        sections().add(flags)
    }
}
