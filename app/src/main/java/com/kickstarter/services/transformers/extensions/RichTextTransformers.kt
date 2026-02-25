package com.kickstarter.services.transformers.extensions

import com.kickstarter.features.projectstory.data.RichTextComponent
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.features.projectstory.data.StoriedProject
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

fun ProjectStoryFragment?.toStoriedProject(): StoriedProject {
    Timber.d("transform()")
    val id = decodeRelayId(this?.id) ?: -1
    Timber.d("-- id: $id")
    Timber.d("-- _?.pid: ${this?.pid}")
    val name = this?.name
    val slug = this?.slug
    val displayPrelaunch = (this?.isLaunched ?: false).negate()
    /* from GraphQLTransformers.getPhoto() */
    val photo = this?.imageUrl.let { photoUrl ->
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
            this?.prelaunchStoryRichText?.storyRichTextComponentFragment?.toRichTextComponent()
        else
            this?.storyRichText?.storyRichTextComponentFragment?.toRichTextComponent()

    return StoriedProject(project, story)
}

/*
 * Note the use of `mapNotNull` to filter out `null` items everywhere. This may change.
 */
fun StoryRichTextComponentFragment.toRichTextComponent(): RichTextComponent {
    val items: List<RichTextItem> = this.items.mapNotNull { fragmentItem ->
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
                                fragmentItemChild.richTextChildParagraphFragment.toChildParagraph()
                            fragmentItemChild.richTextPhotoFragment != null ->
                                fragmentItemChild.richTextPhotoFragment.toPhoto()
                            else -> null
                        }
                    }
                )
            }
            fragmentItem.onRichTextHeader != null -> {
                val styles = fragmentItem.onRichTextHeader.styles.orEmpty()
                val level = when {
                    "HEADING_1" in styles -> RichTextItem.Text.Header.Level.H1
                    "HEADING_2" in styles -> RichTextItem.Text.Header.Level.H2
                    "HEADING_3" in styles -> RichTextItem.Text.Header.Level.H3
                    else -> RichTextItem.Text.Header.Level.H4 /* Least disruptive style */
                }
                RichTextItem.Text.Header(
                    level,
                    fragmentItem.onRichTextHeader.__typename,
                    fragmentItem.onRichTextHeader.text,
                    fragmentItem.onRichTextHeader.link,
                    fragmentItem.onRichTextHeader.styles,
                    children = fragmentItem.onRichTextHeader.children?.mapNotNull { fragmentItemChild ->
                        fragmentItemChild.richTextChildParagraphFragment?.toChildParagraph()
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
                        fragmentItemChild.richTextChildParagraphFragment?.toChildParagraph()
                    },
                )
            }
            fragmentItem.onRichTextPhoto != null -> {
                fragmentItem.onRichTextPhoto.richTextPhotoFragment.toPhoto()
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

private fun RichTextChildParagraphFragment.toChildParagraph(): RichTextItem.Text.ChildParagraph {
    return RichTextItem.Text.ChildParagraph(
        __typename,
        text,
        link,
        styles
    )
}

private fun RichTextPhotoFragment.toPhoto(): RichTextItem.Photo {
    return RichTextItem.Photo(
        __typename,
        url,
        altText,
        caption,
        asset?.let {
            RichTextItem.Photo.Asset(
                it.url,
                it.altText
            )
        }
    )
}
