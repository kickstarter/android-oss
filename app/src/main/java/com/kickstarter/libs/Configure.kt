package com.kickstarter.libs

import com.kickstarter.ui.data.ProjectData

/**
 * Related to Project TabLayout and ViewPager
 * - All fragments loading on the Project ViewPager should
 * implement this interface, as is the bridge used for updating
 * the fragment with the new Data
 */
interface Configure {
    fun configureWith(projectData: ProjectData)
}

/**
 * Related to Project TabLayout and ViewPager
 * - enum class holding the position each tab should
 * be placed on the TabLayout
 */
enum class ProjectPagerTabs {
    OVERVIEW, // - Types.OVERVIEW.ordinal == 0 & position == 0
    CAMPAIGN, // - Types.CAMPAIGN.ordinal == 1 & position == 1
    FAQS, // - Types.FAQS.ordinal == 2 & position == 2
    ENVIRONMENTAL_COMMITMENT, // - Types.ENVIRONMENTAL_COMMITMENT.ordinal == 3 & position == 3
}
