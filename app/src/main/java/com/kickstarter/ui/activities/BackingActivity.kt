package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.BackingWrapper
import com.kickstarter.ui.data.ProjectData.Companion.builder
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.BackingFragment.BackingDelegate
import com.kickstarter.viewmodels.BackingViewModel.BackingViewModel
import com.kickstarter.viewmodels.BackingViewModel.Factory
import io.reactivex.disposables.CompositeDisposable

class BackingActivity : AppCompatActivity(), BackingDelegate {

    private val disposables = CompositeDisposable()

    private lateinit var viewModelFactory: Factory
    private val viewModel: BackingViewModel by viewModels {
        viewModelFactory
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.backing_layout)

        setUpConnectivityStatusCheck(lifecycle)

        this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent)
        }

        viewModel.outputs.showBackingFragment()
            .compose(Transformers.observeForUIV2())
            .subscribe { startBackingFragment(it) }
            .addToDisposable(disposables)

        viewModel.outputs.isRefreshing()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                backingFragment()?.isRefreshing(
                    it
                )
            }
            .addToDisposable(disposables)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    private fun startBackingFragment(backingWrapper: BackingWrapper) {
        val data = builder()
            .project(backingWrapper.project)
            .backing(backingWrapper.backing)
            .user(backingWrapper.user)
            .build()

        backingFragment()?.configureWith(data)

        backingFragment()?.let {
            supportFragmentManager.beginTransaction()
                .show(it)
                .commit()
        }
    }

    private fun backingFragment(): BackingFragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_backing) as BackingFragment?
    }

    override fun refreshProject() {
        viewModel.inputs.refresh()
    }

    override fun showFixPaymentMethod() {}
}
