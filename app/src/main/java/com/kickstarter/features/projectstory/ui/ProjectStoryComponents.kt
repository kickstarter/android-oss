package com.kickstarter.features.projectstory.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Bullet
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.grey_03
import com.kickstarter.ui.compose.designsystem.kds_create_700
import timber.log.Timber

object StoryTheme {
    object Typography {
        val paragraph = TextStyle.Default.merge(fontSize = 16.sp)
        val heading1 = TextStyle.Default.merge(fontSize = 28.sp)
        val heading2 = TextStyle.Default.merge(fontSize = 26.sp)
        val heading3 = TextStyle.Default.merge(fontSize = 24.sp)
        val heading4 = TextStyle.Default.merge(fontSize = 22.sp)
    }

    object InlineStyles {
        val link = SpanStyle(color = kds_create_700, textDecoration = TextDecoration.Underline)
    }
}

@Composable
fun RichTextItemTextComponent(item: RichTextItem.Text) {
    /* `item` here won't be `ChildParagraph` but we will handle it anyways for right now */

    val link =
        when (item) {
            is RichTextItem.Text.Paragraph -> item.link
            is RichTextItem.Text.Header -> item.link
            is RichTextItem.Text.ListItem -> item.link
            is RichTextItem.Text.ChildParagraph -> null
        }

    /* Currently, `styles` is empty for all first-level items except `Header`.
     * At this point, the GQL transformer has already used `styles` to determine `Header.level` */
    val styles =
        when (item) {
            is RichTextItem.Text.Paragraph -> item.styles
            is RichTextItem.Text.Header -> item.styles
            is RichTextItem.Text.ListItem -> item.styles
            is RichTextItem.Text.ChildParagraph -> null
        }

    val children =
        when (item) {
            /* The other possible child for Paragraph is RichTextItem.Photo,
             * which we check for higher up, but we can still filter rather than cast for safety. */
            is RichTextItem.Text.Paragraph -> item.children?.filterIsInstance<RichTextItem.Text.ChildParagraph>()
            is RichTextItem.Text.Header -> item.children
            is RichTextItem.Text.ListItem -> item.children
            is RichTextItem.Text.ChildParagraph -> null
        }

    val itemText =
        when (item) {
            is RichTextItem.Text.Paragraph -> item.text
            is RichTextItem.Text.Header -> item.text
            is RichTextItem.Text.ListItem -> item.text
            is RichTextItem.Text.ChildParagraph -> null
        }

    val textStyle =
        when (item) {
            is RichTextItem.Text.Paragraph -> StoryTheme.Typography.paragraph
            is RichTextItem.Text.Header ->
                when (item.level) {
                    RichTextItem.Text.Header.Level.H1 -> StoryTheme.Typography.heading1
                    RichTextItem.Text.Header.Level.H2 -> StoryTheme.Typography.heading2
                    RichTextItem.Text.Header.Level.H3 -> StoryTheme.Typography.heading3
                    RichTextItem.Text.Header.Level.H4 -> StoryTheme.Typography.heading4
                }
            is RichTextItem.Text.ListItem -> StoryTheme.Typography.paragraph
            is RichTextItem.Text.ChildParagraph -> null
        } ?: TextStyle.Default

    val baseAnnotatedString =
        if (children.isNullOrEmpty()) {
            AnnotatedString(itemText ?: "")
        } else {
            parseRichTextChildrenOfRichText(children)
        }

    val annotatedString =
        when (item) {
            is RichTextItem.Text.ListItem -> buildAnnotatedString {
                withBulletList(bullet = Bullet.Default.copy(padding = 0.5.em)) {
                    withBulletListItem {
                        append(baseAnnotatedString)
                    }
                }
            }
            else -> baseAnnotatedString
        }

    Text(annotatedString, style = textStyle)
}

private fun parseRichTextChildrenOfRichText(children: List<RichTextItem.Text.ChildParagraph>): AnnotatedString {
    return buildAnnotatedString {
        children.forEachIndexed { index, it ->
            val text = it.text ?: ""

            val style = it.styles?.let { styles ->
                SpanStyle(
                    fontWeight = if (styles.contains("STRONG")) FontWeight.Bold else null,
                    fontStyle = if (styles.contains("EMPHASIS")) FontStyle.Italic else FontStyle.Normal
                )
            }

            val linkAnnotation = it.link?.let {
                /* Properties from the design system `link` SpanStyle will override any competing properties
                 * in the `style` determined by the server response. As of 2026-03-03 there are none. */
                val linkBaseStyle = StoryTheme.InlineStyles.link.let { default ->
                    style?.merge(default) ?: default
                }
                LinkAnnotation.Url(
                    it,
                    styles = TextLinkStyles(
                        style = linkBaseStyle
                    )
                )
            }

            /* Join all sibling text with a space _except_ if the text starts with certain
             * kinds of punctuation. This is to handle a peculiarity of how the server-side parser
             * deals w/ spaces, and is likely to be changed on the server-side in the near future. */
            val firstCharacter = text.firstOrNull()
            if (index != 0 && firstCharacter.needsLeadingSpace()) {
                append(" ")
            }

            when {
                linkAnnotation != null -> {
                    withLink(linkAnnotation) {
                        append(text)
                    }
                }
                style != null -> {
                    withStyle(style) {
                        append(text)
                    }
                }
                else -> {
                    append(text)
                }
            }
        }
    }
}

@Composable
fun RichTextItemPhotoComponent(item: RichTextItem.Photo) {
    val imageUrl = item.asset?.url ?: item.url
    AsyncImage(
        model = imageUrl,
        contentDescription = item.altText,
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 30.dp),
        placeholder = ColorPainter(color = grey_03),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun WebViewComponent(url: String) {
    Timber.d("WebViewComponent($url)")
    lateinit var context: Context

    val pinnedHandle = LocalPinnableContainer.current?.pin()
    Timber.d("pinnedHandle: $pinnedHandle")

    @SuppressLint("SetJavaScriptEnabled")
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            context = it
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                /* `clipToOutline` and `setLayerType` help w/ smoother rendering */
                clipToOutline = true
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
        },
        update = {
            if (it.url.isNullOrEmpty()) {
                val baseUrl = context.getEnvironment()?.webEndpoint() ?: Secrets.WebEndpoint.PRODUCTION
                val additionalHeaders = mapOf("Referer" to baseUrl)
                it.loadUrl(url, additionalHeaders)
            }
        },
        onRelease = {
            Timber.d("onRelease()")
            pinnedHandle?.release()
        }
    )
}

private fun Char?.needsLeadingSpace(): Boolean {
    if (this == null || isWhitespace()) return false

    val type = Character.getType(this)
    return type != Character.END_PUNCTUATION.toInt() &&
        type != Character.FINAL_QUOTE_PUNCTUATION.toInt() &&
        this !in ",.!?:;"
}
