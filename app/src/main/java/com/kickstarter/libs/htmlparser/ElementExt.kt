package com.kickstarter.libs.htmlparser

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
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
 * Note: BlockTypes are direct childs of body HTML tag
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

fun TextNode.parseTextElement(element: Element): TextComponent {
    val tagsOther = mutableListOf<String>()
    val urls = mutableListOf<String>()
    extractTextAttributes(element, tagsOther, urls)
    val textStyleList = tagsOther.map { tag -> TextComponent.TextStyleType.initialize(tag) }.filter { it != TextComponent.TextStyleType.UNKNOWN }
    val href = urls.firstOrNull() ?: ""

    return TextComponent(
        this.text(),
        href,
        textStyleList
    )
}

fun TextViewElement.getStyledComponents(
    bodySize: Int,
    headerSize: Int,
    context: Context
): SpannableStringBuilder {
    val joinedSpanned = SpannableStringBuilder("")
    this.components.forEach { textItem ->
        var componentText = textItem.text
        val href = textItem.link ?: ""

        //-  In order to correctly apply the list style we need to add the end line jump, otherwise it gets applied only to the first item
        if(textItem.styles.contains(TextComponent.TextStyleType.LIST)) {
            componentText += "\n"
        }
        
        val spannable = SpannableString(componentText)
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
                TextComponent.TextStyleType.LIST -> spannable.bulletStyle()
                TextComponent.TextStyleType.HEADER -> {
                    spannable.size(headerSize)
                    spannable.boldStyle()
                }
            }
        }

        joinedSpanned.append(spannable)
    }
    return joinedSpanned
}
