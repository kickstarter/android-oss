package com.kickstarter.features.projectstory.data

import com.kickstarter.fragment.ProjectStoryFragment
import com.kickstarter.fragment.RichTextChildParagraphFragment
import com.kickstarter.fragment.RichTextPhotoFragment
import com.kickstarter.fragment.StoryRichTextComponentFragment
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.services.transformers.decodeRelayId
import timber.log.Timber

data class StoriedProject(
    val project: Project,
    val story: RichTextComponent?,
    @Deprecated("Unnecessary")
    val prelaunchStory: RichTextComponent? = null,
)

data class RichTextComponent(
    val items: List<RichTextItem>
)

@Suppress("PropertyName")
sealed interface RichTextItem {
    sealed interface Text : RichTextItem {
        data class Paragraph(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem>?,
        ) : RichTextItem.Text
        data class Header(
            val level: RichTextItem.Text.Header.Level,
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem.Text.ChildParagraph>?,
        ) : RichTextItem.Text {
            enum class Level { H1, H2, H3, H4 }
        }
        data class ListItem(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
            val children: List<RichTextItem.Text.ChildParagraph>?,
        ) : RichTextItem.Text
        data class ChildParagraph(
            val __typename: String,
            val text: String?,
            val link: String?,
            val styles: List<String>?,
        ) : RichTextItem.Text
    }
    data class Photo(
        val __typename: String,
        val url: String,
        val altText: String,
        val caption: String,
        val asset: RichTextItem.Photo.Asset?,
    ) : RichTextItem {
        data class Asset(
            val url: String,
            val altText: String,
        )
    }
    data class Oembed(
        val __typename: String,
        val type: String,
        val iframeUrl: String,
    ) : RichTextItem
    data class ListOpen(
        val __typename: String,
        val _present: Boolean,
    ) : RichTextItem
    data class ListClose(
        val __typename: String,
        val _present: Boolean,
    ) : RichTextItem
}

fun transform(projectStoryFragment: ProjectStoryFragment?): StoriedProject {
    Timber.d("transform()")
    val id = decodeRelayId(projectStoryFragment?.id) ?: -1
    Timber.d("-- id: $id")
    Timber.d("-- _?.pid: ${projectStoryFragment?.pid}")
    val name = projectStoryFragment?.name
    val slug = projectStoryFragment?.slug
    val displayPrelaunch = (projectStoryFragment?.isLaunched ?: false).negate()
    /* from GraphQLTransformers.getPhoto() */
    val photo = projectStoryFragment?.imageUrl.let { photoUrl ->
        Photo.builder()
            .ed(photoUrl)
            .full(photoUrl)
            .little(photoUrl)
            .med(photoUrl)
            .small(photoUrl)
            .thumb(photoUrl)
            .altText(null)
            .build()
    }

    val project = Project.builder()
        .displayPrelaunch(displayPrelaunch)
        .id(id)
        .name(name)
        .photo(photo) // - now we get the full size for field from GraphQL, but V1 provided several image sizes
        .slug(slug)
        .build()

    val story =
        if (project.displayPrelaunch().isTrue())
            projectStoryFragment?.prelaunchStoryRichText?.storyRichTextComponentFragment?.let {
                transform(it)
            }
        else
            projectStoryFragment?.storyRichText?.storyRichTextComponentFragment?.let {
                transform(it)
            }

    return StoriedProject(project, story)
}

/*
 * Note the use of `mapNotNull` to filter out `null` items everywhere. This may change.
 */

fun transform(storyRichTextComponentFragment: StoryRichTextComponentFragment): RichTextComponent {
    val items: List<RichTextItem> = storyRichTextComponentFragment.items.mapNotNull { fragmentItem ->
        when {
            fragmentItem.onRichText != null -> {
                RichTextItem.Text.Paragraph(
                    fragmentItem.onRichText.__typename,
                    fragmentItem.onRichText.text,
                    fragmentItem.onRichText.link,
                    fragmentItem.onRichText.styles,
                    children = fragmentItem.onRichText.children?.mapNotNull { fragmentItemChild ->
                        when {
                            fragmentItemChild.richTextChildParagraphFragment != null ->
                                transform(fragmentItemChild.richTextChildParagraphFragment)
                            fragmentItemChild.richTextPhotoFragment != null -> transform(fragmentItemChild.richTextPhotoFragment)
                            else -> null
                        }
                    }
                )
            }
            fragmentItem.onRichTextHeader1 != null -> {
                RichTextItem.Text.Header(
                    RichTextItem.Text.Header.Level.H1,
                    fragmentItem.onRichTextHeader1.__typename,
                    fragmentItem.onRichTextHeader1.text,
                    fragmentItem.onRichTextHeader1.link,
                    fragmentItem.onRichTextHeader1.styles,
                    children = fragmentItem.onRichTextHeader1.children?.mapNotNull { fragmentItemChild ->
                        fragmentItemChild.richTextChildParagraphFragment?.let(::transform)
                    },
                )
            }
            fragmentItem.onRichTextHeader2 != null -> {
                RichTextItem.Text.Header(
                    RichTextItem.Text.Header.Level.H2,
                    fragmentItem.onRichTextHeader2.__typename,
                    fragmentItem.onRichTextHeader2.text,
                    fragmentItem.onRichTextHeader2.link,
                    fragmentItem.onRichTextHeader2.styles,
                    children = fragmentItem.onRichTextHeader2.children?.mapNotNull { fragmentItemChild ->
                        fragmentItemChild.richTextChildParagraphFragment?.let(::transform)
                    },
                )
            }
            fragmentItem.onRichTextHeader3 != null -> {
                RichTextItem.Text.Header(
                    RichTextItem.Text.Header.Level.H3,
                    fragmentItem.onRichTextHeader3.__typename,
                    fragmentItem.onRichTextHeader3.text,
                    fragmentItem.onRichTextHeader3.link,
                    fragmentItem.onRichTextHeader3.styles,
                    children = fragmentItem.onRichTextHeader3.children?.mapNotNull { fragmentItemChild ->
                        fragmentItemChild.richTextChildParagraphFragment?.let(::transform)
                    },
                )
            }
            fragmentItem.onRichTextHeader4 != null -> {
                RichTextItem.Text.Header(
                    RichTextItem.Text.Header.Level.H4,
                    fragmentItem.onRichTextHeader4.__typename,
                    fragmentItem.onRichTextHeader4.text,
                    fragmentItem.onRichTextHeader4.link,
                    fragmentItem.onRichTextHeader4.styles,
                    children = fragmentItem.onRichTextHeader4.children?.mapNotNull { fragmentItemChild ->
                        fragmentItemChild.richTextChildParagraphFragment?.let(::transform)
                    },
                )
            }
            fragmentItem.onRichTextListItem != null -> {
                RichTextItem.Text.ListItem(
                    fragmentItem.onRichTextListItem.__typename,
                    fragmentItem.onRichTextListItem.text,
                    fragmentItem.onRichTextListItem.link,
                    fragmentItem.onRichTextListItem.styles,
                    children = fragmentItem.onRichTextListItem.children?.mapNotNull { fragmentItemChild ->
                        fragmentItemChild.richTextChildParagraphFragment?.let(::transform)
                    },
                )
            }
            fragmentItem.onRichTextPhoto != null -> {
                transform(fragmentItem.onRichTextPhoto.richTextPhotoFragment)
            }
            fragmentItem.onRichTextOembed != null -> {
                RichTextItem.Oembed(
                    fragmentItem.onRichTextOembed.__typename,
                    fragmentItem.onRichTextOembed.type,
                    fragmentItem.onRichTextOembed.iframeUrl
                )
            }
            else -> null
        }
    }

    return RichTextComponent(items)
}

fun transform(richTextChildParagraphFragment: RichTextChildParagraphFragment): RichTextItem.Text.ChildParagraph {
    return RichTextItem.Text.ChildParagraph(
        richTextChildParagraphFragment.__typename,
        richTextChildParagraphFragment.text,
        richTextChildParagraphFragment.link,
        richTextChildParagraphFragment.styles
    )
}

fun transform(richTextPhotoFragment: RichTextPhotoFragment): RichTextItem.Photo {
    return RichTextItem.Photo(
        richTextPhotoFragment.__typename,
        richTextPhotoFragment.url,
        richTextPhotoFragment.altText,
        richTextPhotoFragment.caption,
        richTextPhotoFragment.asset?.let {
            RichTextItem.Photo.Asset(
                it.url,
                it.altText
            )
        }
    )
}
