package com.kickstarter.services.transformers.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectstory.data.RichTextComponent
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.fragment.RichTextChildParagraphFragment
import com.kickstarter.fragment.StoryRichTextComponentFragment
import org.junit.Test

class GraphQLTransformersTest : KSRobolectricTestCase() {

    @Test
    fun `test rich text paragraph with extra null children`() {
        val storyRichTextComponentFragment = fixtureStoryRichTextComponentFragment()
        val richTextComponent = storyRichTextComponentFragment.toRichTextComponent()
        assertEquals(fixtureRichTextComponent(), richTextComponent)
    }
}

private fun fixtureStoryRichTextComponentFragment() =
    StoryRichTextComponentFragment(
        items = listOf(
            StoryRichTextComponentFragment.Item(
                __typename = "RichText",
                onRichText = fixtureOnRichText(),
                onRichTextHeader = null,
                onRichTextListOpen = null,
                onRichTextListItem = null,
                onRichTextListClose = null,
                onRichTextOembed = null,
                onRichTextPhoto = null,
                onRichTextAudio = null,
                onRichTextVideo = null
            )
        )
    )

private fun fixtureOnRichText() =
    StoryRichTextComponentFragment.OnRichText(
        __typename = "RichText",
        text = null,
        link = null,
        styles = emptyList(),
        children = listOf(
            StoryRichTextComponentFragment.Child(
                __typename = "RichText",
                richTextChildParagraphFragment = RichTextChildParagraphFragment(
                    __typename = "RichText",
                    text = "We are fully committed to making",
                    link = null,
                    styles = emptyList()
                ),
                richTextPhotoFragment = null
            ),
            StoryRichTextComponentFragment.Child(
                __typename = "RichText",
                richTextChildParagraphFragment = RichTextChildParagraphFragment(
                    __typename = "RichText",
                    text = "all software and source code",
                    link = null,
                    styles = listOf("STRONG")
                ),
                richTextPhotoFragment = null
            ),
            StoryRichTextComponentFragment.Child(
                __typename = "RichText",
                richTextChildParagraphFragment = RichTextChildParagraphFragment(
                    __typename = "RichText",
                    text = "available under GPL.",
                    link = null,
                    styles = emptyList()
                ),
                richTextPhotoFragment = null
            ),
            StoryRichTextComponentFragment.Child(
                __typename = "",
                richTextChildParagraphFragment = null,
                richTextPhotoFragment = null
            ),
        )
    )

private fun fixtureRichTextComponent() =
    RichTextComponent(
        items = listOf(
            fixtureParagraph()
        )
    )

private fun fixtureParagraph() =
    RichTextItem.Text.Paragraph(
        __typename = "RichText",
        text = null,
        link = null,
        styles = emptyList(),
        children = listOf(
            RichTextItem.Text.ChildParagraph(
                __typename = "RichText",
                text = "We are fully committed to making",
                link = null,
                styles = emptyList()
            ),
            RichTextItem.Text.ChildParagraph(
                __typename = "RichText",
                text = "all software and source code",
                link = null,
                styles = listOf("STRONG")
            ),
            RichTextItem.Text.ChildParagraph(
                __typename = "RichText",
                text = "available under GPL.",
                link = null,
                styles = emptyList()
            ),
        )
    )
