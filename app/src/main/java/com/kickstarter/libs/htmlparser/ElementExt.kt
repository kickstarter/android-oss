package com.kickstarter.libs.htmlparser

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import com.kickstarter.R
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.boldStyle
import com.kickstarter.libs.utils.extensions.bulletStyle
import com.kickstarter.libs.utils.extensions.color
import com.kickstarter.libs.utils.extensions.italicStyle
import com.kickstarter.libs.utils.extensions.linkStyle
import com.kickstarter.libs.utils.extensions.size
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun Element.extractViewElementTypeFromDiv(): ViewElementType {
    var type: ViewElementType = ViewElementType.UNKNOWN

    if (this.isImageStructure()) {
        if (this.children().getOrNull(0)?.children()?.getOrNull(0)?.tag()?.name == ViewElementType.IMAGE.tag) {
            type = ViewElementType.IMAGE
        }
    } else if (this.isIframeStructure()) {
        if (this.children().getOrNull(0)?.tag()?.name == ViewElementType.EXTERNAL_SOURCES.tag) {
            type = ViewElementType.EXTERNAL_SOURCES
        }
    }

    return type
}

fun Element.isIframeStructure(): Boolean {
    val isTemplateDiv = this.attributes().filter {
        it.key == "class" && it.value == "template oembed"
    }

    return !isTemplateDiv.isNullOrEmpty()
}

fun Element.isImageStructure(): Boolean {
    val isTemplateDiv = this.attributes().filter {
        it.key == "class" && it.value == "template asset"
    }

    return !isTemplateDiv.isNullOrEmpty()
}

fun Element.parseAudioElement(): AudioViewElement {
    val url = this.children()
        .firstOrNull { (it.attr("type") ?: "").contentEquals("audio/mp3") }
        ?.attr("src") ?: ""

    return AudioViewElement(url)
}

fun Element.parseVideoElement(): String {
    val sourceUrls = this.children().mapNotNull { it.attr("src") }
    return sourceUrls.firstOrNull { it.contains("high") } ?: sourceUrls.first()
}
fun Element.parseVideoElementThumbnailUrl(): String? {
    return this.parent()?.attr("data-image")
}

fun Element.parseImageElement(): ImageViewElement {
    var src = ""
    var caption: String? = null
    var href: String? = null

    if (this.parent()?.tag()?.name == "a") {
        href = this.parent()?.attr("href")
    }

    caption = this.attr("data-caption")
    src = this.children().getOrNull(0)?.children()?.getOrNull(0)?.attr("src").toString()

    // - it's a gif collect attribute data-src instead
    if (src.contains(".gif")) {
        src = this.children().getOrNull(0)?.children()?.getOrNull(0)?.attr("data-src").toString()
    }

    return ImageViewElement(src = src, href = href, caption = caption)
}

fun Element.parseExternalElement(): ExternalSourceViewElement {
    val sourceUrls = this.children().getOrNull(0)?.apply {
        this.attr("width", "100%")
    }.toString()

    return ExternalSourceViewElement(sourceUrls)
}

/**
 * This function extract from the textNode a tag list from their ancestors
 * until it detects the parent blockType.
 *
 * Note: BlockTypes are direct children of body HTML tag
 * @param tags - Populates the list of parent tags
 * @param urls - In case of any of the parents is a link(<a>) populates the urls list
 */
private fun extractTextAttributes(
    element: Element,
    tags: MutableList<String>,
    urls: MutableList<String>
) {
    tags.add(element.tagName())
    if (TextComponent.TextBlockType.values().map { it.tag }
        .contains(element.tagName())
    ) {
        // End recursive calls
    } else {
        if (element.tagName() == "a") {
            urls.add(element.attr("href"))
        }
        element.parent()?.let {
            extractTextAttributes(it, tags, urls)
        }
    }
}

private fun getLiElement(element: Element, liElement: MutableList<Element>) {
    if (element.tagName().contains("li")) {
        liElement.add(element)
    } else element.parent()?.let { getLiElement(it, liElement) }
}

/**
 * Each TextComponent will have:
 * - it's own list of styles to apply
 * - the url string in case the textComponent was a link
 * - the text to display
 */
fun TextNode.parseTextElement(element: Element): TextComponent {
    val tagsOther = mutableListOf<String>()
    val urls = mutableListOf<String>()

    extractTextAttributes(element, tagsOther, urls)

    // - Extract from the list of styles the 'UNKNOWN', and 'LIST', the list style is process separately
    val textStyleList = tagsOther.map { tag -> TextComponent.TextStyleType.initialize(tag) }
        .filter { it != TextComponent.TextStyleType.LIST }
        .filter { it != TextComponent.TextStyleType.UNKNOWN }
        .toMutableList()

    val href = urls.firstOrNull() ?: ""

    // - I am child of a li, but not the element itself
    if (tagsOther.contains("ul")) {
        val list = mutableListOf<Element>()
        getLiElement(element, liElement = list)
        val liElement = list.firstOrNull()
        val parent = element.parent()
        val grandFather = parent?.parent()

        // - Clean up the liElement, many times you get empty child TextNodes or TextNodes with &nbsp
        val liChildElements = liElement?.childNodes()?.filter {
            !(it is TextNode && it.text().trim().isEmpty())
        }

        // Am I the first child of the LI element?
        if (this == liChildElements?.first() || element == liChildElements?.first()) {
            textStyleList.add(TextComponent.TextStyleType.LIST)
        } else {
            // Is my parent the first child of the LI element?
            if (liChildElements?.first() == parent) {
                textStyleList.add(TextComponent.TextStyleType.LIST)
            }
        }

        // Am I the last child of the LI element?
        if (this == liChildElements?.last() || element == liChildElements?.last()) {
            textStyleList.add(TextComponent.TextStyleType.LIST_END)
        } else {
            // Is my parent the last child of the LI element?
            if (liChildElements?.last() == parent) {
                textStyleList.add(TextComponent.TextStyleType.LIST_END)
            }
        }

        if (textStyleList.size >= 3) {
            // Is my gradFather the first child of the LI element?
            if (liChildElements?.first() == grandFather) {
                textStyleList.add(TextComponent.TextStyleType.LIST)
            }

            // Is my gradFather the last child of the LI element?
            if (liChildElements?.last() == grandFather) {
                textStyleList.add(TextComponent.TextStyleType.LIST_END)
            }
        }
    }

    return TextComponent(
        this.text(),
        href,
        textStyleList
    )
}

fun TextViewElement.getStyledComponents(
    context: Context
): SpannableStringBuilder {
    val joinedSpanned = SpannableStringBuilder("")
    this.components.forEach { textItem ->
        var componentText = textItem.text
        val href = textItem.link ?: ""

        // - The end list style will be applied only to the LAST child of the LI element
        if (textItem.styles.contains(TextComponent.TextStyleType.LIST_END)) {
            componentText += "\n"
        }

        val spannable = SpannableString(componentText)
        val bodySize = context.resources.getDimensionPixelSize(R.dimen.callout)
        spannable.color()
        spannable.size(bodySize)
        textItem.styles.forEach { style ->
            when (style) {
                TextComponent.TextStyleType.BOLD -> spannable.boldStyle()
                TextComponent.TextStyleType.EMPHASIS -> spannable.italicStyle()
                TextComponent.TextStyleType.LINK -> spannable.linkStyle {
                    ApplicationUtils.openUrlExternally(
                        context,
                        href
                    )
                }
                // - The bullet style will be applied only to the FIRST child of the LI element
                TextComponent.TextStyleType.LIST -> spannable.bulletStyle()
                TextComponent.TextStyleType.HEADER1 -> {
                    val size = context.resources.getDimensionPixelSize(R.dimen.parser_h1)
                    spannable.size(size)
                    spannable.boldStyle()
                }
                TextComponent.TextStyleType.HEADER2 -> {
                    val size = context.resources.getDimensionPixelSize(R.dimen.parser_h2)
                    spannable.size(size)
                    spannable.boldStyle()
                }
                TextComponent.TextStyleType.HEADER3 -> {
                    val size = context.resources.getDimensionPixelSize(R.dimen.parser_h3)
                    spannable.size(size)
                    spannable.boldStyle()
                }
                TextComponent.TextStyleType.HEADER4 -> {
                    val size = context.resources.getDimensionPixelSize(R.dimen.parser_h4)
                    spannable.size(size)
                    spannable.boldStyle()
                }
                TextComponent.TextStyleType.HEADER5 -> {
                    val size = context.resources.getDimensionPixelSize(R.dimen.parser_h5)
                    spannable.size(size)
                    spannable.boldStyle()
                }
                TextComponent.TextStyleType.HEADER6 -> {
                    val size = context.resources.getDimensionPixelSize(R.dimen.parser_h6)
                    spannable.size(size)
                    spannable.boldStyle()
                }
                else -> {}
            }
        }

        joinedSpanned.append(spannable)
    }
    return joinedSpanned
}
