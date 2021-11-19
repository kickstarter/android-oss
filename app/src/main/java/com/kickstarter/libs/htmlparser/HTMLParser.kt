package com.kickstarter.libs.htmlparser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

class HTMLParser {

    fun parse(html: String): List<ViewElement> {
        val doc = Jsoup.parse(html)
        val viewElements = mutableListOf<ViewElement>()
        doc.children().forEach {
            viewElements.addAll(parse(it.children()))
        }

        return viewElements
    }

    private fun parse(children: Elements?): List<ViewElement> {
        val viewElements = mutableListOf<ViewElement>()

        children?.forEach { element ->
            val elementType = ViewElementType.initialize(element)
            when (elementType) {
                ViewElementType.IMAGE -> {
                    viewElements.add(element.parseImageElement())
                }
                ViewElementType.TEXT -> {
                    val textViewElement = TextViewElement(parseTextElement(element, mutableListOf(), mutableListOf()))
                    viewElements.add(textViewElement)
                    return@forEach
                }
                ViewElementType.VIDEO -> {
                    val sourceUrls = element.children().mapNotNull { it.attr("src") }
                    val videoViewElement = VideoViewElement(ArrayList(sourceUrls))
                    viewElements.add(videoViewElement)
                }
                ViewElementType.EXTERNAL_SOURCES -> {
                    viewElements.add(element.parseExternalElement())
                }
                else -> {
                    viewElements.addAll(parse(element.children()))
                }
            }
        }
        return viewElements
    }

    private fun parseTextElement(
        element: Element,
        tags: MutableList<String>,
        textComponents: MutableList<TextComponent>
    ): List<TextComponent> {
        tags.add(element.tag().name)

        val blockType = TextComponent.TextBlockType.values().map { it.tag }
            .contains(element.tagName()).let {
                TextComponent.TextBlockType.initialize(element.tagName())
            }

        for (node in element.childNodes()) {
            (node as? TextNode)?.let { textNode ->
                if (textNode.text().trim().isNotEmpty()) {
                    val textStyleList = tags.map { tag -> TextComponent.TextStyleType.initialize(tag) }.filter { it == TextComponent.TextStyleType.UNKNOWN }

                    val href = (element.attributes().firstOrNull { it.key == "href" })?.value

                    textComponents.add(
                        TextComponent(
                            textNode.text(),
                            href,
                            textStyleList,
                            element.toString(),
                            blockType
                        )
                    )
                }
            }
            (node as? Element)?.let {
                parseTextElement(it, tags, textComponents)
            }
        }
        return textComponents.toList()
    }
}
