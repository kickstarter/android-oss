package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.annotation.RequiresApi
import com.kickstarter.R
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.htmlparser.HTMLParser
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.getStyledComponents
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.PlaygroundViewModel
import com.stripe.android.model.PaymentMethod
import com.stripe.android.paymentsheet.CreateIntentCallback
import com.stripe.android.paymentsheet.CreateIntentResult
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

@RequiresActivityViewModel(PlaygroundViewModel.ViewModel::class)
class PlaygroundActivity : BaseActivity<PlaygroundViewModel.ViewModel?>() {
    private lateinit var binding: PlaygroundLayoutBinding
    private lateinit var view: View

    private lateinit var paymentSheet: PaymentSheet

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaygroundLayoutBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)


        paymentSheet = PaymentSheet(
            activity = this,
            createIntentCallback = { paymentMethod, shouldSavePaymentMethod ->
                // Make a request to your server to create a PaymentIntent and return its client secret
//                try {
//                    val response = myNetworkClient.createIntent(
//                        paymentMethodId = paymentMethod.id!!,
//                        shouldSavePaymentMethod = shouldSavePaymentMethod,
//                    )
//                    CreateIntentResult.Success(response.clientSecret)
//                } catch (e: Exception) {
//                    CreateIntentResult.Failure(
//                        cause = e,
//                        displayMessage = e.message
//                    )
//                }
                // - Will call endpoint here that will validate paymentMethod
                Timber.d("payment Information ${paymentMethod}")
                CreateIntentResult.Success("clientSecret")
            },
            paymentResultCallback = ::onPaymentSheetResult,
        )

        this.binding.payButton.setOnClickListener {
            presentPaymentSheet()
        }
    }

    fun presentPaymentSheet() {
        val intentConfig = PaymentSheet.IntentConfiguration(
            mode = PaymentSheet.IntentConfiguration.Mode.Payment(
                amount = 1099,
                currency = "usd",
            ),
        )

        paymentSheet.presentWithIntentConfiguration(
            intentConfiguration = intentConfig,
            // Optional configuration - See the "Customize the sheet" section in this guide
            configuration = PaymentSheet.Configuration(
                merchantDisplayName = "Kickstarter",
            )
        )
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                // Customer canceled - you should probably do nothing.
            }
            is PaymentSheetResult.Failed -> {
                print("Error: ${paymentSheetResult.error}")
                // PaymentSheet encountered an unrecoverable error. You can display the error to the user, log it, etc.
            }
            is PaymentSheetResult.Completed -> {
                // Display, for example, an order confirmation screen
                print("Completed")
            }
        }
    }

    /**
     * Set up the stepper example
     */
    private fun setStepper() {
        binding.stepper.inputs.setMinimum(1)
        binding.stepper.inputs.setMaximum(9)
        binding.stepper.inputs.setInitialValue(5)
        binding.stepper.inputs.setVariance(1)

        binding.stepper.outputs.display()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(binding.stepper, "The updated value on the display is: $it")
            }.dispose()
    }

    private fun setStartActivity() {
        binding.startActivity.setOnClickListener { startProjectActivity(Pair(ProjectFactory.project(), RefTag.searchFeatured())) }
    }

    private fun startProjectActivity(projectAndRefTag: Pair<Project, RefTag>) {
        val intent = Intent(this, PaginationActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
