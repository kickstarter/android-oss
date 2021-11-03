package com.kickstarter.libs.htmlparser

import junit.framework.TestCase
import org.junit.Test
import java.net.URI

class HTMLParserTest {

    @Test
    fun testParseExternalSourceElement() {

        val linksArray = arrayOf(
            "https://www.youtube.com/embed/3u7EIiohs6U?feature=oembed&wmode=transparent",
            "https://w.soundcloud.com/player/?visual=true&url=https%3A%2F%2Fapi.soundcloud.com%2Ftracks%2F1088168317&show_artwork=true&maxwidth=560",
            "https://open.spotify.com/embed/track/31H5dHBR7g381udIzXSKIE?si=62607f8611e74f0d&utm_source=oembed"
        )

        val htmlString = "<div class=\"template oembed\" contenteditable=\"false\" " +
            "data-href=\"https://www.youtube.com/watch?v=3u7EIiohs6U\"> <iframe width=\"356\"" +
            " height=\"200\" src=\"${linksArray[0]}\" frameborder=\"0\" " +
            "allow=\"accelerometer;autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" " +
            "allowfullscreen ></iframe> </div> <div class=\"template oembed\" " +
            "contenteditable=\"false\" data-href=\"https://soundcloud" +
            ".com/jamesblakeofficial/say-what-you-will\"> <iframe width=\"560\" " +
            "height=\"400\" scrolling=\"no\" frameborder=\"no\" " +
            "src=\"${linksArray[1]}}\"></iframe> </div> " +
            "<div class=\"template oembed\" contenteditable=\"false\" " +
            "data-href=\"https://open.spotify.com/track/31H5dHBR7g381udIzXSKIE?si=62607f8611e74f0d\">" +
            " <iframe width=\"100%\" height=\"80\" title=\"Spotify Embed: Famous Last Words\" frameborder=\"0\"" +
            " allowfullscreen allow=\"autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture\" src=\"${linksArray[2]}\" ></iframe> </div>"

        val listViewElements = HTMLParser().parse(htmlString)

        TestCase.assertEquals(3, listViewElements.size)

        listViewElements.map {
            it as? ExternalSourceViewElement
        }.forEachIndexed { index, item ->
            TestCase.assertTrue(
                item?.htmlContent?.contains(URI.create(linksArray[index]).host)
                    ?: false
            )
        }
    }
}
