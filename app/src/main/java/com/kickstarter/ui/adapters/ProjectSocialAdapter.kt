package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ProjectContextViewBinding
import com.kickstarter.databinding.ProjectSocialViewBinding
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.ProjectContextViewHolder
import com.kickstarter.ui.viewholders.ProjectSocialViewHolder

class ProjectSocialAdapter : KSListAdapter() {

    override fun layout(sectionRow: SectionRow?): Int {
        return if (sectionRow?.section() == 0) {
            R.layout.project_context_view
        } else {
            R.layout.project_social_view
        }
    }

    init {
        insertSection(SECTION_EMPTY_VIEW, emptyList<Any>())
        insertSection(SECTION_FRIENDS_VIEW, emptyList<Any>())
    }

    companion object {
        private const val SECTION_EMPTY_VIEW = 0
        private const val SECTION_FRIENDS_VIEW = 1
    }
    fun takeProject(project: Project) {
        setSection(SECTION_EMPTY_VIEW, listOf(project))
        if (project.friends().isNotEmpty()) {
            setSection(SECTION_FRIENDS_VIEW, project.friends())
        }

        submitList(items())
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return if (layout == R.layout.project_context_view) {
            ProjectContextViewHolder(
                ProjectContextViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )
        } else {
            ProjectSocialViewHolder(
                ProjectSocialViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )
        }
    }
}
