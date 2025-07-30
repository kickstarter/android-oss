package com.kickstarter.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ALLOW
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DENY
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
            builder.setTitle(R.string.Allow_app_to_track)
            builder.setMessage(R.string.We_use_personal_data_to_provide_a_good_experience_on_Kickstarter_and_to_help_connect_you_with_projects_you_ll_love)
                .setPositiveButton(R.string.Allow) { dialog, id ->
                    this.viewModel.inputs.userConsentPreference(true)
                    setFragmentResult(TAG, bundleOf("result" to ALLOW.contextName))
                }
                .setNegativeButton(R.string.Decline) { dialog, id ->
                    this.viewModel.inputs.userConsentPreference(false)
                    setFragmentResult(TAG, bundleOf("result" to DENY.contextName))
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "ConsentManagementDialogFragment"
    }
}
