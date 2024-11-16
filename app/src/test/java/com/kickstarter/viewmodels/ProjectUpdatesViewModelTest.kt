package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.mock.factories.ProjectDataFactory.project
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UpdateFactory.update
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.apiresponses.UpdatesEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.ProjectUpdatesViewModel.Factory
import com.kickstarter.viewmodels.ProjectUpdatesViewModel.ProjectUpdatesViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProjectUpdatesViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectUpdatesViewModel

    private val horizontalProgressBarIsGone = TestSubscriber<Boolean>()
    private val isFetchingUpdates = TestSubscriber<Boolean>()
    private val projectAndUpdates = TestSubscriber<Pair<Project, List<Update>>>()
    private val startUpdateActivity = TestSubscriber<Pair<Project, Update>>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun setUpEnvironment(env: Environment, project: Project, projectData: ProjectData) {

        // Configure the view model with a project intent.
        val intent = Intent().putExtra(IntentKey.PROJECT, project.reduceProjectPayload())
            .putExtra(IntentKey.PROJECT_DATA, projectData)

        vm = Factory(env, intent).create(ProjectUpdatesViewModel::class.java)

        vm.outputs.isFetchingUpdates().subscribe { isFetchingUpdates.onNext(it) }.addToDisposable(disposables)
        vm.outputs.horizontalProgressBarIsGone().subscribe { horizontalProgressBarIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectAndUpdates().subscribe { projectAndUpdates.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startUpdateActivity().subscribe { startUpdateActivity.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun init_whenViewModelInstantiated_shouldTrackPageViewEvent() {
        val project = project()
        setUpEnvironment(environment(), project, project(project))

        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testHorizontalProgressBarIsGone() {
        val project = project()
        setUpEnvironment(environment(), project, project(project))

        horizontalProgressBarIsGone.assertValues(true)
    }

    @Test
    fun testIsFetchingUpdates() {
        val project = project()
        setUpEnvironment(environment(), project, project(project))

        isFetchingUpdates.assertValue(false)
    }

    @Test
    fun testProjectAndUpdates() {
        val updates = listOf(
            update(),
            update()
        )
        val project = project()

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun getProjectUpdates(
                    slug: String,
                    cursor: String,
                    limit: Int
                ): Observable<UpdatesGraphQlEnvelope> {
                    return Observable.just(
                        UpdatesGraphQlEnvelope
                            .builder()
                            .updates(updates)
                            .build()
                    )
                }
            }).build(),
            project, project(project)
        )

        val projectAndUpdates = BehaviorSubject.create<Pair<Project, List<Update>>>()
        vm.outputs.projectAndUpdates().subscribe(projectAndUpdates)

        assertEquals(project, projectAndUpdates.value?.first)
        assertEquals(updates[0], projectAndUpdates.value?.second?.get(0))
    }

    @Test
    fun test_projectAndUpdates_whenUpdatesListIsEmpty() {
        val project = project()

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun getProjectUpdates(
                    slug: String,
                    cursor: String,
                    limit: Int
                ): Observable<UpdatesGraphQlEnvelope> {
                    return Observable.just(
                        UpdatesGraphQlEnvelope
                            .builder()
                            .updates(emptyList())
                            .build()
                    )
                }
            }).build(),
            project, project(project)
        )

        val projectAndUpdates = BehaviorSubject.create<Pair<Project, List<Update>>>()
        vm.outputs.projectAndUpdates().subscribe(projectAndUpdates)

        assertEquals(project, projectAndUpdates.value?.first)
        assertTrue(projectAndUpdates.value?.second?.isEmpty() ?: false)

        this.vm.inputs.refresh()
        isFetchingUpdates.assertValues(false, true, false)
        horizontalProgressBarIsGone.assertValues(true, false, true)

        this.vm.inputs.nextPage()
        assertEquals(project, projectAndUpdates.value?.first)
        assertTrue(projectAndUpdates.value?.second?.isEmpty() ?: false)
    }

    @Test
    fun testStartUpdateActivity() {
        val update = update()
        val updates = listOf(update)
        val project = project()

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun getProjectUpdates(
                    slug: String,
                    cursor: String,
                    limit: Int
                ): Observable<UpdatesGraphQlEnvelope> {
                    return Observable.just(
                        UpdatesGraphQlEnvelope
                            .builder()
                            .updates(updates)
                            .build()
                    )
                }
            }).build(),
            project, project(project)
        )

        vm.inputs.updateClicked(update)
        startUpdateActivity.assertValues(Pair.create(project, update))
    }

    private fun urlsEnvelope(): UpdatesEnvelope.UrlsEnvelope {
        return UpdatesEnvelope.UrlsEnvelope
            .builder()
            .api(
                UpdatesEnvelope.UrlsEnvelope.ApiEnvelope
                    .builder()
                    .moreUpdates("http://more.updates.please")
                    .build()
            )
            .build()
    }
}
