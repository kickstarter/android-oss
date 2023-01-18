package com.kickstarter.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.viewmodels.ConsentManagementDialogFragmentViewModel

class ConsentManagementDialogFragment : DialogFragment() {

    private lateinit var viewModelFactory: ConsentManagementDialogFragmentViewModel.Factory
    private val viewModel: ConsentManagementDialogFragmentViewModel.ConsentManagementDialogFragmentViewModel by viewModels { viewModelFactory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ConsentManagementDialogFragmentViewModel.Factory(env)
        }

        return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle(R.string.FPO_allow_app_to_track)
                builder.setMessage(R.string.FPO_Allow_Kickstarter_to_track_analytics_to_provide_a_more_personalized_experience)
                    .setPositiveButton(R.string.FPO_allow) { dialog, id ->
                        this.viewModel.inputs.userConsentPreference(true)
                    }
                    .setNegativeButton(R.string.FPO_deny) { dialog, id ->
                        this.viewModel.inputs.userConsentPreference(false)
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
}
