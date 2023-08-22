package com.kickstarter.ui.activities.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
    private val generatingSectionConsentQuestion = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_CONSENT_QUESTION.name)
    private val generatingSectionConsentDiv = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_CONSENT_DIVIDER.name)
    private val generatingSectionConsent = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_CONSENT.name)
    private val generatingSectionDetailsQuestion = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_DETAILS_QUESTION.name)
    private val generatingSectionDetailsDiv = composeTestRule.onNodeWithTag(TestTag.GENERATION_SECTION_DETAILS_DIVIDER.name)
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
        val titleText = context.resources.getString(R.string.Use_of_ai)
        title.assertTextEquals(titleText)

        // - Founding section does not exist, as the state.aiDisclosure is empty
        fundingSectionTitle.assertDoesNotExist()
        fundingSectionAttr.assertDoesNotExist()
        fundingSectionCons.assertDoesNotExist()
        fundingSectionOpt.assertDoesNotExist()
        fundingSectionDiv.assertDoesNotExist()

        // - Generating section does not exist, as the state.aiDisclosure is empty
        generatingSectionTitle.assertDoesNotExist()
        generatingSectionConsentQuestion.assertDoesNotExist()
        generatingSectionConsentDiv.assertDoesNotExist()
        generatingSectionConsent.assertDoesNotExist()
        generatingSectionDetailsQuestion.assertDoesNotExist()
        generatingSectionDetailsDiv.assertDoesNotExist()
        generatingSectionDetails.assertDoesNotExist()
        generatingSectionDiv.assertDoesNotExist()

        // - Other section does not exist, as the state.aiDisclosure is empty
        otherSectionTitle.assertDoesNotExist()
        otherSectionDet.assertDoesNotExist()
        otherSectionDiv.assertDoesNotExist()

        // Link is always visible
        val linkText = context.resources.getString(R.string.Learn_about_AI_policy_on_Kickstarter)
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

        val linkText = context.resources.getString(R.string.Learn_about_AI_policy_on_Kickstarter)
        link.assertTextEquals(linkText)

        // Title is always visible
        val titleText = context.resources.getString(R.string.Use_of_ai)
        title.assertTextEquals(titleText)

        // Funding sections
        val fundingTitleText = context.resources.getString(R.string.My_project_seeks_funding_for_AI_technology)
        fundingSectionTitle.assertTextEquals(fundingTitleText)
        val fundingConsText = context.resources.getString(R.string.For_the_database_or_source_I_will_use)
        fundingSectionCons.assertTextEquals(fundingConsText)
        val fundingOptText = context.resources.getString(R.string.There_is_or_will_be_an_opt)
        fundingSectionOpt.assertTextEquals(fundingOptText)
        val fundingAttrText = context.resources.getString(R.string.The_owners_of_those_works)
        fundingSectionAttr.assertTextEquals(fundingAttrText)
        fundingSectionDiv.assertIsDisplayed()

        // Generating section
        val genTitle = context.resources.getString(R.string.I_plan_to_use_AI_generated_content)
        generatingSectionTitle.assertTextEquals(genTitle)
        val genConsentQuestion = context.resources.getString(R.string.Do_you_have_the_consent_of_the_owners_of_the_works_used_for_AI)
        generatingSectionConsentQuestion.assertTextEquals(genConsentQuestion)
        generatingSectionConsentDiv.assertExists()
        generatingSectionConsent.assertTextEquals(disclosure.generatedByAiConsent)
        val genDetailsQuestion = context.resources.getString(R.string.What_parts_of_your_project_will_use_AI_generated_content)
        generatingSectionDetailsQuestion.assertTextEquals(genDetailsQuestion)
        generatingSectionDetailsDiv.assertExists()
        generatingSectionDetails.assertTextEquals(disclosure.generatedByAiDetails)
        generatingSectionDiv.assertExists()

        // Others section
        val othersTitle = context.resources.getString(R.string.I_am_incorporating_AI_in_my_project)
        otherSectionTitle.assertTextEquals(othersTitle)
        otherSectionDet.assertTextEquals(disclosure.otherAiDetails)
    }
}
