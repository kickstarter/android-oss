package com.kickstarter.ui.activities

import android.app.Activity
import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import androidx.core.view.isGone
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.databinding.ActivityCampaignDetailsBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.viewmodels.CampaignDetailsViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CampaignDetailsViewModel.ViewModel::class)
class CampaignDetailsActivity : BaseActivity<CampaignDetailsViewModel.ViewModel>() {

    private lateinit var binding: ActivityCampaignDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampaignDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.viewModel.outputs.goBackToProject()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { goBackToProject() }

        this.viewModel.outputs.pledgeContainerIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe {
                binding.campaignDetailsPledgeContainer.isGone = !it
            }

        this.viewModel.outputs.url()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { binding.webView.loadUrl(it) }

        RxView.clicks(binding.campaignDetailsPledgeActionButton)
            .compose(bindToLifecycle())
            .subscribe { this.viewModel.inputs.pledgeActionButtonClicked() }
    }

    @NonNull
    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
    }

    private fun goBackToProject() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
