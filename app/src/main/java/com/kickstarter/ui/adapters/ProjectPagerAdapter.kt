package com.kickstarter.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kickstarter.libs.Configure
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.ProjectFaqFragment
import com.kickstarter.ui.fragments.ProjectOverviewFragment

class ProjectPagerAdapter(
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    // TODO: improve when with an enum type
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> return ProjectOverviewFragment.newInstance(position)
            1 -> return ProjectFaqFragment.newInstance(position)
            else -> ProjectOverviewFragment.newInstance(position)
        }
    }

    fun updatedWithProjectData(projectData: ProjectData) {
        fragmentManager.fragments.forEach { fragment ->
            // - fragmentManager.fragments will iterate over all fragments added, but only the ones on the
            // - projectTabLayout implement the Configure interface
            if (fragment is Configure) fragment.configureWith(projectData)
        }
    }
}
