package com.kickstarter.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kickstarter.libs.Configure
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.FrequentlyAskedQuestionFragment
import com.kickstarter.ui.fragments.projectpage.ProjectAIFragment
import com.kickstarter.ui.fragments.projectpage.ProjectCampaignFragment
import com.kickstarter.ui.fragments.projectpage.ProjectEnvironmentalCommitmentsFragment
import com.kickstarter.ui.fragments.projectpage.ProjectOverviewFragment
import com.kickstarter.ui.fragments.projectpage.ProjectRiskFragment

class ProjectPagerAdapter(
    private val fragmentManager: FragmentManager,
    private val pagerAdapterList: List<ProjectPagerTabs>,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = pagerAdapterList.size

    override fun createFragment(position: Int): Fragment {
        return when (pagerAdapterList[position]) {
            ProjectPagerTabs.OVERVIEW ->
                ProjectOverviewFragment.newInstance(position)
            ProjectPagerTabs.FAQS ->
                FrequentlyAskedQuestionFragment.newInstance(position)
            ProjectPagerTabs.CAMPAIGN ->
                ProjectCampaignFragment.newInstance(position)
            ProjectPagerTabs.RISKS ->
                ProjectRiskFragment.newInstance(position)
            ProjectPagerTabs.USE_OF_AI ->
                ProjectAIFragment.newInstance(position)
            ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT ->
                ProjectEnvironmentalCommitmentsFragment.newInstance(position)
        }
    }

    /**
     * Will update all the fragments in tha ViewPager with the given ProjectData
     *
     * @param projectData
     */
    fun updatedWithProjectData(projectData: ProjectData) {
        // - fragmentManager.fragments will iterate over all fragments, we are just interested
        // here in those who implement the Configure interface
        fragmentManager.fragments.filter { fragment -> fragment is Configure }.forEach { fragment ->
            if (fragment.isAdded) {
                (fragment as Configure).configureWith(projectData)
            }
        }
    }
}
