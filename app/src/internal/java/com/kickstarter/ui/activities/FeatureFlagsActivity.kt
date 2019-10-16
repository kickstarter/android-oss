package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.FeatureFlagsViewModel

@RequiresActivityViewModel(FeatureFlagsViewModel.ViewModel::class)
class FeatureFlagsActivity : BaseActivity<FeatureFlagsViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flag)


    }
}
