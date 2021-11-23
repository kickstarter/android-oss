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

    /**
     * This function extract from the textNode a tag list from their ancestors
     * until it detects the parent blockType.
     * @param tags - Populates the list of parent tags
     * @param urls - In case of any of the parents is a link(<a>) populates the urls list
     * Returns blockType
     */
    private fun extractTextAttributes(
        element: Element,
        tags: MutableList<String>,
        urls: MutableList<String>
    ): TextComponent.TextBlockType? =
        if (TextComponent.TextBlockType.values().map { it.tag }
            .contains(element.tagName())
        ) {
            TextComponent.TextBlockType.initialize(element.tagName())
        } else {
            tags.add(element.tagName())
            if (element.tagName() == "a") {
                urls.add(element.attr("href"))
            }
            element.parent()?.let {
                extractTextAttributes(it, tags, urls)
            }
        }

    private fun parseTextElement(
        element: Element,
        textComponents: MutableList<TextComponent>
    ): List<TextComponent> {

        for (node in element.childNodes()) {
            (node as? TextNode)?.let { textNode ->
                if (textNode.text().trim().isNotEmpty()) {
                    textComponents.add(textNode.parseTextElement(element))
                }
            }
            (node as? Element)?.let {
                parseTextElement(it, textComponents)
            }
        }
        return textComponents.toList()
    }
}
