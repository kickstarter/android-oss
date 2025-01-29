package com.kickstarter.ui.views.compose.projectpage

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class KSBottomSheetContentTest : KSRobolectricTestCase() {

    private val title = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.TITLE.name)
    private val body = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.BODY.name)
    private val link = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.LINK.name)
    private val close_button = composeTestRule.onNodeWithTag(KSBottomSheetContentTestTag.CLOSE_BUTTON.name)

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
        close_button.assertTextEquals(context().getString(R.string.general_alert_buttons_ok))
    }
}
