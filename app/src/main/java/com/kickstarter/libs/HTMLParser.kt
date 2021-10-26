package com.kickstarter.libs

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import timber.log.Timber
import java.text.ParseException

interface ViewElement

data class VideoViewElement(val sourceUrls: ArrayList<String>) : ViewElement
data class TextViewElement(var components: List<TextComponent>) : ViewElement {

    val attributedText: String
        get() {
            var string = ""
            for (component in components) {
                string += component.text
            }
            return string
        }
}

data class TextComponent(val text: String, val link: String?, val styles: List<TextStyleType>)

data class EmbeddedLinkViewElement(
    val href: String,
    override val src: String,
    val caption: String?,
) : ImageViewElement(src)

open class ImageViewElement(open val src: String) : ViewElement

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

enum class ViewElementType(val tag: String?) {
    IMAGE("img"),
    TEXT(null),
    VIDEO("video"),
    EMBEDDED_LINK(null),
    OEMBED(null),
    UNKNOWN(null);

    companion object {
        fun initialize(element: Element): ViewElementType {
            val tag = element.tag().name
            if (tag == "div") {
                for (attribute in element.attributes()) {
                    if (attribute.key == "class" && attribute.value == "template oembed") {
                        return OEMBED
                    }
                }
            } else if (TextStyleType.initialize(tag) == TextStyleType.LINK && !element.getElementsByTag("img").isEmpty()) {
                return EMBEDDED_LINK
            } else if (TextStyleType.values().map { it.tag }.contains(tag)) {
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

class HTMLParser {

    fun parse(html: String): List<ViewElement> {
        val doc = Jsoup.parse(html)

        val viewElements = mutableListOf<ViewElement>()
        doc.children().forEach {
            viewElements.addAll(parse(it.children()))
        }
        return viewElements.toList()
    }

    private fun parse(children: Elements?): MutableList<ViewElement> {
        val viewElements = mutableListOf<ViewElement>()

        try {
            children?.forEach { element ->
                when (ViewElementType.initialize(element)) {
                    ViewElementType.IMAGE -> {
                        element.dataset()["src"]?.let { sourceUrl ->
                            viewElements.add(ImageViewElement(sourceUrl))
                        }
                    }
                    ViewElementType.TEXT -> {
                        viewElements.add(TextViewElement(parseTextElement(element, mutableListOf(), mutableListOf())))
                        return@forEach
                    }
                    ViewElementType.VIDEO -> {
                        val sourceUrls = element.children().mapNotNull { it.attr("src") }
                        val videoViewElement = VideoViewElement(ArrayList(sourceUrls))
                        viewElements.add(videoViewElement)
                    }
                    ViewElementType.EMBEDDED_LINK -> {
                        val caption = element.getElementsByTag("figcaption").firstOrNull()?.text()
                        (element.attributes().firstOrNull { it.key == "href" })?.value?.let { href ->
                            val imageElements = element.getElementsByTag("img")
                            for (imageElement in imageElements) {
                                imageElement.dataset()["src"]?.let { sourceUrl ->
                                    viewElements.add(EmbeddedLinkViewElement(href, sourceUrl, caption))
                                }
                            }
                        }
                    }
                    ViewElementType.OEMBED -> {
                        val sourceUrls = element.attributes().mapNotNull { if (it.key == "data-href") it.value else null }
                        val videoViewElement = VideoViewElement(ArrayList(sourceUrls))
                        viewElements.add(videoViewElement)
                    }
                    ViewElementType.UNKNOWN -> {
                        print("UNKNOWN ELEMENT")
                    }
                }
                viewElements.addAll(parse(element.children()))
            }
        } catch (exception: ParseException) {
            Timber.e(exception)
        }

        return viewElements
    }

    private fun parseTextElement(
        element: Element,
        tags: MutableList<String>,
        textComponents: MutableList<TextComponent>
    ): MutableList<TextComponent> {
        tags.add(element.tag().name)

        for (node in element.childNodes()) {
            (node as? TextNode)?.let {
                val href = (element.attributes().firstOrNull { it.key == "href" })?.value
                val textStyleList = tags.map { tag -> TextStyleType.initialize(tag) }
                textComponents.add(TextComponent(it.text(), href, textStyleList))
            }
            (node as? Element)?.let {
                // TODO: Out of memory exception when nesting text components on some projects, improve the parsing of childs for nested texts
                // TODO: ej: https://www.kickstarter.com/projects/ww3/yall-means-all-the-emerging-voices-queering-appalachia
                // textComponents.addAll(parseTextElement(it, tags, textComponents))
            }
        }
        return textComponents
    }
}
