package com.kickstarter.libs

import androidx.annotation.StringRes
import com.kickstarter.R
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
    RISKS, // - Types.RISKS.ordinal == 3 & position ==3
    USE_OF_AI, // - Types.USE_OF_AI.ordinal == 4 & position == 4
    ENVIRONMENTAL_COMMITMENT, // - Types.ENVIRONMENTAL_COMMITMENT.ordinal == 5 & position == 5
}

/**
 * Related to Project Environmental Commitments Sections
 * - enum class holding the position each tab should
 * be placed on the TabLayout
 */
enum class EnvironmentalCommitmentCategories(@StringRes val title: Int) {
    LONG_LASTING_DESIGN(R.string.Long_lasting_design),
    SUSTAINABLE_MATERIALS(R.string.Sustainable_materials),
    ENVIRONMENTALLY_FRIENDLY_FACTORIES(R.string.Environmentally_friendly_factories),
    SUSTAINABLE_DISTRIBUTION(R.string.Sustainable_distribution),
    REUSABILITY_AND_RECYCLABILITY(R.string.Reusability_and_recyclability),
    SOMETHING_ELSE(R.string.Something_else)
}
