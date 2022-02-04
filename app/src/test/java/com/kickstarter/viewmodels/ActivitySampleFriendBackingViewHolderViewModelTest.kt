package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Activity
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class ActivitySampleFriendBackingViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ActivitySampleFriendBackingViewHolderViewModel.ViewModel

    private val bindActivity = TestSubscriber.create<Activity>()

    private fun setupEnvironment(@NonNull environment: Environment) {
        this.vm = ActivitySampleFriendBackingViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.bindActivity().subscribe(this.bindActivity)
    }

    @Test
    fun testBindActivityWithoutProjectAndUser() {
        setupEnvironment(environment())
        this.vm.inputs.configureWith(
            Activity.builder()
                .category(Activity.CATEGORY_BACKING)
                .createdAt(DateTime.now())
                .updatedAt(DateTime.now())
                .id(1).build()
        )
        this.bindActivity.assertNoValues()
    }

    @Test
    fun testBindActivityHasProjectAndUser() {
        setupEnvironment(environment())

        val activityWithProjectAndUser = Activity.builder()
            .category(Activity.CATEGORY_BACKING)
            .project(ProjectFactory.backedProject())
            .user(UserFactory.creator())
            .createdAt(DateTime.now())
            .updatedAt(DateTime.now())
            .id(1).build()

        this.vm.inputs.configureWith(activityWithProjectAndUser)

        this.bindActivity.assertValue(activityWithProjectAndUser)
    }
}
