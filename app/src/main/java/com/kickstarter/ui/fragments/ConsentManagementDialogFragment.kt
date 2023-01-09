package com.kickstarter.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.kickstarter.R

class ConsentManagementDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.FPO_allow_app_to_track)
            builder.setMessage(R.string.FPO_Allow_Kickstarter_to_track_analytics_to_provide_a_more_personalized_experience)
                .setPositiveButton(R.string.FPO_allow) { dialog, id -> }
                .setNegativeButton(R.string.FPO_deny) { dialog, id -> }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}