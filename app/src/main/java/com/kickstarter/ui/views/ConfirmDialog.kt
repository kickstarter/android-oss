package com.kickstarter.ui.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.kickstarter.R
import com.kickstarter.databinding.GenericDialogAlertBinding

class ConfirmDialog @JvmOverloads constructor(
    context: Context,
    val title: String?,
    val message: String,
    private val buttonText: String? = null
) : AppCompatDialog(context) {

    private lateinit var binding: GenericDialogAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = GenericDialogAlertBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (title != null) {
            setTitleText(title)
        } else {
            binding.titleTextView.visibility = View.GONE
        }

        if (buttonText != null) {
            setButtonText(buttonText)
        } else {
            setButtonText(context.getString(R.string.general_alert_buttons_ok))
        }

        setMessage(message)

        binding.okButton.setOnClickListener {
            okButtonClick()
        }
    }

    private fun setButtonText(buttonText: String) {
        binding.okButton.text = buttonText
    }

    /**
     * Set the title on the TextView with id title_text_view.
     * Note, default visibility is GONE since we may not always want a title.
     */
    private fun setTitleText(title: String) {
        binding.titleTextView.text = title
        binding.titleTextView.visibility = TextView.VISIBLE
        val params = binding.messageTextView.layoutParams as LinearLayout.LayoutParams
        params.topMargin = context.resources.getDimension(R.dimen.grid_1).toInt()
        binding.messageTextView.layoutParams = params
    }

    /**
     * Set the message on the TextView with id message_text_view.
     */
    private fun setMessage(message: String) {
        binding.messageTextView.text = message
    }

    /**
     * Dismiss the dialog on click ok_button".
     */

    private fun okButtonClick() {
        dismiss()
    }
}
