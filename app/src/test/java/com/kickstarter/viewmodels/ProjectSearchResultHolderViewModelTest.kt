package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.PhotoFactory.photo
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.models.Project
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class ProjectSearchResultHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectSearchResultHolderViewModel.ViewModel
    private val notifyDelegateOfResultClick = TestSubscriber<Project>()
    private val percentFundedTextViewText = TestSubscriber<String>()
    private val projectForDeadlineCountdownUnitTextView = TestSubscriber<Project>()
    private val projectNameTextViewText = TestSubscriber<String>()
    private val projectPhotoUrl = TestSubscriber<String>()
    private val displayPrelaunchProjectBadge = TestSubscriber<Boolean>()

    private fun setUpEnvironment(environment: Environment) {
        vm = ProjectSearchResultHolderViewModel.ViewModel(environment)
        vm.outputs.notifyDelegateOfResultClick().subscribe(notifyDelegateOfResultClick)
        vm.outputs.percentFundedTextViewText().subscribe(percentFundedTextViewText)
        vm.outputs.projectForDeadlineCountdownUnitTextView().subscribe(
            projectForDeadlineCountdownUnitTextView,
        )
        vm.outputs.projectNameTextViewText().subscribe(projectNameTextViewText)
        vm.outputs.projectPhotoUrl().subscribe(projectPhotoUrl)
        vm.outputs.displayPrelaunchProjectBadge().subscribe(displayPrelaunchProjectBadge)
    }

    @Test
    fun testEmitsProjectImage() {
        val project = project()
            .toBuilder()
            .photo(
                photo()
                    .toBuilder()
                    .med("http://www.kickstarter.com/med.jpg")
                    .build(),
            )
            .build()
        setUpEnvironment(environment())
        vm.inputs.configureWith(Pair.create(project, false))

        projectPhotoUrl.assertValues("http://www.kickstarter.com/med.jpg")
    }

    @Test
    fun testEmitsFeaturedProjectImage() {
        val project = project()
            .toBuilder()
            .photo(
                photo()
                    .toBuilder()
                    .full("http://www.kickstarter.com/full.jpg")
                    .build(),
            )
            .build()
        setUpEnvironment(environment())
        vm.inputs.configureWith(Pair.create(project, true))

        projectPhotoUrl.assertValues("http://www.kickstarter.com/full.jpg")
    }

    @Test
    fun testEmitsProjectName() {
        val project = project()
        setUpEnvironment(environment())
        vm.inputs.configureWith(Pair.create(project, true))

        projectNameTextViewText.assertValues(project.name())
    }

    @Test
    fun testShowComingSoonLabelPrelauncProjects() {
        val project = ProjectFactory.prelaunchProject("")

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val env = environment().toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .build()

        setUpEnvironment(env)
        vm.inputs.configureWith(Pair.create(project, true))

        displayPrelaunchProjectBadge.assertValues(true)
    }

    @Test
    fun testShowComingSoonLabelPrelauncProjects_feature_flag_disabled() {
        val project = ProjectFactory.prelaunchProject("")

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return false
                }
            }

        val env = environment().toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .build()

        setUpEnvironment(env)
        vm.inputs.configureWith(Pair.create(project, true))

        displayPrelaunchProjectBadge.assertNoValues()
    }

    @Test
    fun testEmitsProjectStats() {
        val project = project()
            .toBuilder()
            .pledged(100.0)
            .goal(200.0)
            .deadline(DateTime().plusHours(24 * 10 + 1))
            .build()
        setUpEnvironment(environment())
        vm.inputs.configureWith(Pair.create(project, true))

        percentFundedTextViewText.assertValues(NumberUtils.flooredPercentage(project.percentageFunded()))
        projectForDeadlineCountdownUnitTextView.assertValues(project)
        displayPrelaunchProjectBadge.assertNoValues()
    }

    @Test
    fun testEmitsProjectClicked() {
        val project = project()
        setUpEnvironment(environment())
        vm.inputs.configureWith(Pair.create(project, true))
        vm.inputs.projectClicked()

        notifyDelegateOfResultClick.assertValues(project)
    }
}
