package com.kickstarter.ui.activities

import android.os.Bundle
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.viewmodels.EditorialViewModel
import kotlinx.android.synthetic.main.activity_editorial.*

@RequiresActivityViewModel(EditorialViewModel.ViewModel::class)
class EditorialActivity : BaseActivity<EditorialViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editorial)

        this.viewModel.outputs.description()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { editorial_description.setText(it) }

        this.viewModel.outputs.graphic()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { editorial_graphic.setImageResource(it) }

        this.viewModel.outputs.title()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { editorial_title.setText(it) }

        this.viewModel.outputs.discoveryParams()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { discoveryFragment().updateParams(it) }

        this.viewModel.outputs.rootCategories()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { discoveryFragment().takeCategories(it) }

        this.viewModel.outputs.refreshDiscoveryFragment()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { discoveryFragment().refresh() }

        this.viewModel.outputs.retryContainerIsGone()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { ViewUtils.setGone(editorial_retry_container, it) }

        RxView.clicks(editorial_retry_container)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.retryContainerClicked() }
    }

    private fun discoveryFragment(): DiscoveryFragment = supportFragmentManager.findFragmentById(R.id.fragment_discovery) as DiscoveryFragment

    // No-op because we have retry behavior
    override fun onNetworkConnectionChanged(isConnected: Boolean) {}
}
