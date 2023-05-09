package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.htmlparser.ViewElement
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.viewmodels.projectpage.ProjectCampaignViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProjectCampaignViewModelTests : KSRobolectricTestCase() {

    private lateinit var vm: ProjectCampaignViewModel.ProjectCampaignViewModel

    private val storyViewElementsList = TestSubscriber.create<List<ViewElement>>()
    private val onScrollToVideoPosition = TestSubscriber.create<Int>()
    private val onOpenVideoInFullScreen = TestSubscriber.create<Pair<String, Long>>()
    private val updateVideoCloseSeekPosition = TestSubscriber.create<Pair<Int, Long>>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectCampaignViewModel.ProjectCampaignViewModel(environment)

        disposables.add(this.vm.outputs.storyViewElements().subscribe { this.storyViewElementsList.onNext(it) })
        disposables.add(this.vm.outputs.onScrollToVideoPosition().subscribe { this.onScrollToVideoPosition.onNext(it) })
        disposables.add(this.vm.outputs.onOpenVideoInFullScreen().subscribe { this.onOpenVideoInFullScreen.onNext(it) })
        disposables.add(this.vm.outputs.updateVideoCloseSeekPosition().subscribe { this.updateVideoCloseSeekPosition.onNext(it) })
    }

    @Test
    fun fullStoryTest() {
        // Story for project https://staging.kickstarter.com/projects/gkdreamcatcher/george-kahn-jazz-and-blues-revue-our-first-studio
        val story = "<p><i>7/09/14 - update #5 We have had a little change in our rewards.  I just found out that due to copyright and use rules we are not allowed to send out copies of the Live at LACMA broadcast.  We cannot even give it away!  So everything else in the backer rewards holds true, but the best we can do for you is to come up with a way for you to listen to the LACMA radio broadcast.  We're working on that, but in the meanwhile we are already rehearsing for our studio session on July 22 and 23rd!  Can't wait!</i></p>" +
            "<p><i>7/08/14 - Update #4 </i>We are on the verge of hitting $19,000 now.  Thank you for believing in the <b>Jazz &amp; Blues Revue</b> - we know you will not be disappointed.\n\nWe are so thrilled that you believed in our project and we can't wait to share our music with you. To celebrate the completion of our Kickstarter campaign, we are performing LIVE in concert for one night only, this Thursday, July 10 in Studio City, CA.</p>" +
            "<p> There are two sets, 8:00 PM and 9:30 PM. Both sets will feature virtually all the players for the recording: Courtney Lemmon, Gina Saputo and Crystal Starr on vocals, George on piano, Dr. Bobby Rodriguez on trumpet, Lyman Medeiros on bass, M.B. Gordy on drums, and special guest Brent Canter on guitar.\n\n</p>" +
            "<p> The cover charge is only $15.00. Please call to reserve your seat - Vitello's is a small, intimate dinner room, and it may sell out!\n \n <b>Call for reservations : 818.769.0905 Vitellos is located at 4349 Tujunga Avenue, Studio City, CA 91604</b></p>" +
            "<p><i>7/07/14 - Update #3 Wow Wow Wow! we broke through our goal today, thanks to two fans that bought the House Concert Package. Here is a short video I took today at the studio where we are going to record our album!</i></p>" +
            "<div class=\"template oembed\" contenteditable=\"false\" data-href=\"http://youtu.be/x-pMbrukngc\">\n" +
            "<iframe width=\"356\" height=\"200\" src=\"https://www.youtube.com/embed/x-pMbrukngc?feature=oembed&amp;wmode=transparent\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>\n\n" +
            "</div>\n\n\n" +
            "<p><i>And here's a little look inside - you still have 3 days left to pledge at the $499 level, and get to join us in the studio as we record our Jazz &amp; Blues Revue album later this month!</i></p>" +
            "<div class=\"template oembed\" contenteditable=\"false\" data-href=\"http://youtu.be/H6PvAUHx7b0\">\n" +
            "<iframe width=\"356\" height=\"200\" src=\"https://www.youtube.com/embed/H6PvAUHx7b0?feature=oembed&amp;wmode=transparent\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>\n\n" +
            "</div>\n\n\n" +
            "<p><i>7/06/14 - Update #2 - $2100 away from our goal, it is time to start the party!  Please enjoy \"Yes We Can Can\" from the <b>LIVE AT LACMA </b>radio broadcast </i></p>" +
            "<div class=\"template asset\" contenteditable=\"false\" data-id=\"2236466\">\n" +
            "<figure>\n<audio controls=\"controls\" preload=\"none\">" +
            "<source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/002/236/466/f17de99e2a9e76a4954418c16d963f9b_mp3.mp3?2015\" type=\"audio/mp3\"></source>" +
            "<source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/002/236/466/f17de99e2a9e76a4954418c16d963f9b_aac.aac?2015\" type=\"audio/aac\"></source>" +
            "<source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/002/236/466/f17de99e2a9e76a4954418c16d963f9b_ogg.ogg?2015\" type=\"audio/ogg\"></source>" +
            "<source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/002/236/466/f17de99e2a9e76a4954418c16d963f9b_webm.webm?2015\" type=\"audio/webm\"></source>" +
            "</audio>\n</figure>\n\n" +
            "</div>\n\n\n" +
            "<p><i>6/26/14 - Update #1 - This is why we decided to Play Big!</i></p>" +
            "<p>Each time we perform people ask for our album, so we decided NOW'S THE TIME and we are ready to put out the debut album of the <b>George Kahn Jazz &amp; Blues Revue.</b> Now you can help us do it, and get some fabulous rewards as a thank you!</p>" +
            "<a href=\"http://jazandbluesrevue.com\" target=\"_blank\"><div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"The Jazz &amp; Blues Revue\" data-caption=\"The Jazz &amp; Blues Revue\" data-id=\"1996633\">\n<figure>\n<img alt=\"The Jazz &amp; Blues Revue\" class=\"fit\" src=\"https://ksr-qa-ugc.imgix.net/assets/001/996/633/8de19cd1f52cdd8bc007d3b6514b99ce_original.JPG?ixlib=rb-4.0.2&amp;w=700&amp;fit=max&amp;v=1399849878&amp;gif-q=50&amp;q=92&amp;s=d9cd5799086c2c1bff1e838339370ff6\">\n<figcaption class=\"px2\">The Jazz &amp; Blues Revue</figcaption>\n</figure>\n\n</div>\n\n\n<div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"Gina Saputo, Courtney Lemmon and Crystal Starr\" data-caption=\"Gina Saputo, Courtney Lemmon and Crystal Starr\" data-id=\"1996648\">\n<figure>\n<img alt=\"Gina Saputo, Courtney Lemmon and Crystal Starr\" class=\"fit\" src=\"https://ksr-qa-ugc.imgix.net/assets/001/996/648/80bf7b5e1733dcadbddf0977ef6657c2_original.JPG?ixlib=rb-4.0.2&amp;w=700&amp;fit=max&amp;v=1399850168&amp;gif-q=50&amp;q=92&amp;s=1d7532fd68957b7e39a2867d40cc8628\">\n<figcaption class=\"px2\">Gina Saputo, Courtney Lemmon and Crystal Starr</figcaption>\n</figure>\n\n</div>\n\n\n</a><h1 id=\"h:the-secret-sauce\" class=\"page-anchor\"><b>THE \"SECRET SAUCE\"</b></h1><p>Making a jazz record is like trying to catch lightning in a bottle - we have all the songs picked out, the charts are written, and we have played them live countless times - but the magic happens when we go into the studio and combine all our planning and preparation with spontaneous interaction and improvisation.  George Kahn has released 7 albums, and each of the singers has recorded on their own, but this project has the \"secret sauce\" - it is going to be more than the sum of its parts.  We want YOU to be part of the magic. </p><h1 id=\"h:whats-in-it-for-you\" class=\"page-anchor\"><b>WHAT'S IN IT FOR YOU?</b></h1><p>Now you have a chance to become part of the creation of this\nalbum by pledging money to make it happen.  In return, you get to choose a variety of\n\"rewards\". For a sample of what we do, listen to this version of  \"Cantaloupe Island\"  from the \"Live at LACMA\" radio broadcast:</p><div class=\"template asset\" contenteditable=\"false\" data-id=\"1996694\">\n<figure>\n<audio controls=\"controls\" preload=\"none\"><source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/001/996/694/91e0fa06cc6772afebd7c6459347f733_mp3.mp3?2015\" type=\"audio/mp3\"></source><source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/001/996/694/91e0fa06cc6772afebd7c6459347f733_aac.aac?2015\" type=\"audio/aac\"></source><source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/001/996/694/91e0fa06cc6772afebd7c6459347f733_ogg.ogg?2015\" type=\"audio/ogg\"></source><source src=\"https://dr0rfahizzuzj.cloudfront.net/assets/001/996/694/91e0fa06cc6772afebd7c6459347f733_webm.webm?2015\" type=\"audio/webm\"></source></audio>\n</figure>\n\n</div>\n\n\n<p>In addition, you can\nchoose to get all kinds of fun things, from an autographed copy of our new CD,\nto dinner with George, to a visit to the recording session, to having us come\nto YOUR house and do a Jazz &amp; Blues Revue show for you and your guests. </p>\n\n<h1 id=\"h:great-jazz-albums-do\" class=\"page-anchor\"><b>GREAT JAZZ ALBUMS DON'T GROW ON TREES</b></h1><div class=\"template asset\" contenteditable=\"false\" data-alt-text=\"\" data-caption=\"\" data-id=\"1996649\">\n<figure>\n<img alt=\"\" class=\"fit\" src=\"https://ksr-qa-ugc.imgix.net/assets/001/996/649/b95118bf93213e2568c80606c852b9ff_original.jpg?ixlib=rb-4.0.2&amp;w=700&amp;fit=max&amp;v=1399850224&amp;gif-q=50&amp;q=92&amp;s=a17da18b83f2211f7369f9c0cbc99382\">\n</figure>\n\n</div>\n\n\n<p>To release a top-quality album of 13 songs performed by the\nbest musicians in Los Angeles costs money!  Unlike  previous albums where we looked for investors to\nhelp complete the album, we have decided to use Kickstarter.  \"Where does all this money go\", you ask?  Here is a brief breakdown of some of the costs involved in the album project:</p><p><b>PROPOSED\nBUDGET – </b></p>\n\n<ul>\n<li>Musicians (8 of the best Los Angeles Studio cats!)</li>\n<li>Hard dives and digital backup of recording</li>\n<li>Studio\ntime-recording sessions. 4-5 days</li>\n<li>Mixing\nand mastering 3-4 days</li>\n<li>Photography,\nvideography, art direction</li>\n<li>CD\nproduction and pressing</li>\n<li>Licensing fees (all our songs are \"cover\" tunes, which require licensing)</li>\n<li>Domestic\nand international distribution.</li>\n<li>Kickstarter\nfee: 5% of funded project</li>\n<li>Amazon:\n5% of funded project (credit card fees)</li>\n<li>Conservatively, we feel that $18,000 will get it done. if we raise more, we can do a music video\nand some radio promotion!.  That would be awesome!  Rest assured, every penny will go into making this the best project possible, and bringing it to the most people.</li>\n</ul>"
        val project = ProjectFactory.project()
            .toBuilder()
            .story(story)
            .build()
        val projectData = ProjectDataFactory.project(project)

        setUpEnvironment(environment())
        this.vm.inputs.configureWith(projectData)

        this.storyViewElementsList.assertValueCount(1)
        disposables.add(
            this.vm.storyViewElements().subscribe {
                assertEquals(it.size, 25)
                assertTrue(it.filterIsInstance<AudioViewElement>().size == 2)
            }
        )
    }

    @Test
    fun videoOperations() {
        setUpEnvironment(environment())
        val seekPosition = 7L
        val index = 1
        val source = "source"

        this.vm.inputs.closeFullScreenVideo(seekPosition)
        this.vm.inputs.openVideoInFullScreen(index, source, seekPosition)

        this.updateVideoCloseSeekPosition.assertNoValues()
        this.onOpenVideoInFullScreen.assertValue(Pair(source, seekPosition))
        this.updateVideoCloseSeekPosition.assertNoValues()
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
