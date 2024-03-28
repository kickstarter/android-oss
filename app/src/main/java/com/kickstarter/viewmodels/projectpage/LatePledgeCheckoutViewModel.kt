package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.confirmPaymentIntent
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class LatePledgeCheckoutUIState(
    val storeCards: List<StoredCard> = listOf(),
    val userEmail: String = ""
)

class LatePledgeCheckoutViewModel(val environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private var storedCards: List<StoredCard> = listOf()
    private var userEmail: String = ""
    private var checkoutId: String? = null

    private var stripe: Stripe = requireNotNull(environment.stripe())

    private var clientSecretForNewCard: String = ""
    private var newStoredCard: StoredCard = StoredCard.builder().build()
    private var errorAction: (message: String?) -> Unit = {}

    private var mutableLatePledgeCheckoutUIState = MutableStateFlow(LatePledgeCheckoutUIState())
    val latePledgeCheckoutUIState: StateFlow<LatePledgeCheckoutUIState>
        get() = mutableLatePledgeCheckoutUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = LatePledgeCheckoutUIState()
            )

    private var mutableClientSecretForNewPaymentMethod = MutableSharedFlow<String>()
    val clientSecretForNewPaymentMethod: SharedFlow<String>
        get() = mutableClientSecretForNewPaymentMethod.asSharedFlow()

    private var mutablePaymentRequiresAction = MutableSharedFlow<String>()
    val paymentRequiresAction: SharedFlow<String>
        get() = mutablePaymentRequiresAction.asSharedFlow()

    init {
        viewModelScope.launch {
            environment.currentUserV2()?.observable()?.asFlow()?.map {
                if (it.isPresent()) {
                    apolloClient.userPrivacy().asFlow().map { userPrivacy ->
                        userEmail = userPrivacy.email
                        emitCurrentState()
                    }.catch {
                        errorAction.invoke(null)
                    }.collect()

                    apolloClient.getStoredCards().asFlow().map { cards ->
                        storedCards = cards
                        emitCurrentState()
                    }.catch {
                        errorAction.invoke(null)
                    }.collect()
                }
            }?.catch {
                errorAction.invoke(null)
            }?.collect()
        }
    }

    fun provideCheckoutId(checkoutId: Long) {
        this.checkoutId = checkoutId.toString()
    }

    fun onAddNewCardClicked(project: Project, totalAmount: Double) {
        viewModelScope.launch {
            apolloClient.createPaymentIntent(
                CreatePaymentIntentInput(
                    project = project,
                    amount = totalAmount.toString()
                )
            ).asFlow().map { clientSecret ->
                clientSecretForNewCard = clientSecret
                mutableClientSecretForNewPaymentMethod.emit(clientSecretForNewCard)
            }.catch {
                errorAction.invoke(null)
            }.collect()
        }
    }

    fun onNewCardSuccessfullyAdded(storedCard: StoredCard) {
        newStoredCard = storedCard
        var mutableStoredCardList = storedCards.toMutableList()
        mutableStoredCardList.add(0, storedCard)
        storedCards = mutableStoredCardList.toList()
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    fun onPledgeButtonClicked(selectedCard: StoredCard?, project: Project, totalAmount: Double) {
        if (selectedCard == newStoredCard) {
            stripe.retrievePaymentIntent(
                clientSecret = clientSecretForNewCard,
                callback = object : ApiResultCallback<PaymentIntent> {
                    override fun onError(e: Exception) {
                        errorAction.invoke(null)
                    }

                    override fun onSuccess(result: PaymentIntent) {
                        result.paymentMethodId?.let { cardId ->
                            val cardWithId = selectedCard.toBuilder().stripeCardId(cardId).build()
                            newStoredCard = cardWithId
                            createPaymentIntentForCheckout(cardWithId, project, totalAmount)
                        } ?: run {
                            errorAction.invoke(null)
                        }
                    }
                }
            )
        } else {
            createPaymentIntentForCheckout(selectedCard, project, totalAmount)
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    private fun createPaymentIntentForCheckout(
        selectedCard: StoredCard?,
        project: Project,
        totalAmount: Double
    ) {
        viewModelScope.launch {
            apolloClient.createPaymentIntent(
                CreatePaymentIntentInput(
                    project = project,
                    amount = totalAmount.toString()
                )
            ).asFlow().map { clientSecret ->
                selectedCard?.let {
                    checkoutId?.let {
                        validateCheckout(clientSecret = clientSecret, selectedCard = selectedCard)
                    } ?: run {
                        errorAction.invoke(null)
                    }
                } ?: run {
                    errorAction.invoke(null)
                }
            }.catch {
                errorAction.invoke(null)
            }.collect()
        }
    }

    private suspend fun validateCheckout(clientSecret: String, selectedCard: StoredCard) {
        apolloClient.validateCheckout(
            checkoutId = checkoutId ?: "",
            paymentIntentClientSecret = clientSecret,
            paymentSourceId = selectedCard.stripeCardId() ?: ""
        ).asFlow().map { validation ->
            if (validation.isValid) {
                // Validation success, proceed with stripe
                stripeConfirmPaymentIntent(clientSecret = clientSecret, selectedCard = selectedCard)
            } else {
                // User validation.messages for displaying an error
                if (validation.messages.isNotEmpty()) {
                    errorAction.invoke(validation.messages.first())
                } else {
                    errorAction.invoke(null)
                }
            }
        }.catch {
            errorAction.invoke(null)
        }.collect()
    }

    private suspend fun stripeConfirmPaymentIntent(clientSecret: String, selectedCard: StoredCard) {
        val intentParams = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            clientSecret = clientSecret,
            paymentMethodId = selectedCard.stripeCardId() ?: ""
        )
        val withSDK =
            intentParams.withShouldUseStripeSdk(shouldUseStripeSdk = true)
        val paymentIntent = stripe.confirmPaymentIntent(withSDK)
        if (paymentIntent.lastPaymentError.isNotNull()) {
            // Display error with lastPaymentError.message
            errorAction.invoke(paymentIntent.lastPaymentError?.message)
        } else {
            // Success, move on
            completeOnSessionCheckout(clientSecret = clientSecret, selectedCard = selectedCard)
        }
    }

    private suspend fun completeOnSessionCheckout(clientSecret: String, selectedCard: StoredCard) {
        apolloClient.completeOnSessionCheckout(
            checkoutId = checkoutId ?: "",
            paymentIntentClientSecret = clientSecret,
            paymentSourceId = if(selectedCard == newStoredCard) null else selectedCard.id() ?: ""
        ).asFlow().map { iDRequiresActionPair ->
            if (iDRequiresActionPair.second) {
                mutablePaymentRequiresAction.emit(clientSecret)
            } else {
                // Go to Thanks Page, full complete flow
            }
        }.catch {
            errorAction.invoke(null)
        }.collect()
    }

    private suspend fun emitCurrentState() {
        mutableLatePledgeCheckoutUIState.emit(
            LatePledgeCheckoutUIState(
                storeCards = storedCards,
                userEmail = userEmail,
            )
        )
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LatePledgeCheckoutViewModel(environment) as T
        }
    }
}
