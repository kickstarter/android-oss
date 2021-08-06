package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.core.view.isGone
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.ActivityEditorialBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.viewmodels.EditorialViewModel

@RequiresActivityViewModel(EditorialViewModel.ViewModel::class)
class EditorialActivity : BaseActivity<EditorialViewModel.ViewModel>() {

    private lateinit var binding: ActivityEditorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorialBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.viewModel.outputs.description()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { binding.editorialDescription.setText(it) }

        this.viewModel.outputs.graphic()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { binding.editorialGraphic.setImageResource(it) }

        this.viewModel.outputs.title()
            .compose(observeForUI())
            .compose(bindToLifecycle())
            .subscribe { binding.editorialTitle.setText(it) }

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
            .subscribe {
                binding.editorialRetryContainer.root.isGone = it
            }

        RxView.clicks(binding.editorialRetryContainer.root)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.retryContainerClicked() }
    }

    private fun discoveryFragment(): DiscoveryFragment = supportFragmentManager.findFragmentById(R.id.fragment_discovery) as DiscoveryFragment

    // No-op because we have retry behavior
    override fun onNetworkConnectionChanged(isConnected: Boolean) {}
}
