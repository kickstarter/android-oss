package com.kickstarter.screenshoot.testing.ui.components

import com.karumi.shot.ScreenshotTest

class TextViewElementShotTest : ScreenshotTest {
/*
    lateinit var component: ApplicationComponent
    lateinit var app: InstrumentedApp
    private var headerSize: Int = 0
    private var body: Int = 0

    @Before
    fun setup() {
        // - Test Application
        app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()

        headerSize = app.resources.getDimensionPixelSize(R.dimen.title_3)
        body = app.resources.getDimensionPixelSize(R.dimen.callout)
    }

    @Test
    fun textElementWithListWithNestedTypes() {
        val textView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.view_element_text_from_html, null
            ) as ConstraintLayout
            ).findViewById(R.id.text_view) as AppCompatTextView

        val html = "<ul>\n" +
            "   <li>This</li>\n" +
            "   <li><strong>is</strong></li>\n" +
            "   <li><em>a</em></li>\n" +
            "   <li><a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\">list</a></li>\n" +
            "</ul>"

        val listOfElements = HTMLParser().parse(html)

        val element = listOfElements.first() as TextViewElement

        textView.text = element.getStyledComponents(body, headerSize, app)
        compareScreenshot(textView)
    }

    @Test
    fun textElementWithParagraphLinkBold() {
        val textView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.view_element_text_from_html, null
            ) as ConstraintLayout
            ).findViewById(R.id.text_view) as AppCompatTextView

        val url1 = "http://record.pt/"
        val url2 = "http://recordblabla.pt/"
        val html = "<p><a href=$url1 target=\"_blank\" rel=\"noopener\"><strong>What about a bold link to that same newspaper website?</strong></a></p>\n<p><a href=$url2 target=\"_blank\" rel=\"noopener\"><em>Maybe an italic one?</em></a></p>"

        val listOfElements = HTMLParser().parse(html)

        val element = listOfElements.first() as TextViewElement

        textView.text = element.getStyledComponents(body, headerSize, app)
        compareScreenshot(textView)
    }

    @Test
    fun textElementHeadline() {

        val textView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.view_element_text_from_html, null
            ) as ConstraintLayout
            ).findViewById(R.id.text_view) as AppCompatTextView

        val html = "<h1 id=\\\"h:this-is-a-headline\\\" class=\\\"page-anchor\\\">This is a headline</h1>"

        val listOfElements = HTMLParser().parse(html)

        val element = listOfElements.first() as TextViewElement

        textView.text = element.getStyledComponents(body, headerSize, app)
        compareScreenshot(textView)
    }

    @Test
    fun textElementParagraphWithLinks() {
        val textView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.view_element_text_from_html, null
            ) as ConstraintLayout
            ).findViewById(R.id.text_view) as AppCompatTextView

        val html = "<p>This is the same paragraph about bacon but with some sprinkles – <strong>Bacon</strong> ipsum dolor amet ham chuck short ribs, shank flank cupim frankfurter chicken. <a href=\"http://record.pt/\" target=\\\"_blank\\\" rel=\\\"noopener\\\"><strong>Sausage</strong> frankfurter</a> chicken <a href=\"http://record.pt/\" target=\\\"_blank\\\" rel=\\\"noopener\\\">ball tip, <em>drumstick</em></a><em> brisket</em> pork chop turkey. Andouille bacon ham hock, pastrami sausage pork chop <a href=\"http://record.pt/\" target=\\\"_blank\\\" rel=\\\"noopener\\\">corned beef frankfurter shank</a> chislic short ribs. <strong>Hamburger</strong> <em>bacon pork belly, drumstick pork chop capicola kielbasa pancetta buffalo pork. </em><em><strong>Meatball</strong></em><em> doner pancetta ham ribeye.</em> <strong>Picanha</strong> ham venison ribeye short loin beef, tail pig ball tip buffalo salami shoulder ground round chicken. <strong>Porchetta</strong> capicola drumstick, tongue fatback pork pork belly cow sirloin ham hock flank venison beef ribs.</p>"

        val listOfElements = HTMLParser().parse(html)

        val element = listOfElements.first() as TextViewElement

        textView.text = element.getStyledComponents(body, headerSize, app)
        compareScreenshot(textView)
    }

    @Test
    fun textElementVeryComplexList() {
        val textView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.view_element_text_from_html, null
            ) as ConstraintLayout
            ).findViewById(R.id.text_view) as AppCompatTextView

        val url = "https://www.meneame.net/"
        val html = "<ul>\n" +
            "   <li>This</li>\n" +
            "   <li>This and more text <a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\"><em><strong>lalalalalala</strong></em></a></li>\n" +
            "   <li>More text here <em><strong>lalalalalala</strong></em> and here </li>\n" +
            "   <li><strong>is</strong></li>\n" +
            "   <li>is <strong>is</strong> is</li>\n" +
            "   <li><em><strong>is</strong></em> with some <strong>text</strong> in the middle <em><strong>is</strong></em></li>\n" +
            "   <li><em>a</em></li>\n" +
            "   <li><a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\">link</a></li>\n" +
            "   <li>text with <a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\">link</a></li>\n" +
            "   <li>Hola <strong><em>que tal </em></strong> majete</li>\n" +
            "   <li><a href=$url target=\\\"_blank\\\" rel=\\\"noopener\\\"><em><strong>Meneane </strong></em></a><a href=$url target=\\\"_blank\\\" rel=\\\"noopener\\\">Another URL in this list</a> and some text</li>"
        "</ul>"

        val listOfElements = HTMLParser().parse(html)

        val element = listOfElements.first() as TextViewElement

        textView.text = element.getStyledComponents(body, headerSize, app)
        compareScreenshot(textView)
    }
 */
}
