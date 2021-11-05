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

data class EmbeddedLinkViewElement(
    val href: String,
    override val src: String,
    val caption: String?
) : ImageViewElement(src)

open class ImageViewElement(open val src: String) : ViewElement

data class ExternalSourceViewElement(
    val htmlContent: String
) : ViewElement

enum class ViewElementType(val tag: String?) {
    IMAGE("img"),
    TEXT(null),
    VIDEO("video"),
    EXTERNAL_SOURCES("iframe"),
    EMBEDDED_LINK(null),
    OEMBED(null),
    UNKNOWN(null);

    companion object {
        fun initialize(element: Element): ViewElementType {
            val tag = element.tag().name
            if (tag == "div") {

                for (attribute in element.attributes()) {
                    if (attribute.key == "class" && attribute.value == "template oembed") {
                        if (element.children().getOrNull(0)?.tag()?.name == EXTERNAL_SOURCES.tag) {
                            return EXTERNAL_SOURCES
                        }
                        return OEMBED
                    }
                }
            } else if (TextComponent.TextStyleType.initialize(tag) == TextComponent.TextStyleType.LINK && !element.getElementsByTag("img").isEmpty()) {
                return EMBEDDED_LINK
            } else if (TextComponent.TextStyleType.values().map { it.tag }.contains(tag)) {
                return TEXT
            } else if (tag == IMAGE.tag) {
                return IMAGE
            } else if (tag == VIDEO.tag) {
                return VIDEO
            }
            return UNKNOWN
        }
    }
}
