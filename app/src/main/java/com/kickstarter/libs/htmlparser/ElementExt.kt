package com.kickstarter.libs.htmlparser

import org.jsoup.nodes.Element

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
        val scr = this.attr("src")
        if (scr.contains("youtube")) {
            this.attr("src", "$scr&amp;fs=0")
        }
    }.toString()

    return ExternalSourceViewElement(sourceUrls)
}
