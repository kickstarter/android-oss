package com.kickstarter.ui.intents

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.PushNotificationEnvelopeFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Test
import rx.observers.TestSubscriber

class ProjectIntentMapperTest : KSRobolectricTestCase() {
    private val disposables = CompositeDisposable()

    @Test
    fun testProject_creatorProjectIntent() {
        val resultTest = TestSubscriber.create<Project>()
        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()

        val intent = Intent().putExtra(IntentKey.PROJECT, creatorProject)
        ProjectIntentMapper.project(intent, MockApolloClient()).subscribe(resultTest)

        resultTest.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromProjectParamExtra() {
        val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")
        val resultTest = TestSubscriber.create<Project>()
        val resultTestV2 = io.reactivex.subscribers.TestSubscriber.create<Project>()

        attacheTestResultSubscriber(intent, resultTest, resultTestV2)

        resultTest.assertValueCount(1)
        resultTestV2.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromProjectParamExtraApollo() {
        val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")
        val resultTest = TestSubscriber.create<Project>()
        val resultTestV2 = io.reactivex.subscribers.TestSubscriber.create<Project>()

        attacheTestResultSubscriber(intent, resultTest, resultTestV2)

        resultTest.assertValueCount(1)
        resultTestV2.assertValueCount(1)
    }

    @Test
    fun testProject_emitsTwiceFromProjectExtra() {
        val project = ProjectFactory.project()
        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        val resultTest = TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApiClient())
            .subscribe(resultTest)
        resultTest.assertValueCount(2)
    }

    @Test
    fun testProject_emitsTwiceFromProjectExtraApollo() {
        val project = ProjectFactory.project()
        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        val resultTest = TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApolloClient())
            .subscribe(resultTest)
        resultTest.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromKsrProjectUri() {
        val uri = Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val resultTest = TestSubscriber.create<Project>()
        val resultTestV2 = io.reactivex.subscribers.TestSubscriber.create<Project>()

        attacheTestResultSubscriber(intent, resultTest, resultTestV2)

        resultTest.assertValueCount(1)
        resultTestV2.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromKsrProjectUriApollo() {
        val uri = Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val resultTest = TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApolloClient())
            .subscribe(resultTest)
        resultTest.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromHttpsProjectUri() {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val resultTest = TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApiClient())
            .subscribe(resultTest)
        resultTest.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromHttpsProjectUriApollo() {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val resultTest = TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApolloClient())
            .subscribe(resultTest)
        resultTest.assertValueCount(1)
    }

    @Test
    fun testRefTag_emitsFromRefTag() {
        val refTag = RefTag.from("test")
        val intent = Intent().putExtra(IntentKey.REF_TAG, refTag)
        val resultTest = TestSubscriber.create<RefTag>()
        ProjectIntentMapper.refTag(intent).subscribe(resultTest)
        resultTest.assertValue(refTag)
    }

    @Test
    fun testRefTag_emitsNullWithNoRefTag() {
        val intent = Intent()
        val resultTest = TestSubscriber.create<RefTag?>()
        ProjectIntentMapper.refTag(intent).subscribe(resultTest)
        resultTest.assertValue(null)
    }

    @Test
    fun testPushNotificationEnvelope_emitsFromEnvelope() {
        val envelope = PushNotificationEnvelopeFactory.envelope()
        val intent = Intent().putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope)
        val resultTest = TestSubscriber.create<PushNotificationEnvelope>()
        ProjectIntentMapper.pushNotificationEnvelope(intent).subscribe(resultTest)
        resultTest.assertValue(envelope)
    }

    @Test
    fun testPushNotificationEnvelope_doesNotEmitWithoutEnvelope() {
        val envelope = PushNotificationEnvelopeFactory.envelope()
        val intent = Intent()
        val resultTest = TestSubscriber.create<PushNotificationEnvelope>()
        ProjectIntentMapper.pushNotificationEnvelope(intent).subscribe(resultTest)
        resultTest.assertNoValues()
    }

    @Test
    fun testProjectHasSaveQueryFromUri() {
        assertTrue(ProjectIntentMapper.hasSaveQueryFromUri("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee?save=true".toUri()))
        assertFalse(ProjectIntentMapper.hasSaveQueryFromUri("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee".toUri()))
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun attacheTestResultSubscriber(
        intent: Intent,
        resultTest: TestSubscriber<Project>?,
        resultTestV2: io.reactivex.subscribers.TestSubscriber<Project>
    ) {
        ProjectIntentMapper.project(intent, MockApolloClient())
            .subscribe(resultTest)

        disposables.add(
            ProjectIntentMapper.project(intent, MockApolloClientV2()).subscribe {
                resultTestV2.onNext(it)
            }
        )
    }
}
