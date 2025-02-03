package com.kickstarter.ui.views.compose.projectpage

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class KSBottomSheetContentTest : KSRobolectricTestCase() {

    private val title = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.TITLE.name)
    private val body = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.BODY.name)
    private val link = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.LINK.name)
    private val closeButton = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.CLOSE_BUTTON.name)

    @Test
    fun verifyInitState() {
        val projectNotice = "More info on this project notice served from backend"
        composeTestRule.setContent {
            KSTheme {
                KSBottomSheetContent(
                    title = context().getString(R.string.project_project_notices_header),
                    body = projectNotice,
                    linkText = context().getString(R.string.project_project_notices_notice_sheet_cta),
                    onLinkClicked = {},
                    onClose = {}
                )
            }
        }
        title.assertTextEquals(context().getString(R.string.project_project_notices_header))
        body.assertTextEquals(projectNotice)
        link.assertTextEquals(context().getString(R.string.project_project_notices_notice_sheet_cta))
        closeButton.assertTextEquals(context().getString(R.string.general_alert_buttons_ok))
    }

    @Test
    fun verifyOnClickAction() {
        var linkClickedCount = 0
        var closeButtonClickedCount = 0
        val projectNotice = "More info on this project notice served from backend"
        composeTestRule.setContent {
            KSTheme {
                KSBottomSheetContent(
                    title = context().getString(R.string.project_project_notices_header),
                    body = projectNotice,
                    linkText = context().getString(R.string.project_project_notices_notice_sheet_cta),
                    onLinkClicked = { linkClickedCount++ },
                    onClose = { closeButtonClickedCount++ }
                )
            }
        }
        link.performClick()
        assertEquals(1, linkClickedCount)
        closeButton.performClick()
        assertEquals(1, closeButtonClickedCount)
    }
}
