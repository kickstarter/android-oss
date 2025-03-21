package com.kickstarter.viewmodels.projectpage

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.locationId
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.stripe.android.Stripe
import com.stripe.android.confirmPaymentIntent
import com.stripe.android.model.ConfirmPaymentIntentParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class LatePledgeCheckoutUIState(
    val storeCards: List<StoredCard> = listOf(),
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val selectedRewards: List<Reward> = emptyList(),
    val shippingAmount: Double = 0.0,
    val checkoutTotal: Double = 0.0,
    val isPledgeButtonEnabled: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class LatePledgeCheckoutViewModel(val environment: Environment) : ViewModel() {

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val analytics = requireNotNull(environment.analytics())

    private var storedCards: List<StoredCard> = listOf()
    private var userEmail: String = ""
    private var checkoutId: String? = null
    private var backing: Backing? = null
    private val selectedRewards = mutableListOf<Reward>()
    private var buttonEnabled = true

    private var stripe: Stripe = requireNotNull(environment.stripe())

    private var clientSecretForNewCard: String = ""
    private var newStoredCard: StoredCard? = null
    private var errorAction: (message: String?) -> Unit = {}

    private var clientSecretFor3DSVerification: String = ""
    private var selectedCardFor3DSVerification: StoredCard? = null

    private var selectedReward: Reward? = null
    private var mutableLatePledgeCheckoutUIState = MutableStateFlow(LatePledgeCheckoutUIState())

    private var paymentIntent: String? = null
    private var pledgeButtonClickedJob: Job? = null

    private var scope = viewModelScope
    private var dispatcher = Dispatchers.IO

    private var mutableOnPledgeSuccessAction = MutableSharedFlow<Boolean>()
    val onPledgeSuccess: SharedFlow<Boolean>
        get() = mutableOnPledgeSuccessAction
            .asSharedFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = false
            )

    val latePledgeCheckoutUIState: StateFlow<LatePledgeCheckoutUIState>
        get() = mutableLatePledgeCheckoutUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = LatePledgeCheckoutUIState(isLoading = false)
            )

    private val mutableCheckoutPayment =
        MutableStateFlow(CheckoutPayment(id = 0L, paymentUrl = null, backing = null))
    val checkoutPayment: StateFlow<CheckoutPayment>
        get() = mutableCheckoutPayment
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = CheckoutPayment(id = 0L, paymentUrl = null, null)
            )

    private var mutableClientSecretForNewPaymentMethod = MutableSharedFlow<String>()
    val clientSecretForNewPaymentMethod: SharedFlow<String>
        get() = mutableClientSecretForNewPaymentMethod.asSharedFlow()

    private var mutablePaymentRequiresAction = MutableSharedFlow<String>()
    val paymentRequiresAction: SharedFlow<String>
        get() = mutablePaymentRequiresAction.asSharedFlow()

    private suspend fun loadUserInfo() =
        try {
            coroutineScope {
                emitCurrentState(isLoading = true)

                @OptIn(FlowPreview::class)
                val user = environment.currentUserV2()?.observable()?.asFlow()?.first()
                user?.getValue() ?: return@coroutineScope

                // `userPrivacy()` should be migrated to a suspending function
                val privacy = async { apolloClient.userPrivacy().asFlow().first() }
                val cards = async { apolloClient.getStoredCards().asFlow().first() }

                // TODO: change to Pair?
                val privacyAndCards = awaitAll(privacy, cards)

                userEmail = (privacyAndCards[0] as UserPrivacy).email
                @Suppress("UNCHECKED_CAST")
                storedCards = privacyAndCards[1] as List<StoredCard>
                emitCurrentState()
            }
        } catch (e: Exception) {
            // log `e`
            errorAction.invoke(null)
            emitCurrentState()
        }

    fun getCheckoutData() = checkoutData
    fun getPledgeData() = pledgeData

    @SuppressLint("LogNotTimber")
    fun provideCheckoutIdAndBacking(checkoutId: Long, backing: Backing) {
        this.checkoutId = checkoutId.toString()
        this.backing = backing
    }

    fun onAddNewCardClicked(project: Project) {
        viewModelScope.launch(dispatcher) {
            apolloClient.createSetupIntent(
                project = project,
            ).asFlow().onStart {
                emitCurrentState(isLoading = true)
            }.map { clientSecret ->
                buttonEnabled = true
                clientSecretForNewCard = clientSecret
                mutableClientSecretForNewPaymentMethod.emit(clientSecretForNewCard)
            }.onCompletion {
                emitCurrentState()
            }.catch {
                errorAction.invoke(null)
            }.collect()
        }
    }

    fun onNewCardSuccessfullyAdded() {
        viewModelScope.launch(dispatcher) {
            apolloClient.savePaymentMethod(
                SavePaymentMethodData(
                    reusable = true,
                    intentClientSecret = clientSecretForNewCard
                )
            ).asFlow().onStart {
                emitCurrentState(isLoading = true)
            }.map {
                refreshUserCards()
            }.catch {
                errorAction.invoke(null)
            }.onCompletion {
                emitCurrentState()
            }
                .collect()
        }
    }

    fun onNewCardFailed() {
        viewModelScope.launch {
            emitCurrentState()
        }
    }

    private suspend fun refreshUserCards() {
        apolloClient.getStoredCards()
            .asFlow().onStart {
                emitCurrentState(isLoading = true)
            }.map { cards ->
                storedCards = cards
            }.onCompletion {
                emitCurrentState()
            }.catch {
                errorAction.invoke(null)
            }.collect()
    }

    @SuppressLint("LogNotTimber")
    fun onPledgeButtonClicked(selectedCard: StoredCard?) {

        // - Avoid launching any other coroutine call while previous coroutine active
        if (pledgeButtonClickedJob?.isActive.isTrue()) return

        this.pledgeData?.let {
            pledgeButtonClickedJob = viewModelScope.launch(dispatcher) {
                val project = it.projectData().project()
                buttonEnabled = false
                emitCurrentState(isLoading = true)

                if (paymentIntent == null) {
                    createPaymentIntentForCheckout(selectedCard, project, it.checkoutTotalAmount())
                } else {
                    paymentIntent?.let { pi ->
                        selectedCard?.let { card ->
                            validateCheckout(
                                clientSecret = pi,
                                selectedCard = card
                            )
                        }
                    }
                }
            }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    private suspend fun createPaymentIntentForCheckout(
        selectedCard: StoredCard?,
        project: Project,
        totalAmount: Double
    ) {
        if (paymentIntent.isNotNull()) return
        checkoutId?.let { cId ->
            backing?.let { b ->
                apolloClient.createPaymentIntent(
                    CreatePaymentIntentInput(
                        project = project,
                        amount = totalAmount.toString(),
                        checkoutId = cId,
                        backing = b
                    )
                )
                    .asFlow()
                    .map { clientSecret ->
                        paymentIntent = clientSecret
                        selectedCard?.let {
                            checkoutId?.let {
                                validateCheckout(
                                    clientSecret = clientSecret,
                                    selectedCard = selectedCard
                                )
                            } ?: run {
                                emitCurrentState()
                                errorAction.invoke(null)
                            }
                        } ?: run {
                            emitCurrentState()
                            errorAction.invoke(null)
                        }
                    }.catch {
                        // - Clean up states if error happens when creating payment intent
                        paymentIntent = null
                        errorAction.invoke(null)
                    }.collect() // - No completation block, will continue on validateCheckout
            }
        } ?: run {
            buttonEnabled = true
            paymentIntent = null
            emitCurrentState()
            errorAction.invoke(null)
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
                val error = if (validation.messages.isNotEmpty()) {
                    validation.messages.first()
                } else {
                    null
                }
                emitCurrentState()
                errorAction.invoke(error)
            }
        }.catch {
            errorAction.invoke(null)
        }
            .onCompletion {
                emitCurrentState()
            }
            .collect()
    }

    private suspend fun stripeConfirmPaymentIntent(clientSecret: String, selectedCard: StoredCard) {
        val intentParams = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            clientSecret = clientSecret,
            paymentMethodId = selectedCard.stripeCardId() ?: ""
        )
        val withSDK =
            intentParams.withShouldUseStripeSdk(shouldUseStripeSdk = true)
        val paymentIntent = stripe.confirmPaymentIntent(withSDK)
        if (paymentIntent.requiresAction()) {
            clientSecretFor3DSVerification = clientSecret
            selectedCardFor3DSVerification = selectedCard
            mutablePaymentRequiresAction.emit(clientSecret)
            emitCurrentState()
        } else if (paymentIntent.lastPaymentError.isNotNull()) {
            // Display error with lastPaymentError.message
            emitCurrentState()
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
            paymentSourceId = if (selectedCard == newStoredCard) null else selectedCard.id() ?: "",
            paymentSourceReusable = true
        ).asFlow().map { iDRequiresActionPair ->
            if (iDRequiresActionPair.second) {
                mutablePaymentRequiresAction.emit(clientSecret)
            } else {
                mutableOnPledgeSuccessAction.emit(true)
            }
        }.onCompletion {
            emitCurrentState()
        }.catch {
            errorAction.invoke(null)
        }.collect()
    }

    fun completeOnSessionCheckoutFor3DS() {
        scope.launch(dispatcher) {
            if (clientSecretFor3DSVerification.isNotEmpty() && selectedCardFor3DSVerification.isNotNull()) {
                apolloClient.completeOnSessionCheckout(
                    checkoutId = checkoutId ?: "",
                    paymentIntentClientSecret = clientSecretFor3DSVerification,
                    paymentSourceId = if (selectedCardFor3DSVerification == newStoredCard) null else selectedCardFor3DSVerification?.id() ?: "",
                    paymentSourceReusable = true
                ).asFlow()
                    .onStart {
                        emitCurrentState(isLoading = true)
                    }
                    .map { iDRequiresActionPair ->
                        if (iDRequiresActionPair.second) {
                            mutablePaymentRequiresAction.emit(clientSecretFor3DSVerification)
                        } else {
                            mutableOnPledgeSuccessAction.emit(true)
                        }
                    }.onCompletion {
                        emitCurrentState()
                    }.catch {
                        errorAction.invoke(null)
                        clear3DSValues()
                    }.collect()
            } else {
                errorAction.invoke(null)
                clear3DSValues()
            }
        }
    }

    fun clear3DSValues() {
        buttonEnabled = true
        clientSecretFor3DSVerification = ""
        selectedCardFor3DSVerification = null

        scope.launch {
            emitCurrentState()
        }
    }

    private fun createCheckoutData(shippingAmount: Double, total: Double, bonusAmount: Double?, checkout: Checkout? = null): CheckoutData {
        return CheckoutData.builder()
            .amount(total)
            .id(checkout?.id())
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .bonusAmount(bonusAmount)
            .shippingAmount(shippingAmount)
            .build()
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableLatePledgeCheckoutUIState.emit(
            LatePledgeCheckoutUIState(
                storeCards = storedCards,
                userEmail = userEmail,
                isLoading = isLoading,
                selectedRewards = selectedRewards.toList(),
                shippingAmount = this.pledgeData?.shippingCostIfShipping() ?: 0.0,
                checkoutTotal = this.pledgeData?.checkoutTotalAmount() ?: 0.0,
                isPledgeButtonEnabled = buttonEnabled && !isLoading,
            )
        )
    }

    fun sendPageViewedEvent() {
        this.pledgeData?.let { pData ->
            this.checkoutData?.let { cData ->
                this.selectedReward?.let {
                    this@LatePledgeCheckoutViewModel.analytics.trackCheckoutScreenViewed(
                        checkoutData = cData,
                        pledgeData = pData
                    )
                }
            }
        }
    }

    fun sendSubmitCTAEvent() {
        this.pledgeData?.let { pData ->
            this.checkoutData?.let { cData ->
                this@LatePledgeCheckoutViewModel.analytics.trackLatePledgeSubmitCTA(
                    checkoutData = cData,
                    pledgeData = pData
                )
            }
        }
    }

    fun userRewardSelection(reward: Reward) {
        this.selectedReward = reward
    }

    fun loading() {
        scope.launch {
            emitCurrentState(isLoading = true)
        }
    }

    @SuppressLint("LogNotTimber")
    private fun createCheckout() {
        this.pledgeData?.let { pData ->
            val locationId = if (!RewardUtils.isNoReward(pData.reward())) pData.locationId() else null
            val rewards = if (!RewardUtils.isNoReward(pData.reward())) pData.rewardsAndAddOnsList() else emptyList()
            val totalPledge = pData.checkoutTotalAmount()

            this.pledgeData?.projectData()?.let { projectData ->
                if (projectData.project()
                    .postCampaignPledgingEnabled() == true && projectData.project()
                        .isInPostCampaignPledgingPhase() == true
                ) {
                    scope.launch(dispatcher) {
                        apolloClient.createCheckout(
                            CreateCheckoutData(
                                project = projectData.project(),
                                amount = totalPledge.toString(),
                                locationId = locationId?.toString(),
                                rewardsIds = rewards,
                                refTag = projectData.refTagFromIntent()
                            )
                        )
                            .asFlow()
                            .onStart {
                                emitCurrentState(isLoading = false)
                            }
                            .map { checkoutPayment ->
                                mutableCheckoutPayment.emit(checkoutPayment)
                            }
                            .catch {
                                errorAction.invoke(it.message)
                            }
                            .onCompletion {
                                emitCurrentState()
                            }
                            .collect()
                    }
                } else {
                    errorAction.invoke(null)
                }
            }
        }
    }

    /**
     * For testing, by default run in
     * scope: viewModelScope
     * dispatcher: Dispatchers.IO
     *
     */
    @VisibleForTesting
    internal fun provideScopeAndDispatcher(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        this.scope = scope
        this.dispatcher = dispatcher
    }

    @SuppressLint("LogNotTimber")
    fun providePledgeData(pledgeData: PledgeData) {
        this.pledgeData = pledgeData
        this.checkoutData = createCheckoutData(pledgeData.shippingCostIfShipping(), pledgeData.pledgeAmountTotal(), pledgeData.bonusAmount())
        selectedRewards.clear()
        pledgeData.addOns()?.let { addOns ->
            selectedRewards.add(pledgeData.reward())
            selectedRewards.addAll(addOns)
        }
        scope.launch(dispatcher) {
            loadUserInfo()
            createCheckout()
        }
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LatePledgeCheckoutViewModel(environment) as T
        }
    }
}
