package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.kickstarter.R
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPaymentSheetAppearance
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.Project
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.PlaygroundViewModel
import com.kickstarter.viewmodels.PlaygroundViewModel.Factory
import com.stripe.android.paymentsheet.CreateIntentResult
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch

class PlaygroundActivity : ComponentActivity() {
    private lateinit var binding: PlaygroundLayoutBinding
    private lateinit var view: View
    private lateinit var viewModelFactory: Factory
    private var stripePaymentMethod: String = ""
    private var savePayment = false
    val viewModel: PlaygroundViewModel by viewModels { viewModelFactory }

    private lateinit var paymentSheet: PaymentSheet

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaygroundLayoutBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env)
        }


        paymentSheet = PaymentSheet(
            activity = this,
            createIntentCallback = { paymentMethod, shouldSavePaymentMethod ->
                stripePaymentMethod = paymentMethod.id ?: ""
                savePayment = shouldSavePaymentMethod

                var payload = CompleteOrderPayload()

//                viewModel.completeOrder(stripePaymentMethod)
//                viewModel.payloadUIState.collect {
//                    payload = it
//                }

                //Timber.d("payment Information ${paymentMethod}")
                CreateIntentResult.Success(payload.clientSecret)
            },
            paymentResultCallback = ::onPaymentSheetResult,
        )

        this.binding.newMethodButton.setOnClickListener {
            presentPaymentSheet()
        }

        this.binding.pledgeButton.setOnClickListener {
            stripePaymentMethod.let {
                viewModel.completeOrder(it)
            }
        }

    }


    fun presentPaymentSheet() {
        val intentConfig = PaymentSheet.IntentConfiguration(
            mode = PaymentSheet.IntentConfiguration.Mode.Payment(
                amount = 1099,
                currency = "usd",
            ),
            onBehalfOf = "acct_1Ir6hZ4NJG33TWAg"
        )

        paymentSheet.presentWithIntentConfiguration(
            intentConfiguration = intentConfig,
            // Optional configuration - See the "Customize the sheet" section in this guide
            configuration = this.getPaymentSheetConfiguration("arkariang@gmail.com")
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
