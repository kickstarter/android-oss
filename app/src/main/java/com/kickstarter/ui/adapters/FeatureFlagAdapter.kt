package com.kickstarter.ui.adapters

import android.view.View
import com.kickstarter.R
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class FeatureFlagAdapter : KSAdapter() {

    override fun layout(sectionRow: SectionRow): Int = R.layout.item_feature_flag

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when (layout) {
            R.layout.item_feature_flag -> FeatureFlagViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    fun takeFlags(flags: List<Map.Entry<String, Boolean>>) {
        sections().clear()
        sections().add(flags)
    }
}