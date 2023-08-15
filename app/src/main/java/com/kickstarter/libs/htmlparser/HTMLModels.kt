package com.kickstarter.libs.htmlparser

import org.jsoup.nodes.Element

interface ViewElement

data class TextViewElement(var components: List<TextComponent>) : ViewElement

data class AudioViewElement(
    val sourceUrl: String
) : ViewElement

data class VideoViewElement(
    val sourceUrl: String,
    val thumbnailUrl: String?,
    val seekPosition: Long
) : ViewElement

data class TextComponent(
    var text: String,
    val link: String?,
    val styles: List<TextStyleType>
) {

    // - Direct body childs for text allows only TextBlockTypes. All header tags defined by w3school https://www.w3schools.com/tags/tag_hn.asp
    enum class TextBlockType(val tag: String?) {
        PARAGRAPH("p"),
        HEADER1("h1"),
        HEADER2("h2"),
        HEADER3("h3"),
        HEADER4("h4"),
        HEADER5("h5"),
        HEADER6("h6"),
        LIST("ul");

        companion object {
            fun initialize(tag: String): TextBlockType? {
                return values().firstOrNull { it.tag == tag }
            }
        }
    }

    // - Styles to apply
    enum class TextStyleType(val tag: String?) {
        BOLD("strong"),
        EMPHASIS("em"),
        LIST("li"),
        LIST_END("</li>"),
        LINK("a"),
        HEADER1("h1"),
        HEADER2("h2"),
        HEADER3("h3"),
        HEADER4("h4"),
        HEADER5("h5"),
        HEADER6("h6"),
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
    AUDIO("audio"),
    EXTERNAL_SOURCES("iframe"),
    UNKNOWN(null);

    companion object {
        fun initialize(element: Element): ViewElementType {
            val tag = element.tag().name
            when {
                tag == "a" -> {
                    element.children().find { it.tagName() == "div" }?.let { return@let it.extractViewElementTypeFromDiv() }
                }
                tag == "div" -> {
                    return element.extractViewElementTypeFromDiv()
                }
                TextComponent.TextBlockType.values().map { it.tag }.contains(tag) -> {
                    return TEXT
                }
                tag == VIDEO.tag -> {
                    return VIDEO
                }
                tag == AUDIO.tag -> {
                    return AUDIO
                }
            }
            return UNKNOWN
        }
    }
}
