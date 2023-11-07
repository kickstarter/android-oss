package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.BackingFactory.backing
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.BackingViewModel.BackingViewModel
import com.kickstarter.viewmodels.BackingViewModel.Factory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class BackingViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingViewModel
    private val isRefreshing = TestSubscriber.create<Boolean>()
    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }

    private fun setUpEnvironment(environment: Environment, intent: Intent) {
        vm = Factory(environment, intent)
            .create(BackingViewModel::class.java)

        vm.outputs.isRefreshing().subscribe { this.isRefreshing.onNext(it) }
            .addToDisposable(disposables)
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

        val project = ProjectFactory.backedProject().toBuilder().isBacking(true).backing(backing).build()

        val env = environment().toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override fun getBacking(backingId: String): Observable<Backing> {
                        return Observable.just(backing)
                    }
                }
            )
            .currentUserV2(MockCurrentUserV2(creatorUser))
            .build()

        val intent = Intent().apply {
            putExtra(IntentKey.BACKING, backing)
            putExtra(IntentKey.PROJECT, project)
        }
        setUpEnvironment(env, intent = intent)

        vm.outputs.showBackingFragment().subscribe {
            assertNotNull(it)
            assertEquals(backing, it)
        }
            .addToDisposable(disposables)
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

        val env = environment().toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override fun getBacking(backingId: String): Observable<Backing> {
                        return Observable.just(backing)
                    }
                }
            )
            .currentUserV2(MockCurrentUserV2(creatorUser))
            .build()

        setUpEnvironment(env, intent)

        vm.inputs.refresh()

        this.isRefreshing.assertValue(false)
    }
}
