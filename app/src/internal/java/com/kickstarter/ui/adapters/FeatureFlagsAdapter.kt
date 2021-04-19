package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.databinding.ItemFeatureFlagBinding
import com.kickstarter.model.FeatureFlagsModel
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class FeatureFlagsAdapter(private val delegate: FeatureFlagViewHolder.Delegate) : KSAdapter() {

    override fun layout(sectionRow: SectionRow): Int = R.layout.item_feature_flag

    override fun viewHolder(layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.item_feature_flag -> FeatureFlagViewHolder(ItemFeatureFlagBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), this.delegate)
            else -> EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }

    fun takeFlags(flags: List<FeatureFlagsModel>) {
        sections().clear()
        sections().add(flags)
    }
}
