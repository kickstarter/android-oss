package com.kickstarter.ui.intents

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Test

class ProjectIntentMapperTest : KSRobolectricTestCase() {
    private val disposables = CompositeDisposable()

    @Test
    fun testProject_emitsFromProjectParamExtra() {
        val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")
        val resultTestV2 = io.reactivex.subscribers.TestSubscriber.create<Project>()

        attacheTestResultSubscriber(intent, resultTestV2)

        resultTestV2.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromProjectParamExtraApollo() {
        val intent = Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee")
        val resultTestV2 = io.reactivex.subscribers.TestSubscriber.create<Project>()

        attacheTestResultSubscriber(intent, resultTestV2)

        resultTestV2.assertValueCount(1)
    }

    @Test
    fun testProject_emitsTwiceFromProjectExtra_V2() {
        val project = ProjectFactory.project()
        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        val resultTest = io.reactivex.subscribers.TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApiClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)
        resultTest.assertValueCount(2)
    }

    @Test
    fun testProject_emitsFromKsrProjectUri() {
        val uri = Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val resultTestV2 = io.reactivex.subscribers.TestSubscriber.create<Project>()

        attacheTestResultSubscriber(intent, resultTestV2)

        resultTestV2.assertValueCount(1)
    }

    @Test
    fun testProject_emitsFromHttpsProjectUri_V2() {
        val uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val resultTest = io.reactivex.subscribers.TestSubscriber.create<Project>()
        ProjectIntentMapper.project(intent, MockApiClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)
        resultTest.assertValueCount(1)
    }

    @Test
    fun testRefTag_emitsFromRefTag() {
        val refTag = RefTag.from("test")
        val intent = Intent().putExtra(IntentKey.REF_TAG, refTag)
        val resultTest = io.reactivex.subscribers.TestSubscriber.create<KsOptional<RefTag?>>()
        ProjectIntentMapper.refTag(intent).subscribe { resultTest.onNext(it) }.addToDisposable(disposables)
        assertEquals(resultTest.values().get(0).getValue(), refTag)
    }

    @Test
    fun testRefTag_emitsNullWithNoRefTag() {
        val intent = Intent()
        val resultTest = io.reactivex.subscribers.TestSubscriber.create<KsOptional<RefTag?>>()
        ProjectIntentMapper.refTag(intent).subscribe { resultTest.onNext(it) }.addToDisposable(disposables)
        resultTest.assertValueCount(1)
        assertNull(resultTest.values().get(0).getValue())
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
        resultTestV2: io.reactivex.subscribers.TestSubscriber<Project>
    ) {

        ProjectIntentMapper.project(intent, MockApolloClientV2()).subscribe {
            resultTestV2.onNext(it)
        }.addToDisposable(disposables)
    }
}
