package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ProjectCardViewBinding
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.ProjectCardViewHolder

class DiscoveryProjectCardAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate :
        ProjectCardViewHolder.Delegate

    fun takeProjects(projects: List<Pair<Project, DiscoveryParams>>) {
        clearSections()

        insertSection(SECTION_PROJECT_CARD_VIEW, emptyList<Pair<Project, DiscoveryParams>>())
        setSection(SECTION_PROJECT_CARD_VIEW, projects)

        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.project_card_view

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return ProjectCardViewHolder(
            ProjectCardViewBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            delegate
        )
    }

    companion object {
        private const val SECTION_PROJECT_CARD_VIEW = 0
    }
}
