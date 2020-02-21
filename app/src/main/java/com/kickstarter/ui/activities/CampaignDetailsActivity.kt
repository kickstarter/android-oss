package com.kickstarter.ui.activities

import android.app.Activity
import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.CampaignDetailsViewModel
import kotlinx.android.synthetic.main.activity_campaign_details.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CampaignDetailsViewModel.ViewModel::class)
class CampaignDetailsActivity : BaseActivity<CampaignDetailsViewModel.ViewModel>(){

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_details)

        this.viewModel.outputs.goBackToProject()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe { goBackToProject() }

        this.viewModel.outputs.pledgeContainerIsVisible()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(campaign_details_pledge_container, !it) }

        this.viewModel.outputs.url()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe { web_view.loadUrl(it) }

        RxView.clicks(campaign_details_pledge_action_button)
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
