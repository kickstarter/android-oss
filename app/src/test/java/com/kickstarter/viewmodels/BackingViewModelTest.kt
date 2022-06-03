package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.BackingFactory.backing
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Backing
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class BackingViewModelTest : KSRobolectricTestCase() {
    private var vm: BackingViewModel.ViewModel? = null
    private val isRefreshing = TestSubscriber.create<Boolean>()

    private fun setUpEnvironment(environment: Environment) {
        vm = BackingViewModel.ViewModel(environment)
        vm?.outputs?.isRefreshing()?.subscribe(this.isRefreshing)
    }

    @Test
    fun testShowBackingFragmentFacing() {
        val creatorUser = user()
            .toBuilder()
            .name("Kawhi Leonard")
            .build()

        val backerUser = user()
            .toBuilder()
            .name("random backer")
            .build()

        val backing = backing(backerUser)

        setUpEnvironment(
            envWithBacking(backing)
                .toBuilder()
                .currentUser(MockCurrentUser(creatorUser))
                .build()
        )
        vm?.outputs?.showBackingFragment()?.subscribe {
            assertNotNull(it)
            assertEquals(backing, it)
        }
    }

    @Test
    fun testIsRefreshing() {
        val creatorUser = user()
            .toBuilder()
            .name("Kawhi Leonard")
            .build()

        val backerUser = user()
            .toBuilder()
            .name("random backer")
            .build()

        val backing = backing(backerUser)

        val intent = Intent().apply {
            putExtra(IntentKey.BACKING, backing)
            putExtra(IntentKey.PROJECT, ProjectFactory.backedProject())
        }

        val vm = BackingViewModel.ViewModel(
            envWithBacking(backing)
                .toBuilder()
                .currentUser(MockCurrentUser(creatorUser))
                .build()
        )
            .also {
                it.intent(intent)
            }

        vm.outputs.isRefreshing().subscribe(this.isRefreshing)

        vm.inputs.refresh()

        this.isRefreshing.assertValue(false)
    }

    /**
     * Returns an environment with a backing and logged in user.
     */
    private fun envWithBacking(backing: Backing): Environment {
        return environment().toBuilder()
            .apolloClient(
                object : MockApolloClient() {
                    override fun getBacking(backingId: String): Observable<Backing> {
                        return Observable.just(backing)
                    }
                }
            )
            .currentUser(MockCurrentUser(user()))
            .build()
    }
}
