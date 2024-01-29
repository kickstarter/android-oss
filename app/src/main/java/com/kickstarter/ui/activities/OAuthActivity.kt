package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.viewmodels.LoginViewModel

class OAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpConnectivityStatusCheck(lifecycle)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }

        // TODO: Will continue work on https://kickstarter.atlassian.net/browse/MBL-1167
    }
}
