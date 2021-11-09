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
                    viewElements.add(TextViewElement(parseTextElement(element, mutableListOf(), mutableListOf())))
                    return@forEach
                }
                ViewElementType.VIDEO -> {
                    val sourceUrls = element.children().mapNotNull { it.attr("src") }
                    val videoViewElement = VideoViewElement(ArrayList(sourceUrls))
                    viewElements.add(videoViewElement)
                }
                ViewElementType.EXTERNAL_SOURCES -> {
                    val sourceUrls = element.children()[0].toString()
                    val externalSourceViewElement = ExternalSourceViewElement(sourceUrls)
                    viewElements.add(externalSourceViewElement)
                }
                ViewElementType.UNKNOWN -> {
                    print("UNKNOWN ELEMENT")
                }
            }
            viewElements.addAll(parse(element.children()))
        }
        return viewElements
    }

    private fun parseTextElement(
        element: Element,
        tags: MutableList<String>,
        textComponents: MutableList<TextComponent>
    ): List<TextComponent> {
        tags.add(element.tag().name)

        for (node in element.childNodes()) {
            (node as? TextNode)?.let {
                val href = (element.attributes().firstOrNull { it.key == "href" })?.value
                textComponents.add(TextComponent(element.toString(), href))
            }
            (node as? Element)?.let {
                // TODO: Out of memory exception when nesting text components on some projects, improve the parsing of childs for nested texts
                // TODO: ej: https://www.kickstarter.com/projects/ww3/yall-means-all-the-emerging-voices-queering-appalachia
                // TODO: ej: https://staging.kickstarter.com/projects/334999551/shades-of-fear
                // TODO: ej: https://staging.kickstarter.com/projects/thedreadmachine/mixtape-1986

                textComponents.addAll(parseTextElement(it, tags, textComponents))
            }
        }
        return textComponents.toList()
    }
}
