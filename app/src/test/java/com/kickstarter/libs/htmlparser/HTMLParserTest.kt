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

    @Test
    fun parseTextElementListWithNestedLinks() {

        val url = "https://www.meneame.net/"
        val html = "<ul><li><a href=$url target=\\\"_blank\\\" rel=\\\"noopener\\\"><em><strong>Meneane</strong></em></a><a href=$url target=\\\"_blank\\\" rel=\\\"noopener\\\">Another URL in this list</a> and some text</li></ul>"
        val listOfElements = HTMLParser().parse(html)
        val textElement: TextViewElement = listOfElements.first() as TextViewElement

        assert(textElement.components.size == 3)

        // - First component
        val firstTextComponent = textElement.components[0]
        assert(firstTextComponent.link == url)
        assert(firstTextComponent.text == "Meneane")
        assert(firstTextComponent.styles.size == 4)
        assert(firstTextComponent.styles[0] == TextComponent.TextStyleType.BOLD)
        assert(firstTextComponent.styles[1] == TextComponent.TextStyleType.EMPHASIS)
        assert(firstTextComponent.styles[2] == TextComponent.TextStyleType.LINK)
        assert(firstTextComponent.styles[3] == TextComponent.TextStyleType.LIST)

        // - Second Component
        val secondComponent = textElement.components[1]
        assert(secondComponent.link == url)
        assert(secondComponent.text == "Another URL in this list")
        assert(secondComponent.styles.size == 1)
        assert(secondComponent.styles[0] == TextComponent.TextStyleType.LINK)

        // - Third Component
        val thirdComponent = textElement.components[2]
        assert(thirdComponent.link == "")
        assert(thirdComponent.text == " and some text")
        assert(thirdComponent.styles.size == 1)
        assert(thirdComponent.styles[0] == TextComponent.TextStyleType.LIST_END)
    }

    @Test
    fun parseTextElementParagraphWithEmphasisAndBold() {
        val html = "<p>This is a paragraph about bacon – Bacon ipsum dolor amet ham chuck short ribs, shank flank cupim frankfurter chicken. Sausage frankfurter chicken ball tip, drumstick brisket pork chop turkey. Andouille bacon ham hock, pastrami sausage pork chop corned beef frankfurter shank chislic short ribs. Hamburger bacon pork belly, drumstick pork chop capicola kielbasa pancetta buffalo pork. Meatball doner pancetta ham ribeye. Picanha ham venison ribeye short loin beef, tail pig ball tip buffalo salami shoulder ground round chicken. Porchetta capicola drumstick, tongue fatback pork pork belly cow sirloin ham hock flank venison beef ribs.<strong><em>Bold word Italic word</em></strong></p>"
        val listOfElements = HTMLParser().parse(html)
        val textElement: TextViewElement = listOfElements.first() as TextViewElement

        assert(textElement.components.size == 2)

        // First element
        val firstTextComponent = textElement.components[0]
        assert(firstTextComponent.link == "")
        assert(firstTextComponent.text == "This is a paragraph about bacon – Bacon ipsum dolor amet ham chuck short ribs, shank flank cupim frankfurter chicken. Sausage frankfurter chicken ball tip, drumstick brisket pork chop turkey. Andouille bacon ham hock, pastrami sausage pork chop corned beef frankfurter shank chislic short ribs. Hamburger bacon pork belly, drumstick pork chop capicola kielbasa pancetta buffalo pork. Meatball doner pancetta ham ribeye. Picanha ham venison ribeye short loin beef, tail pig ball tip buffalo salami shoulder ground round chicken. Porchetta capicola drumstick, tongue fatback pork pork belly cow sirloin ham hock flank venison beef ribs.")
        assert(firstTextComponent.styles.isEmpty())

        // Second Element
        val secondElement = textElement.components[1]
        assert(secondElement.link == "")
        assert(secondElement.text == "Bold word Italic word")
        assert(secondElement.styles.size == 2)
        assert(secondElement.styles[0] == TextComponent.TextStyleType.EMPHASIS)
        assert(secondElement.styles[1] == TextComponent.TextStyleType.BOLD)
    }

    @Test
    fun parseTwoTextElementsParagraphWithLinkAndNestedStyles() {
        val url1 = "http://record.pt/"
        val url2 = "http://recordblabla.pt/"
        val html = "<p><a href=$url1 target=\"_blank\" rel=\"noopener\"><strong>What about a bold link to that same newspaper website?</strong></a></p>\n<p><a href=$url2 target=\"_blank\" rel=\"noopener\"><em>Maybe an italic one?</em></a></p>"

        val listOfElements = HTMLParser().parse(html)
        assert(listOfElements.size == 2)

        val firstTextElement: TextViewElement = listOfElements.first() as TextViewElement
        assert(firstTextElement.components.size == 1)
        assert(firstTextElement.components[0].link == url1)
        assert(firstTextElement.components[0].text == "What about a bold link to that same newspaper website?")
        assert(firstTextElement.components[0].styles[0] == TextComponent.TextStyleType.BOLD)
        assert(firstTextElement.components[0].styles[1] == TextComponent.TextStyleType.LINK)

        val secondTextElement: TextViewElement = listOfElements.last() as TextViewElement
        assert(secondTextElement.components.size == 1)
        assert(secondTextElement.components[0].link == url2)
        assert(secondTextElement.components[0].text == "Maybe an italic one?")
        assert(secondTextElement.components[0].styles[0] == TextComponent.TextStyleType.EMPHASIS)
        assert(secondTextElement.components[0].styles[1] == TextComponent.TextStyleType.LINK)
    }

    @Test
    fun parseTextElementHeadline() {
        val html = "<h1 id=\\\"h:this-is-a-headline\\\" class=\\\"page-anchor\\\">This is a headline</h1>"
        val listOfElements = HTMLParser().parse(html)
        assert(listOfElements.size == 1)

        val textElement: TextViewElement = listOfElements.last() as TextViewElement
        assert(textElement.components.size == 1)
        assert(textElement.components[0].text == "This is a headline")
        assert(textElement.components[0].styles[0] == TextComponent.TextStyleType.HEADER)
    }

    @Test
    fun parseVideoElement() {
        val highURL = "https://v.kickstarter.com/1638357287_13f84af2b93199f2f08d64f29bb5849c4ccc9ae7/assets/034/747/335/b060e1907417e761401ac958a6df9cd7_h264_high.mp4"
        val baseURL = "https://v.kickstarter" +
            ".com/1638357287_13f84af2b93199f2f08d64f29bb5849c4ccc9ae7/assets/034/747/335/b060e1907417e761401ac958a6df9cd7_h264_base.mp4"
        val thumbnailUrl = "https://dr0rfahizzuzj.cloudfront.net/assets/034/747/335/b060e1907417e761401ac958a6df9cd7_h264_high.jpg?2021"

        val html = "<div class=\"video-player\" data-image=$thumbnailUrl data-dimensions=\"{&quot;" +
            "width&quot;:640,&quot;height&quot;:360}\" data-context=\"Story Description\"> " +
            " <video class=\"landscape\" preload=\"none\">" +
            " <source src=$highURL type=\"video/mp4; codecs=&quot;avc1.64001E, mp4a.40.2&quot;\">  " +
            " <source src=$baseURL type=\"video/mp4; codecs=&quot;avc1.42E01E, mp4a.40" +
            ".2&quot;\"> You'll need an HTML5 capable browser to see this content.  " +
            "</video> </div>"

        val listOfElements = HTMLParser().parse(html)
        assert(listOfElements.size == 1)

        val videoViewElement: VideoViewElement = listOfElements.last() as VideoViewElement
        TestCase.assertEquals(videoViewElement.sourceUrl, highURL)
        TestCase.assertEquals(videoViewElement.thumbnailUrl, thumbnailUrl)
    }

    @Test
    fun parseVideoElementWithOneSource() {
        val baseURL = "https://v.kickstarter" +
            ".com/1638357287_13f84af2b93199f2f08d64f29bb5849c4ccc9ae7/assets/034/747/335/b060e1907417e761401ac958a6df9cd7_h264_base.mp4"
        val thumbnailUrl = "https://dr0rfahizzuzj.cloudfront.net/assets/034/747/335/b060e1907417e761401ac958a6df9cd7_h264_high.jpg?2021"

        val html = "<div class=\"video-player\" data-image=$thumbnailUrl data-dimensions=\"{&quot;" +
            "width&quot;:640,&quot;height&quot;:360}\" data-context=\"Story Description\"> " +
            " <video class=\"landscape\" preload=\"none\">" +
            " <source src=$baseURL type=\"video/mp4; codecs=&quot;avc1.42E01E, mp4a.40" +
            ".2&quot;\"> You'll need an HTML5 capable browser to see this content.  " +
            "</video> </div>"
        val listOfElements = HTMLParser().parse(html)
        assert(listOfElements.size == 1)

        val videoViewElement: VideoViewElement = listOfElements.last() as VideoViewElement
        TestCase.assertEquals(videoViewElement.sourceUrl, baseURL)
        TestCase.assertEquals(videoViewElement.thumbnailUrl, thumbnailUrl)
    }
}
