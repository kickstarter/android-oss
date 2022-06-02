package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.BackingFactory.backing
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Backing
import org.junit.Test
import rx.Observable

class BackingViewModelTest : KSRobolectricTestCase() {
    private var vm: BackingViewModel.ViewModel? = null

    private fun setUpEnvironment(environment: Environment) {
        vm = BackingViewModel.ViewModel(environment)
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
