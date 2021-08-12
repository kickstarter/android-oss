package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.CommentComposerStatus
import com.kickstarter.ui.views.CommentComposerView
import org.junit.Before
import org.junit.Test

class CommentComposerShotTest : ScreenshotTest {

    lateinit var commentComposerView: CommentComposerView
    lateinit var component: ApplicationComponent

    @Before
    fun setup() {
        // - Test Application
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()

        commentComposerView =
            (
                LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
                    .inflate(
                        R.layout.item_comment_composer, null
                    ) as ConstraintLayout
                )
                .findViewById(R.id.comment_composer_view)

        commentComposerView.setAvatarUrl(null) // -> internal network call to picasso we need to wrap Picasso into our own client to be able to mock on testing.
        commentComposerView.setActionButtonTitle("Post")
    }

    @Test
    fun commentComposerViewScreenshotTest_DISABLED() {
        commentComposerView.setCommentComposerStatus(CommentComposerStatus.DISABLED)
        compareScreenshot(commentComposerView)
    }

    @Test
    fun commentComposerViewScreenshotTest_ENABLED() {
        commentComposerView.setCommentComposerStatus(CommentComposerStatus.ENABLED)
        compareScreenshot(commentComposerView)
    }

    /*@Test
    fun commentComposerViewScreenshotTest_WithShortText() {
        commentComposerView.setCommentComposerStatus(CommentComposerStatus.ENABLED)
        commentComposerView.setCommentComposerText("new comment")
        compareScreenshot(commentComposerView)
    }

    @Test
    fun commentComposerViewScreenshotTest_WithMediumText() {
        commentComposerView.setCommentComposerStatus(CommentComposerStatus.ENABLED)
        commentComposerView.setCommentComposerText("Final FAQ’s:*There will be a Pledge Manager after the campaign where you can add additional items & pay for shipping, so if things are looking expensive now you can always break it up with partial pledge here & the rest in the Pledge Manager BUT every dollar here helps towards unlocking more Stretch Goals! Also yes, you can change your pledge levels in the PM afterwards and still get all the Kickstarter Exclusives!*There is an \"All-In\" for all of the actual game content (X-Men Core Game + All New Expansions). This is the Uncanny Pledge level. The only other extra items not included will be the Upgraded Locations, Upgraded Tokens, Upgraded Dashboards, Playmat and any of the original MU content (MU Core Game or Expansions).*The original Marvel United content is available in this Kickstarter again! All of the original Expansions can be bought either separately OR as part of the Ultimate Classic Bundle. The ONLY WAY to get the Stretch Goals from the original Marvel United is by purchasing the Ultimate Classic Bundle & the ONLY way to get the Kickstarter Exclusives for the Expansions is to purchase them here or in the Pledge Manager.*The original Core Box is ONLY available at retail stores. Due note that to get every Hero, purchase your Core Box anywhere but Wal-Mart (Wal-Mart Swapped out Wasp for Venom, who is already in the Stretch Goal Box for MU. The Stretch Goals Box is available ONLY in the Ultimate Classic Bundle on Kickstarter).Now let’s hit those social media platforms HARD and get us more backers!#BruteForce4MUBe Courteous! Be Kind! Be United!(If this helps even a single new backer or KS user, it was worth it!)")
        compareScreenshot(commentComposerView)
    }

    @Test
    fun commentComposerViewScreenshotTest_WithLongText() {
        commentComposerView.setCommentComposerStatus(CommentComposerStatus.ENABLED)
        commentComposerView.setCommentComposerText("Risks and challengesOutstanding customer service and creating quality products are at the core of our values, and we like being frank with our fans and supporters.Eye Mask is a new product and, like with all Kickstarter campaigns, you will be the first to receive it. When you see Eye Mask in a store, you can let your friends know you helped make this product possible.We may be a small business, but we are deeply committed to top-notch product development, and in every product we create, we pay attention to the details. For us, the quality of the product comes above everything else — and Eye Mask is no exception.Moving from prototyping to production can be challenging at times. We have done our best to minimize the possibility of bumps in the road by taking steps to advance manufacturing preparation. We have a ready-to-start manufacturing partner, with all suppliers lined up — but given the circumstances caused by Covid-19, unexpected situations may occur. In any case, our priority is to deliver a top-quality product to our backers, even if it may take a bit of patience. Some of our members have already participated in eight Kickstarter campaigns, and the experience we’ve amassed over the years is an advantage.Of course, some international customs, certification, logistical delays, and force majeure may occasionally come up. However, rest assured — we will be working full throttle to avoid any issues on our end.Learn about accountability on KickstarterEnvironmental commitmentsVisit our Environmental Resources Center to learn how Kickstarter encourages sustainable practices.Long-lasting designThe use of the best materials allows us not only to offer a comfortable experience but also a long-lasting relationship with Eye Mask. We have made it washable so that it looks like the first day for a lifetime. We would like you to use it again and again.early all great ideas follow a similar creative process and this article explains how this process works. Understanding this is important because creative thinking is one of the most useful skills you can possess. Nearly every problem you face in work and in life can benefit from innovative solutions, lateral thinking, and creative ideas.Anyone can learn to be creative by using these five steps. That's not to say being creative is easy. Uncovering your creative genius requires courage and tons of practice. However, this five-step approach should help demystify the creative process and illuminate the path to more innovative thinking.To explain how this process works, let me tell you a short story.A Problem in Need of a Creative SolutionIn the 1870s, newspapers and printers faced a very specific and very costly problem. Photography was a new and exciting medium at the time. Readers wanted to see more pictures, but nobody could figure out how to print images quickly and cheaply.For example, if a newspaper wanted to print an image in the 1870s, they had to commission an engraver to etch a copy of the photograph onto a steel plate by hand. These plates were used to press the image onto the page, but they often broke after just a few uses. This process of photoengraving, you can imagine, was remarkably time consuming and expensive.The man who invented a solution to this problem was named Frederic Eugene Ives. He went on to become a trailblazer in the field of photography and held over 70 patents by the end of his career. His story of creativity and innovation, which I will share now, is a useful case study for understanding the 5 key steps of the creative process.A Flash of InsightIves got his start as a printer’s apprentice in Ithaca, New York. After two years of learning the ins and outs of the printing process, he began managing the photographic laboratory at nearby Cornell University. He spent the rest of the decade experimenting with new photography techniques and learning about cameras, printers, and optics.In 1881, Ives had a flash of insight regarding a better printing technique.“While operating my photostereotype process in Ithaca, I studied the problem of halftone process,” Ives said. “I went to bed one night in a state of brain fog over the problem, and the instant I woke in the morning saw before me, apparently projected on the ceiling, the completely worked out process and equipment in operation.”Ives quickly translated his vision into reality and patented his printing approach in 1881. He spent the remainder of the decade improving upon it. By 1885, he had developed a simplified process that delivered even better results. The Ives Process, as it came to be known, reduced the cost of printing images by 15x and remained the standard printing technique for the next 80 years.Alright, now let's discuss what lessons we can learn from Ives about the creative process.The printing process developed by Frederic Eugene Ives is a great example of the optimal creative process. The printing process developed by Frederic Eugene Ives used a method called “halftone printing” to break a photograph down into a series of tiny dots. The image looks like a collection of dots up close, but when viewed from a normal distance the dots blend together to create a picture with varying shades of gray. (Source: Unknown.)The 5 Stages of the Creative ProcessIn 1940, an advertising executive named James Webb Young published a short guide titled, A Technique for Producing Ideas. In this guide, he made a simple, but profound statement about generating creative ideas.According to Young, innovative ideas happen when you develop new combinations of old elements. In other words, creative thinking is not about generating something new from a blank slate, but rather about taking what is already present and combining those bits and pieces in a way that has not been done previously.Most important, the ability to generate new combinations hinges upon your ability to see the relationships between concepts. If you can form a new link between two old ideas, you have done something creative.Young believed this process of creative connection always occurred in five steps.Gather new material. At first, you learn. During this stage you focus on 1) learning specific material directly related to your task and 2) learning general material by becoming fascinated with a wide range of concepts.Thoroughly work over the materials in your mind. During this stage, you examine what you have learned by looking at the facts from different angles and experimenting with fitting various ideas together.Step away from the problem. Next, you put the problem completely out of your mind and go do something else that excites you and energizes you.Let your idea return to you. At some point, but only after you have stopped thinking about it, your idea will come back to you with a flash of insight and renewed energy.Shape and develop your idea based on feedback. For any idea to succeed, you must release it out into the world, submit it to criticism, and adapt it as needed.creative-processThe Idea in PracticeThe creative process used by Frederic Eugene Ives offers a perfect example of these five steps in action.First, Ives gathered new material. He spent two years working as a printer's apprentice and then four years running the photographic laboratory at Cornell University. These experiences gave him a lot of material to draw upon and make associations between photography and printing.Second, Ives began to mentally work over everything he learned. By 1878, Ives was spending nearly all of his time experimenting with new techniques. He was constantly tinkering and experimenting with different ways of putting ideas together.Third, Ives stepped away from the problem. In this case, he went to sleep for a few hours before his flash of insight. Letting creative challenges sit for longer periods of time can work as well. Regardless of how long you step away, you need to do something that interests you and takes your mind off of the problem.Fourth, his idea returned to him. Ives awoke with the solution to his problem laid out before him. (On a personal note, I often find creative ideas hit me just as I am lying down for sleep. Once I give my brain permission to stop working for the day, the solution appears easily.)Finally, Ives continued to revise his idea for years. In fact, he improved so many aspects of the process he filed a second patent. This is a critical point and is often overlooked. It can be easy to fall in love with the initial version of your idea, but great ideas always evolve.The Creative Process in Short“An idea is a feat of association, and the height of it is a good metaphor.”—Robert FrostThe creative process is the act of making new connections between old ideas. Thus, we can say creative thinking is the task of recognizing relationships between concepts.One way to approach creative challenges is by following the five-step process of 1) gathering material, 2) intensely working over the material in your mind, 3) stepping away from the problem, 4) test")
        compareScreenshot(commentComposerView)
    }*/
}
