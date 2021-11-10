package com.kickstarter.libs.htmlparser

import org.jsoup.nodes.Element

interface ViewElement

data class VideoViewElement(val sourceUrls: List<String>) : ViewElement
data class TextViewElement(var components: List<TextComponent>) : ViewElement {
    val attributedText: String
        get() {
            var string = ""
            // - TODO: distinct will avoid duplicated, but is a workaround should be fixed on the parser
            components.distinct()
                .map {
                    string += it.text
                }
            return string
        }
}

data class TextComponent(val text: String, val link: String?) {
    enum class TextStyleType(val tag: String?) {
        PARAGRAPH("p"),
        HEADER1("h1"),
        BOLD("strong"),
        LINK("a"),
        EMPHASIS("em"),
        CAPTION("figcaption"),
        LIST("li"),
        UNKNOWN(null);

        companion object {
            fun initialize(tag: String): TextStyleType {
                return (values().find { it.tag == tag }) ?: UNKNOWN
            }
        }
    }
}

data class ImageViewElement(
    val src: String,
    val href: String? = null,
    val caption: String? = null
) : ViewElement

data class ExternalSourceViewElement(
    val htmlContent: String
) : ViewElement

enum class ViewElementType(val tag: String?) {
    IMAGE("img"),
    TEXT(null),
    VIDEO("video"),
    EXTERNAL_SOURCES("iframe"),
    UNKNOWN(null);

    companion object {
        fun initialize(element: Element): ViewElementType {
            val tag = element.tag().name
            when {
                tag == "a" -> {
                    element.children().find { it.tagName() == "div" }?.let { return@let it.extractViewElementTypeFromDiv() }
                    // TODO: Return text element in case is only a link not an image/video wrapped in a link
                }
                tag == "div" -> {
                    return element.extractViewElementTypeFromDiv()
                }
                TextComponent.TextStyleType.values().map { it.tag }.contains(tag) -> {
                    return TEXT
                }
                tag == VIDEO.tag -> {
                    return VIDEO
                }
            }
            return UNKNOWN
        }
    }
}
