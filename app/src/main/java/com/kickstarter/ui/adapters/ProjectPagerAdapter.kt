package com.kickstarter.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kickstarter.libs.Configure
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.FrequentlyAskedQuestionFragment
import com.kickstarter.ui.fragments.projectpage.ProjectEnvironmentalCommitmentsFragment
import com.kickstarter.ui.fragments.projectpage.ProjectOverviewFragment

class ProjectPagerAdapter(
    private val fragmentManager: FragmentManager,
    private val pagerAdapterMap: MutableMap<ProjectPagerTabs, Boolean>,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = pagerAdapterMap.count { it.value }

    override fun createFragment(position: Int): Fragment {
        return when {
            position == ProjectPagerTabs.OVERVIEW.ordinal && pagerAdapterMap[ProjectPagerTabs.OVERVIEW] == true ->
                ProjectOverviewFragment.newInstance(position)
            position == ProjectPagerTabs.FAQS.ordinal && pagerAdapterMap[ProjectPagerTabs.FAQS] == true ->
                FrequentlyAskedQuestionFragment.newInstance(position)
            /*position == ProjectPagerTabs.CAMPAIGN.ordinal && pagerAdapterMap[ProjectPagerTabs.CAMPAIGN] == true ->
                ProjectCampaignFragment.newInstance(position) */ // TODO bring back ProjectCampaignFragment.newInstance(position), on second place once the HTML parser is in place
            position == ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT.ordinal && pagerAdapterMap[ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT] == true ->
                ProjectEnvironmentalCommitmentsFragment.newInstance(position)
            else -> ProjectOverviewFragment.newInstance(position)
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
            (fragment as Configure).configureWith(projectData)
        }
    }
}
