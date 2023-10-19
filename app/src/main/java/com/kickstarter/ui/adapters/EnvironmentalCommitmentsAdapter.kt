package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemEnvironmentalCommitmentsCardBinding
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.viewholders.EnvironmentalCommitmentsViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class EnvironmentalCommitmentsAdapter : KSListAdapter() {

    init {
        insertSection(SECTION_ENV_COMMITMENTS, emptyList<EnvironmentalCommitment>())
    }

    fun takeData(environmentalCommitments: List<EnvironmentalCommitment>) {
        if (environmentalCommitments.isNotEmpty()) {
            setSection(SECTION_ENV_COMMITMENTS, environmentalCommitments)
        }
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_environmental_commitments_card

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return EnvironmentalCommitmentsViewHolder(
            ItemEnvironmentalCommitmentsCardBinding.inflate(
                LayoutInflater.from(
                    viewGroup
                        .context
                ),
                viewGroup,
                false
            )
        )
    }

    companion object {
        private const val SECTION_ENV_COMMITMENTS = 0
    }
}
