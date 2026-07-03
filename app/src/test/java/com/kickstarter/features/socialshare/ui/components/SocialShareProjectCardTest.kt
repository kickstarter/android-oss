package com.kickstarter.features.socialshare.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SocialShareProjectCardTest : KSRobolectricTestCase() {

    private val defaultShareData = SocialShareData(
        projectName = "Ringo Move - The Ultimate Workout Bottle",
        projectUrl = "https://www.kickstarter.com/projects/ringo/ringo-move",
        imageUrl = "https://example.com/image.jpg",
        creatorName = "Ringo"
    )

    @Test
    fun `SocialShareProjectCard displays project name`() {
        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = defaultShareData)
            }
        }

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.PROJECT_NAME.name)
            .assertIsDisplayed()
            .assertTextEquals(defaultShareData.projectName)
    }

    @Test
    fun `SocialShareProjectCard displays creator name`() {
        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = defaultShareData)
            }
        }

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.CREATOR_NAME.name)
            .assertIsDisplayed()
            .assertTextEquals(defaultShareData.creatorName)
    }

    @Test
    fun `SocialShareProjectCard displays KS logo`() {
        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = defaultShareData)
            }
        }

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.KS_LOGO.name)
            .assertIsDisplayed()

        // Verify the human-readable content description (not the internal code name)
        composeTestRule
            .onNodeWithContentDescription("Kickstarter")
            .assertIsDisplayed()
    }

    @Test
    fun `SocialShareProjectCard reflects updated project name`() {
        val updatedData = defaultShareData.copy(
            projectName = "La Santa & The Monk: A Santa Muerte Devotional Manual"
        )

        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = updatedData)
            }
        }

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.PROJECT_NAME.name)
            .assertTextEquals(updatedData.projectName)
    }

    @Test
    fun `SocialShareProjectCard reflects updated creator name`() {
        val updatedData = defaultShareData.copy(creatorName = "Ed's Manifesto")

        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = updatedData)
            }
        }

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.CREATOR_NAME.name)
            .assertTextEquals(updatedData.creatorName)
    }

    @Test
    fun `SocialShareProjectCard renders branded content via the preloaded hero bitmap path`() {
        // A non-null heroBitmap takes the synchronous Image(bitmap) branch used at capture time
        // (instead of the async KSAsyncImage). The branded content must still be present.
        val heroBitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)

        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = defaultShareData, heroBitmap = heroBitmap)
            }
        }

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.PROJECT_NAME.name)
            .assertIsDisplayed()
            .assertTextEquals(defaultShareData.projectName)

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.CREATOR_NAME.name)
            .assertIsDisplayed()
            .assertTextEquals(defaultShareData.creatorName)

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.KS_LOGO.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Kickstarter")
            .assertIsDisplayed()
    }

    @Test
    fun `SocialShareProjectCard displays correctly with empty image url`() {
        val noImageData = defaultShareData.copy(imageUrl = "")

        composeTestRule.setContent {
            KSTheme {
                SocialShareProjectCard(shareData = noImageData)
            }
        }

        // Text content should still be displayed even without an image
        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.PROJECT_NAME.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.CREATOR_NAME.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SocialShareProjectCardTestTag.KS_LOGO.name)
            .assertIsDisplayed()
    }
}
