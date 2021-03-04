package com.kickstarter.ui.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.kickstarter.KSApplication
import com.kickstarter.databinding.AppRatingPromptBinding
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.qualifiers.AppRatingPreference
import com.kickstarter.libs.utils.ViewUtils
import javax.inject.Inject

class AppRatingDialog(context: Context) : AppCompatDialog(context) {
    @JvmField
    @Inject
    @AppRatingPreference
    var hasSeenAppRatingPreference: BooleanPreferenceType? = null

    private lateinit var binding: AppRatingPromptBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = AppRatingPromptBinding.inflate(layoutInflater)
        
        setContentView(binding.root)
        
        (context.applicationContext as KSApplication).component().inject(this)

        binding.noThanksButton.setOnClickListener {
            noThanksButtonClick()
        }
        
        binding.rateButton.setOnClickListener {
            rateButtonClick()
        }
        
        binding.remindButton.setOnClickListener {
            remindButtonClick()
        }
    }
    
    private fun rateButtonClick() {
        hasSeenAppRatingPreference?.set(true)
        dismiss()
        ViewUtils.openStoreRating(context, context.packageName)
    }

    private fun remindButtonClick() {
        dismiss()
    }

    private fun noThanksButtonClick() {
        hasSeenAppRatingPreference?.set(true)
        dismiss()
    }
}
