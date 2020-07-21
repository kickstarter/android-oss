package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel
import org.junit.Test
import rx.observers.TestSubscriber

class BackingAddOnViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnViewHolderViewModel.ViewModel

    private val backerLimitIsGone = TestSubscriber.create<Boolean>()

    private fun setupEnvironment(@NonNull environment: Environment ) {
        this.vm = BackingAddOnViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.backerLimitPillIsGone().subscribe(this.backerLimitIsGone)
    }

    @Test
    fun testAddOnBackerLimitGone(){
        setupEnvironment(environment())

        val addOn = RewardFactory.reward().toBuilder().isAddOn(true).limit(null).build()
        this.vm.inputs.configureWith(android.util.Pair<ProjectData, Reward>(ProjectDataFactory.project(ProjectFactory.project()), addOn))

        this.backerLimitIsGone.assertValue(true)

    }
}