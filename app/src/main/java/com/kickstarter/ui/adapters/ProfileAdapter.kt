package com.kickstarter.ui.adapters

import android.view.View
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.models.Empty
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.EmptyProfileViewHolder
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.ProfileCardViewHolder

class ProfileAdapter(private val delegate: Delegate) : KSAdapter() {

    interface Delegate : ProfileCardViewHolder.Delegate, EmptyProfileViewHolder.Delegate

    init {

        insertSection(SECTION_EMPTY_VIEW, emptyList<Any>())
        insertSection(SECTION_PROJECTS_VIEW, emptyList<Any>())
    }

    fun takeProjects(projects: List<Project>) {
        if (projects.isEmpty()) {
            setSection(SECTION_EMPTY_VIEW, listOf(Empty.get()))
        } else {
            setSection(SECTION_PROJECTS_VIEW, projects)
        }

        notifyDataSetChanged()
    }

    @LayoutRes
    override fun layout(sectionRow: KSAdapter.SectionRow): Int {
        return if (sectionRow.section() == SECTION_EMPTY_VIEW) {
            R.layout.profile_empty_state_view
        } else {
            R.layout.profile_card_view
        }
    }

    override fun viewHolder(@LayoutRes layout: Int, view: View): KSViewHolder {
        return when (layout) {
            R.layout.profile_empty_state_view -> EmptyProfileViewHolder(view, this.delegate)
            R.layout.profile_card_view -> ProfileCardViewHolder(view, this.delegate)
            else -> EmptyViewHolder(view)
        }
    }

    companion object {
        private const val SECTION_EMPTY_VIEW = 0
        private const val SECTION_PROJECTS_VIEW = 1
    }
}
