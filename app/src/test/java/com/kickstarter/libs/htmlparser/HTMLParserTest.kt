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

    @Test
    fun parseImageWithoutCaptionOrLink() {
        val src = "https://ksr-qa-ugc.imgix.net/assets/035/272/957/f885374b7b855bd5a135dec24232a059_original.png?ixlib=rb-4.0.2&w=700&fit=max&v=1635378787&auto=format&gif-q=50&lossless=true&s=02a9283693d143fe7ba04c1a0d52fa4c"
        val onlyImageHtml = "<div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"\" data-caption=\"\" data-id=\"35272957\">\\n " +
            "<figure>\\n " + "" +
            "<img alt=\"\" class=\"fit\" src=\"$src\">\\n " +
            "</figure>\\n\\n</div>"

        val listOfElements = HTMLParser().parse(onlyImageHtml)
        val imageView: ImageViewElement = listOfElements.first() as ImageViewElement
        assert(listOfElements.size == 1)
        assert(imageView.caption == "")
        assert(imageView.href == null)
        assert(imageView.src == src)
    }

    @Test
    fun parseImageWithCaptionWithoutLink() {
        val src = "https://ksr-qa-ugc.imgix.net/assets/035/272/957/f885374b7b855bd5a135dec24232a059_original.png?ixlib=rb-4.0.2&w=700&fit=max&v=1635378787&auto=format&gif-q=50&lossless=true&s=02a9283693d143fe7ba04c1a0d52fa4c"
        val onlyImageHtml = "<div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"\" data-caption=\"This is Coach Beard with a caption\" data-id=\"35272957\">\\n " +
            "<figure>\\n " + "" +
            "<img alt=\"\" class=\"fit\" src=\"$src\">\\n " +
            "</figure>\\n\\n</div>"

        val listOfElements = HTMLParser().parse(onlyImageHtml)
        val imageView: ImageViewElement = listOfElements.first() as ImageViewElement
        assert(listOfElements.size == 1)
        assert(imageView.caption == "This is Coach Beard with a caption")
        assert(imageView.href == null)
        assert(imageView.src == src)
    }

    @Test
    fun parseImageWithCaptionAndLink() {
        val src = "https://ksr-qa-ugc.imgix.net/assets/035/272/957/f885374b7b855bd5a135dec24232a059_original.png?ixlib=rb-4.0.2&w=700&fit=max&v=1635378787&auto=format&gif-q=50&lossless=true&s=02a9283693d143fe7ba04c1a0d52fa4c"
        val href = "http://record.pt/"
        val onlyImageHtml = "<a href=$href target=\"_blank\" rel=\"noopener\">" +
            "<div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"\" data-caption=\"This is an Android with a caption and a link\" data-id=\"35272959\">\n" +
            "<figure>" +
            "\n<img alt=\"\" class=\"fit\" src=$src>\n" +
            "<figcaption class=\"px2\">This is an Android with a caption and a link</figcaption>" +
            "</figure>" +
            "\n\n</div>\n" +
            "</a>"

        val listOfElements = HTMLParser().parse(onlyImageHtml)
        val imageView: ImageViewElement = listOfElements.first() as ImageViewElement
        assert(listOfElements.size == 1)
        assert(imageView.caption == "This is an Android with a caption and a link")
        assert(imageView.href == href)
        assert(imageView.src == src)
    }

    @Test
    fun parseGifWithCaptionAndLink() {
        val src = "https://ksr-qa-ugc.imgix.net/assets/035/272/962/ad1848184f8254f017730e6978565521_original.gif?ixlib=rb-4.0.2&w=700&fit=max&v=1635378954&auto=format&frame=1&q=92&s=fae855ae1e9f3919c1631c074419cd43"
        val href = "https://twitter.com/TedLasso"
        val onlyImageHtml = "<a href=\"https://twitter.com/TedLasso\" target=\"_blank\" rel=\"noopener\">" +
            "<div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"\" data-caption=\"This is Ted having the time of his life with a caption and a link\" data-id=\"35272962\">\n" +
            "<figure>" +
            "\n<img alt=\"\" class=\"fit js-lazy-image\" data-src=$src src=\\\"https://ksr-qa-ugc.imgix.net/assets/035/272/962/ad1848184f8254f017730e6978565521_original.gif?ixlib=rb-4.0.2&amp;w=700&amp;fit=max&amp;v=1635378954&amp;auto=format&amp;frame=1&amp;q=92&amp;s=fae855ae1e9f3919c1631c074419cd43\\\">\n" +
            "<figcaption class=\"px2\">This is Ted having the time of his life with a caption and a link</figcaption>" +
            "</figure>" +
            "\n\n</div>\n" +
            "</a>"

        val listOfElements = HTMLParser().parse(onlyImageHtml)
        val imageView: ImageViewElement = listOfElements.first() as ImageViewElement
        assert(listOfElements.size == 1)
        assert(imageView.caption == "This is Ted having the time of his life with a caption and a link")
        assert(imageView.href == href)
        assert(imageView.src == src)
    }
}
