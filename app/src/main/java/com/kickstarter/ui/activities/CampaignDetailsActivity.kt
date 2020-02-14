package com.kickstarter.ui.activities

import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.viewmodels.CampaignDetailsViewModel
import kotlinx.android.synthetic.main.activity_campaign_details.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CampaignDetailsViewModel.ViewModel::class)
class CampaignDetailsActivity : BaseActivity<CampaignDetailsViewModel.ViewModel>(){

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_details)

        this.viewModel.outputs.url()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe { web_view.loadUrl(it) }
    }

    @NonNull
    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
    }
}
