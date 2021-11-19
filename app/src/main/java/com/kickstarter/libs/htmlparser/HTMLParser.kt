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
                    val textViewElement = TextViewElement(parseTextElement(element, mutableListOf()))
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

    private fun getTextBlockType(
        element: Element,
        tags: MutableList<String>
    ): TextComponent.TextBlockType? =
        if (TextComponent.TextBlockType.values().map { it.tag }
            .contains(element.tagName())
        ) {
            TextComponent.TextBlockType.initialize(element.tagName())
        } else {
            tags.add(element.tagName())
            if (element.tagName() == "a") {
                // urls.add(element.attr("href"))
            }
            element.parent()?.let {
                getTextBlockType(it, tags)
            }
        }

    private fun parseTextElement(
        element: Element,
        textComponents: MutableList<TextComponent>
    ): List<TextComponent> {

        for (node in element.childNodes()) {
            (node as? TextNode)?.let { textNode ->
                if (textNode.text().trim().isNotEmpty()) {

                    val href = (element.attributes().firstOrNull { it.key == "href" })?.value ?: ""
                    val tagsOther = mutableListOf<String>()
                    val blockType = getTextBlockType(element, tagsOther)
                    val textStyleList = tagsOther.map { tag -> TextComponent.TextStyleType.initialize(tag) }.filter { it != TextComponent.TextStyleType.UNKNOWN }

                    blockType?.let { block ->
                        textComponents.add(
                            TextComponent(
                                textNode.text(),
                                href,
                                textStyleList,
                                element.toString(),
                                block,
                                tagsOther
                            )
                        )
                    }
                }
            }
            (node as? Element)?.let {
                parseTextElement(it, textComponents)
            }
        }
        return textComponents.toList()
    }
}
