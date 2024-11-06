import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test

class CollectionPlanTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPledgeInFullOptionIsSelectedInitially() {
        composeTestRule.setContent {
            CollectionPlan(isEligible = true, initialSelectedOption = "Pledge in full")
        }

        composeTestRule
            .onNodeWithText("Pledge in full")
            .assertIsSelected()
    }

    @Test
    fun testPledgeOverTimeOptionIsDisabledWhenNotEligible() {
        composeTestRule.setContent {
            CollectionPlan(isEligible = false, initialSelectedOption = "Pledge in full")
        }

        composeTestRule
            .onNodeWithText("Pledge Over Time")
            .assertHasClickAction()
            .performClick()

        composeTestRule
            .onNodeWithText("Pledge in full")
            .assertIsSelected()
    }

    @Test
    fun testPledgeOverTimeOptionIsSelectableWhenEligible() {
        composeTestRule.setContent {
            CollectionPlan(isEligible = true, initialSelectedOption = "Pledge in full")
        }

        composeTestRule
            .onNodeWithText("Pledge Over Time")
            .performClick()

        composeTestRule
            .onNodeWithText("Pledge Over Time")
            .assertIsSelected()
    }

    @Test
    fun testChargeScheduleDisplaysWhenPledgeOverTimeSelectedAndExpanded() {
        composeTestRule.setContent {
            CollectionPlan(isEligible = true, initialSelectedOption = "Pledge Over Time")
        }

        composeTestRule
            .onNodeWithText("Pledge Over Time")
            .performClick()

        composeTestRule
            .onNodeWithText("Charge 1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Aug 11, 2024")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("$250")
            .assertIsDisplayed()
    }
}
