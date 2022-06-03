package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.BackingWrapper
import com.kickstarter.ui.data.ProjectData.Companion.builder
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.BackingFragment.BackingDelegate
import com.kickstarter.viewmodels.BackingViewModel

@RequiresActivityViewModel(BackingViewModel.ViewModel::class)
class BackingActivity : BaseActivity<BackingViewModel.ViewModel>(), BackingDelegate {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.backing_layout)

        viewModel.outputs.showBackingFragment()
            .compose(bindToLifecycle())
            .subscribe { startBackingFragment(it) }

        viewModel.outputs.isRefreshing()
            .compose(bindToLifecycle())
            .subscribe {
                backingFragment()?.isRefreshing(
                    it
                )
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
