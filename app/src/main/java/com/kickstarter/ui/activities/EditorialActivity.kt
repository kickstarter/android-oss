package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityEditorialBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.EditorialViewModel
import io.reactivex.disposables.CompositeDisposable

class EditorialActivity : AppCompatActivity() {

    private lateinit var factory: EditorialViewModel.Factory
    private val viewModel: EditorialViewModel.EditorialViewModel by viewModels { factory }
    private val disposables = CompositeDisposable()

    private lateinit var binding: ActivityEditorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorialBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        getEnvironment()?.let { env ->
            intent?.let {
                factory = EditorialViewModel.Factory(env, it)
            }
        }

        setUpConnectivityStatusCheck(lifecycle)
        this.onBackPressedDispatcher.addCallback { finishWithAnimation() }

        setContentView(binding.root)

        this.viewModel.outputs.description()
            .compose(observeForUIV2())
            .subscribe { binding.editorialDescription.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.graphic()
            .compose(observeForUIV2())
            .subscribe { binding.editorialGraphic.setImageResource(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.title()
            .compose(observeForUIV2())
            .subscribe { binding.editorialTitle.setText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.discoveryParams()
            .compose(observeForUIV2())
            .subscribe { discoveryFragment().updateParams(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.rootCategories()
            .compose(observeForUIV2())
            .subscribe { discoveryFragment().takeCategories(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.refreshDiscoveryFragment()
            .compose(observeForUIV2())
            .subscribe { discoveryFragment().refresh() }
            .addToDisposable(disposables)

        this.viewModel.outputs.retryContainerIsGone()
            .compose(observeForUIV2())
            .subscribe {
                binding.editorialRetryContainer.root.isGone = it
            }
            .addToDisposable(disposables)

        binding.editorialRetryContainer.root.setOnClickListener {
            this.viewModel.inputs.retryContainerClicked()
        }
    }

    private fun discoveryFragment(): DiscoveryFragment = supportFragmentManager.findFragmentById(R.id.fragment_discovery) as DiscoveryFragment

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
