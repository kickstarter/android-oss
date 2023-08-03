package com.kickstarter.ui.activities.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.models.AiDisclosure
import com.kickstarter.ui.activities.compose.projectpage.AiDisclosureScreen
import com.kickstarter.ui.activities.compose.projectpage.TestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.viewmodels.projectpage.ProjectAIViewModel
import org.junit.Test

class AiDisclosureScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val title = composeTestRule.onNodeWithTag(TestTag.TILE_TAG.name)

    // Funding section
    private val fundingSectionTitle = composeTestRule.onNodeWithTag(TestTag.FOUNDING_SECTION_TITLE.name)
    private val fundingSectionAttr = composeTestRule.onNodeWithTag(TestTag.FOUNDING_SECTION_ATTRIBUTION.name)
    private val fundingSectionCons = composeTestRule.onNodeWithTag(TestTag.FOUNDING_SECTION_CONSENT.name)
    private val fundingSectionOpt = composeTestRule.onNodeWithTag(TestTag.FOUNDING_SECTION_OPTION.name)
    private val fundingSectionDiv = composeTestRule.onNodeWithTag(TestTag.FOUNDING_SECTION_DIVIDER.name)

    // Generating section
    private val generatingSectionTitle = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_TITLE.name)
    private val generatingSectionConsent = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_CONSENT.name)
    private val generatingSectionDetails = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_DETAILS.name)
    private val generatingSectionDiv = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_DIVIDER.name)

    // Other Section
    private val otherSectionTitle = composeTestRule.onNodeWithTag(TestTag.OTHER_SECTION_TITLE.name)
    private val otherSectionDet = composeTestRule.onNodeWithTag(TestTag.OTHER_SECTION_DETAILS.name)
    private val otherSectionDiv = composeTestRule.onNodeWithTag(TestTag.OTHER_SECTION_DIVIDER.name)

    private val link = composeTestRule.onNodeWithTag(TestTag.LINK.name)

    @Test
    fun emptyState() {
        composeTestRule.setContent {
            KSTheme {
                AiDisclosureScreen(
                    state = ProjectAIViewModel.UiState(),
                    clickCallback = {}
                )
            }
        }

        // Title is always visible
        val titleText = context.resources.getString(R.string.Use_of_ai_fpo)
        title.assertTextEquals(titleText)

        // - Founding section does not exist, as the state.aiDisclosure is empty
        fundingSectionTitle.assertDoesNotExist()
        fundingSectionAttr.assertDoesNotExist()
        fundingSectionCons.assertDoesNotExist()
        fundingSectionOpt.assertDoesNotExist()
        fundingSectionDiv.assertDoesNotExist()

        // - Generating section does not exist, as the state.aiDisclosure is empty
        generatingSectionTitle.assertDoesNotExist()
        generatingSectionConsent.assertDoesNotExist()
        generatingSectionDetails.assertDoesNotExist()
        generatingSectionDiv.assertDoesNotExist()

        // - Other section does not exist, as the state.aiDisclosure is empty
        otherSectionTitle.assertDoesNotExist()
        otherSectionDet.assertDoesNotExist()
        otherSectionDiv.assertDoesNotExist()

        // Link is always visible
        val linkText = context.resources.getString(R.string.Learn_about_AI_fpo)
        link.assertTextEquals(linkText)
    }

    @Test
    fun completeState() {
        val disclosure = AiDisclosure
            .builder()
            .fundingForAiAttribution(true)
            .fundingForAiConsent(true)
            .fundingForAiOption(true)
            .generatedByAiConsent("consent")
            .generatedByAiDetails("details")
            .otherAiDetails("other details")
            .build()

        val url = "Some other URL here"
        val completeState = ProjectAIViewModel.UiState(
            openExternalUrl = url,
            aiDisclosure = disclosure
        )

        composeTestRule.setContent {
            KSTheme {
                AiDisclosureScreen(
                    state = completeState
                )
            }
        }

        val linkText = context.resources.getString(R.string.Learn_about_AI_fpo)
        link.assertTextEquals(linkText)

        // Title is always visible
        val titleText = context.resources.getString(R.string.Use_of_ai_fpo)
        title.assertTextEquals(titleText)

        // Funding sections
        val fundingTitleText = context.resources.getString(R.string.My_project_seeks_founding_fpo)
        fundingSectionTitle.assertTextEquals(fundingTitleText)
        val fundingConsText = context.resources.getString(R.string.For_the_database_orsource_fpo)
        fundingSectionCons.assertTextEquals(fundingConsText)
        val fundingOptText = context.resources.getString(R.string.There_is_or_will_be_fpo)
        fundingSectionOpt.assertTextEquals(fundingOptText)
        val fundingAttrText = context.resources.getString(R.string.The_owners_of_fpo)
        fundingSectionAttr.assertTextEquals(fundingAttrText)
        fundingSectionDiv.assertIsDisplayed()

        // Generating section
        val genTitle = context.resources.getString(R.string.I_plan_to_use_AI_fpo)
        generatingSectionTitle.assertTextEquals(genTitle)
        generatingSectionConsent.assertTextEquals(disclosure.generatedByAiConsent)
        generatingSectionDetails.assertTextEquals(disclosure.generatedByAiDetails)
        generatingSectionDiv.assertIsDisplayed()

        // Others section
        val othersTitle = context.resources.getString(R.string.I_am_incorporating_AI_fpo)
        otherSectionTitle.assertTextEquals(othersTitle)
        otherSectionDet.assertTextEquals(disclosure.otherAiDetails)
    }
}
