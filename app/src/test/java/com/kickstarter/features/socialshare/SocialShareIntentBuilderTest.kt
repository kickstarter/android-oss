package com.kickstarter.features.socialshare

import android.content.Intent
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import org.junit.Test

class SocialShareIntentBuilderTest : KSRobolectricTestCase() {

    private val shareData = SocialShareData(
        projectName = "Ringo Move - The Ultimate Workout Bottle",
        projectUrl = "https://www.kickstarter.com/projects/ringo/ringo-move",
        imageUrl = "https://example.com/image.jpg",
        creatorName = "Ringo"
    )

    private val imageUri: Uri =
        Uri.parse("content://com.kickstarter.fileprovider/share_images/kickstarter_share.png")

    @Test
    fun `COPY_LINK returns null, handled by clipboard, not an intent`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.COPY_LINK, shareData, imageUri)
        assertNull(intent)
    }

    @Test
    fun `INSTAGRAM_FEED returns null when imageUri is null`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.INSTAGRAM_FEED, shareData, null)
        assertNull(intent)
    }

    @Test
    fun `INSTAGRAM_FEED intent targets Instagram with image and text`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.INSTAGRAM_FEED, shareData, imageUri)

        assertEquals(Intent.ACTION_SEND, intent?.action)
        assertEquals("image/png", intent?.type)
        assertEquals("com.instagram.android", intent?.`package`)
        assertEquals(imageUri, intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertNotNull(intent?.getStringExtra(Intent.EXTRA_TEXT))
        assertNotNull(intent?.clipData)
        assertTrue(intent?.flags!! and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `INSTAGRAM_STORIES returns null when imageUri is null`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.INSTAGRAM_STORIES, shareData, null)
        assertNull(intent)
    }

    @Test
    fun `INSTAGRAM_STORIES intent uses ADD_TO_STORY action with source_application extra`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.INSTAGRAM_STORIES, shareData, imageUri)

        assertEquals("com.instagram.share.ADD_TO_STORY", intent?.action)
        assertEquals(context().packageName, intent?.getStringExtra("source_application"))
        assertNotNull(intent?.clipData)
        assertTrue(intent?.flags!! and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `X intent with image sends image and text to X app`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.X, shareData, imageUri)

        assertEquals(Intent.ACTION_SEND, intent?.action)
        assertEquals("image/png", intent?.type)
        assertEquals("com.twitter.android", intent?.`package`)
        assertEquals(imageUri, intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT)!!
        assertTrue(text.contains(shareData.projectUrl))
        assertTrue(text.contains(shareData.projectName))
        assertNotNull(intent.clipData)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `X intent without image falls back to text-only`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.X, shareData, null)

        assertEquals(Intent.ACTION_SEND, intent?.action)
        assertEquals("text/plain", intent?.type)
        assertEquals("com.twitter.android", intent?.`package`)
        assertTrue(intent?.getStringExtra(Intent.EXTRA_TEXT)!!.contains(shareData.projectUrl))
        assertNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
    }

    @Test
    fun `FACEBOOK_FEED returns null when imageUri is null`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.FACEBOOK_FEED, shareData, null)
        assertNull(intent)
    }

    @Test
    fun `FACEBOOK_FEED intent targets Facebook with image and text`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.FACEBOOK_FEED, shareData, imageUri)

        assertEquals(Intent.ACTION_SEND, intent?.action)
        assertEquals("image/png", intent?.type)
        assertEquals("com.facebook.katana", intent?.`package`)
        assertEquals(imageUri, intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertNotNull(intent?.getStringExtra(Intent.EXTRA_TEXT))
        assertNotNull(intent?.clipData)
        assertTrue(intent?.flags!! and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `FACEBOOK_STORIES returns null when imageUri is null`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.FACEBOOK_STORIES, shareData, null)
        assertNull(intent)
    }

    @Test
    fun `FACEBOOK_STORIES intent uses ADD_TO_STORY action targeting Facebook`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.FACEBOOK_STORIES, shareData, imageUri)

        assertEquals("com.facebook.stories.ADD_TO_STORY", intent?.action)
        assertEquals("com.facebook.katana", intent?.`package`)
        assertEquals(imageUri, intent?.getParcelableExtra<Uri>("backgroundAssetUri"))
        assertNotNull(intent?.clipData)
        assertTrue(intent?.flags!! and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `WHATSAPP intent with image sends image and caption to WhatsApp`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.WHATSAPP, shareData, imageUri)

        assertEquals(Intent.ACTION_SEND, intent?.action)
        assertEquals("image/png", intent?.type)
        assertEquals("com.whatsapp", intent?.`package`)
        assertEquals(imageUri, intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertTrue(intent?.getStringExtra(Intent.EXTRA_TEXT)!!.contains(shareData.projectUrl))
        assertNotNull(intent.clipData)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `WHATSAPP intent without image falls back to text-only`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.WHATSAPP, shareData, null)!!

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("com.whatsapp", intent.`package`)
        assertTrue(intent.getStringExtra(Intent.EXTRA_TEXT)!!.contains(shareData.projectUrl))
        assertNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
    }

    @Test
    fun `MESSAGES intent uses ACTION_SENDTO with smsto scheme and pre-filled body`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.MESSAGES, shareData, imageUri)!!

        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("smsto:", intent.data?.toString())
        val body = intent.getStringExtra("sms_body")!!
        assertTrue(body.contains(shareData.projectUrl))
        assertTrue(body.contains(shareData.projectName))
    }

    @Test
    fun `EMAIL intent with image sends image as attachment with subject and body`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.EMAIL, shareData, imageUri)!!

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("message/rfc822", intent.type)
        assertTrue(intent.getStringExtra(Intent.EXTRA_SUBJECT)!!.contains(shareData.projectName))
        val body = intent.getStringExtra(Intent.EXTRA_TEXT)!!
        assertTrue(body.contains(shareData.projectUrl))
        assertTrue(body.contains(shareData.creatorName))
        @Suppress("DEPRECATION")
        assertEquals(imageUri, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertNotNull(intent.clipData)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `EMAIL intent without image falls back to ACTION_SENDTO with mailto scheme`() {
        val intent = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.EMAIL, shareData, null)!!

        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto:", intent.data?.toString())
        assertTrue(intent.getStringExtra(Intent.EXTRA_SUBJECT)!!.contains(shareData.projectName))
        assertTrue(intent.getStringExtra(Intent.EXTRA_TEXT)!!.contains(shareData.projectUrl))
        @Suppress("DEPRECATION")
        assertNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
    }

    @Test
    fun `MORE intent with image wraps ACTION_SEND in a chooser with image, text and title`() {
        val chooser = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.MORE, shareData, imageUri)!!

        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val wrapped = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, wrapped.action)
        assertEquals("image/png", wrapped.type)
        @Suppress("DEPRECATION")
        assertEquals(imageUri, wrapped.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertTrue(wrapped.getStringExtra(Intent.EXTRA_TEXT)!!.contains(shareData.projectUrl))
        assertEquals(shareData.projectName, wrapped.getStringExtra(Intent.EXTRA_TITLE))
        assertNotNull(wrapped.clipData)
        assertTrue(wrapped.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `MORE intent without image wraps text-only ACTION_SEND in a chooser`() {
        val chooser = SocialShareIntentBuilder.buildIntent(context(), SocialSharePlatform.MORE, shareData, null)!!

        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val wrapped = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, wrapped.action)
        assertEquals("text/plain", wrapped.type)
        assertTrue(wrapped.getStringExtra(Intent.EXTRA_TEXT)!!.contains(shareData.projectUrl))
        assertEquals(shareData.projectName, wrapped.getStringExtra(Intent.EXTRA_TITLE))
        @Suppress("DEPRECATION")
        assertNull(wrapped.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
    }

    @Test
    fun `All image-bearing intents carry FLAG_GRANT_READ_URI_PERMISSION and ClipData`() {
        val imagePlatforms = listOf(
            SocialSharePlatform.INSTAGRAM_FEED,
            SocialSharePlatform.INSTAGRAM_STORIES,
            SocialSharePlatform.X,
            SocialSharePlatform.FACEBOOK_FEED,
            SocialSharePlatform.FACEBOOK_STORIES,
            SocialSharePlatform.WHATSAPP,
            SocialSharePlatform.EMAIL,
            SocialSharePlatform.MORE
        )
        imagePlatforms.forEach { platform ->
            val intent = SocialShareIntentBuilder.buildIntent(context(), platform, shareData, imageUri)!!
            // - MORE wraps the real intent inside a chooser — unwrap it
            val actual = if (intent.action == Intent.ACTION_CHOOSER) {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            } else intent

            assertTrue(
                "$platform must have FLAG_GRANT_READ_URI_PERMISSION",
                actual.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
            )
            assertNotNull("$platform must have ClipData", actual.clipData)
        }
    }

    @Test
    fun `All text-capable platforms include the project URL`() {
        val cases = listOf(
            SocialSharePlatform.X to imageUri,
            SocialSharePlatform.WHATSAPP to imageUri,
            SocialSharePlatform.MESSAGES to null,
            SocialSharePlatform.EMAIL to imageUri
        )
        cases.forEach { (platform, uri) ->
            val intent = SocialShareIntentBuilder.buildIntent(context(), platform, shareData, uri)!!
            // Messages uses sms_body; everything else uses EXTRA_TEXT
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.getStringExtra("sms_body")
            assertTrue("$platform should contain project URL in text", text!!.contains(shareData.projectUrl))
        }
    }
}
