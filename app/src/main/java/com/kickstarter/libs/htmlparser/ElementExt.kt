package com.kickstarter.libs.htmlparser

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

fun Element.parseVideoElement(): String {
    val sourceUrls = this.children().mapNotNull { it.attr("src") }
    return sourceUrls.firstOrNull { it.contains("high") } ?: sourceUrls.first()
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
