package com.kickstarter.viewmodels

import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class BackingAddOnsFragmentViewModelTest: KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnsFragmentViewModel.ViewModel
    private val addOnsList = TestSubscriber.create<Pair<ProjectData, List<Reward>>>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = BackingAddOnsFragmentViewModel.ViewModel(environment)
        this.vm.outputs.addOnsList().subscribe(this.addOnsList)
    }

    @Test
    fun emptyAddOnsListForRWWithOutAddOns() {

        val environment = environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun getProjectAddOns(slug: String): Observable<List<Reward>> {
                        return Observable.empty()
                    }
                })
                .build()

        setUpEnvironment(environment)

        val rw = RewardFactory.rewardHasAddOns()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeReason)
        this.vm.arguments(bundle)

        this.addOnsList.assertNoValues()
    }

    @Test
    fun addOnsListForRWWithAddOns() {
        val addOn = RewardFactory.addOn()
        val listAddons = listOf(addOn, addOn, addOn)
        val environment = environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun getProjectAddOns(slug: String): Observable<List<Reward>> {
                        return Observable.just(listAddons)
                    }
                })
                .build()
        setUpEnvironment(environment)

        val rw = RewardFactory.rewardHasAddOns()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeReason)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Pair(projectData,listAddons))
    }
}