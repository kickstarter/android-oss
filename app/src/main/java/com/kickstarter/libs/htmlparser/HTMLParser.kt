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
            when (ViewElementType.initialize(element)) {
                ViewElementType.IMAGE -> {
                    viewElements.add(element.parseImageElement())
                }
                ViewElementType.TEXT -> {
                    val textViewElement = TextViewElement(parseTextElement(element, mutableListOf()))
                    viewElements.add(textViewElement)
                    return@forEach
                }
                ViewElementType.VIDEO -> {
                    val videoViewElement = VideoViewElement(
                        element.parseVideoElement(),
                        element
                            .parseVideoElementThumbnailUrl(),
                        0
                    )
                    viewElements.add(videoViewElement)
                }
                ViewElementType.AUDIO -> {
                    val audioElement = element.parseAudioElement()
                    viewElements.add(audioElement)
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
