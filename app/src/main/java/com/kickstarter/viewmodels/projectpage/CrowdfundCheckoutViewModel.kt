package com.kickstarter.viewmodels.projectpage

import CollectionOptions
import android.os.Bundle
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.expandedRewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getBackingData
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.services.mutations.getUpdateBackingData
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class CheckoutUIState(
    val storeCards: List<StoredCard> = listOf(),
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val selectedRewards: List<Reward> = emptyList(),
    val shippingAmount: Double = 0.0,
    val checkoutTotal: Double = 0.0,
    val isPledgeButtonEnabled: Boolean = true,
    val selectedPaymentMethod: StoredCard = StoredCard.builder().build(),
    val bonusAmount: Double = 0.0,
    val shippingRule: ShippingRule? = null,
    val showPlotWidget: Boolean = false,
    val plotEligible: Boolean = false,
    val isIncrementalPledge: Boolean? = null,
    val paymentIncrements: List<PaymentIncrement>? = null
)

data class PaymentSheetPresenterState(val setupClientId: String = "")
class CrowdfundCheckoutViewModel(val environment: Environment, bundle: Bundle? = null) : ViewModel() {
    val analytics = requireNotNull(environment.analytics())
    val apolloClient = requireNotNull(environment.apolloClientV2())
    val currentUser = requireNotNull(environment.currentUserV2()?.loggedInUser()?.asFlow())
    val cookieManager = requireNotNull(environment.cookieManager())
    val sharedPreferences = requireNotNull(environment.sharedPreferences())
    val ffClient = requireNotNull(environment.featureFlagClient())

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null // TOD potentially needs to change with user card input
    private var pledgeReason: PledgeReason? = null
    private var storedCards = emptyList<StoredCard>()
    private var project = Project.builder().build()
    private var backing: Backing? = null
    private var user: User? = null
    private var selectedRewards = emptyList<Reward>()
    private var selectedPaymentMethod: StoredCard = StoredCard.builder().build()
    private var shippingRule: ShippingRule? = null
    private var refTag: RefTag? = null
    private var shippingAmount = 0.0
    private var totalAmount = 0.0
    private var bonusAmount = 0.0
    private var thirdPartyEventSent = Pair(false, "")
    private var incrementalPledge: Boolean? = null
    private var showPlotWidget: Boolean = false
    private var plotEligible: Boolean = false
    private var paymentIncrements: List<PaymentIncrement>? = null

    private var errorAction: (message: String?) -> Unit = {}

    private var scope: CoroutineScope = viewModelScope
    private var dispatcher: CoroutineDispatcher = Dispatchers.IO

    // - UI screen states
    private var _crowdfundCheckoutUIState = MutableStateFlow(CheckoutUIState())
    val crowdfundCheckoutUIState: StateFlow<CheckoutUIState>
        get() = _crowdfundCheckoutUIState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = CheckoutUIState(isLoading = false)
            )

    // - CreateBacking/UpdateBacking Result States
    private var _checkoutResultState = MutableStateFlow<Pair<CheckoutData, PledgeData>>(Pair(null, null))
    val checkoutResultState: StateFlow<Pair<CheckoutData, PledgeData>>
        get() = _checkoutResultState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Pair<CheckoutData, PledgeData>(null, null)
            )

    // - PaymentSheet related states
    private var _presentPaymentSheet = MutableStateFlow(PaymentSheetPresenterState())
    val presentPaymentSheetStates
        get() = _presentPaymentSheet
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = PaymentSheetPresenterState()
            )

    /**
     * By default run in
     * scope: viewModelScope
     * dispatcher: Dispatchers.IO
     */
    fun provideScopeAndDispatcher(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        this.scope = scope
        this.dispatcher = dispatcher
    }

    /**
     * PledgeData information that is given to the VM via
     * constructor on the bundle object.
     */
    fun getPledgeData() = this.pledgeData

    /**
     * PledgeReason information that is given to the VM via
     * constructor on the bundle object.
     */
    fun getPledgeReason() = this.pledgeReason

    /**
     * Takes the arguments from the ArgumentsKey.PLEDGE_PLEDGE_DATA and ArgumentsKey.PLEDGE_PLEDGE_REASON
     * from the Bundle.
     *
     * Set the pledgeData and pledgeReason, these values will be used for several functions on this viewmodel.
     * This method is called from the [com.kickstarter.ui.fragments.projectpage.CheckoutFragment.onCreate]
     *
     * @param arguments the bundle coming from the previous screen
     *  - PledgeData (Parcelable)
     *  - PledgeReason (Serializable)
     *
     *  The method will also extract from PledgeData the Project and its backing information.
     *  The method will also extract the refTag.
     *  The method will also trigger the proper method to extract information from either the backing or the pledgeData.
     *  The method will also trigger the user information collection.
     *  The method will also trigger the page viewed event.
     *  The method will also trigger the payment plan build if the project is eligible.
     */
    fun provideBundle(arguments: Bundle?) {
        val pData = arguments?.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData?
        pledgeReason = arguments?.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason?

        if (pData != null) {
            pledgeData = pData
            project = pData.projectData().project()
            backing = project.backing()
            refTag = RefTagUtils.storedCookieRefTagForProject(
                project,
                cookieManager,
                sharedPreferences
            )

            when (pledgeReason) {
                PledgeReason.PLEDGE,
                PledgeReason.UPDATE_REWARD -> getPledgeInfoFrom(pData)
                PledgeReason.UPDATE_PAYMENT,
                PledgeReason.FIX_PLEDGE -> {
                    backing?.let { getPledgeInfoFrom(it) }
                }
                else -> {
                    errorAction.invoke(null)
                }
            }

            collectUserInformation()
            sendPageViewedEvent()
            if (project.isPledgeOverTimeAllowed().isTrue()) {
                buildPaymentPlan()
            }
        }
    }

    private fun getPledgeInfoFrom(backing: Backing) {
        // TODO: explore make re-usable into a separate Utils/extension extracting function all information from backing code
        val list = mutableListOf<Reward>()
        backing.reward()?.let {
            list.add(it)
        }
        backing.addOns()?.let {
            list.addAll(it)
        }
        backing.location()?.let {
            shippingRule = ShippingRule.builder()
                .location(it)
                .build()
        }

        if (backing.location() == null && backing.locationName() != null && backing.locationId() != null) {
            val location = Location.builder()
                .name(backing.locationName())
                .displayableName(backing.locationName())
                .id(backing.locationId())
                .build()
            shippingRule = ShippingRule.builder()
                .location(location)
                .build()
        }

        selectedRewards = list.toList()

        shippingAmount = (backing.shippingAmount() ?: 0.0).toDouble()

        bonusAmount = (backing.bonusAmount() ?: 0.0).toDouble()
        totalAmount = (backing.amount() ?: 0.0).toDouble()

        // - User was backing reward no reward
        if (backing.reward() == null) {
            bonusAmount = 0.0
        }

        checkoutData = CheckoutData.builder()
            .amount(totalAmount)
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .bonusAmount(bonusAmount)
            .shippingAmount(shippingAmount)
            .build()
    }

    private fun getPledgeInfoFrom(pData: PledgeData) {
        selectedRewards = pData.rewardsAndAddOnsList()
        if (selectedRewards.isNotEmpty()) {
            val isNoReward = RewardUtils.isNoReward(selectedRewards.first())
            pledgeData = pData
            refTag = RefTagUtils.storedCookieRefTagForProject(
                project,
                cookieManager,
                sharedPreferences
            )
            shippingRule = pData.shippingRule()

            if (!isNoReward) {
                shippingAmount = pData.shippingCostIfShipping()
                bonusAmount = pData.bonusAmount()
                totalAmount = pData.checkoutTotalAmount()
            }

            if (isNoReward) {
                totalAmount = selectedRewards.first().pledgeAmount() + pData.bonusAmount()
                bonusAmount = 0.0
            }

            checkoutData = CheckoutData.builder()
                .amount(pData.pledgeAmountTotal())
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .bonusAmount(bonusAmount)
                .shippingAmount(pData.shippingCostIfShipping())
                .build()
        }
    }

    private fun buildPaymentPlan() {
        scope.launch(dispatcher) {
            apolloClient.buildPaymentPlan(BuildPaymentPlanData(pledgeData?.projectData()?.project()?.slug() ?: "", pledgeData?.checkoutTotalAmount().toString() ?: "")).asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }.catch {
                    errorAction.invoke(it.message)
                    emitCurrentState(isLoading = false)
                }
                .collectLatest {
                    showPlotWidget = project.isPledgeOverTimeAllowed() == true
                    plotEligible = it.amountIsPledgeOverTimeEligible
                    paymentIncrements = it.paymentIncrements
                    emitCurrentState(isLoading = false)
                }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    // TODO: can potentially be extracted to an UserUseCase
    private fun collectUserInformation() {
        scope.launch(dispatcher) {
            emitCurrentState(isLoading = true)
            currentUser.combine(apolloClient.userPrivacy().asFlow()) { cUser, privacy ->
                cUser.toBuilder()
                    .email(privacy.email)
                    .name(privacy.name)
                    .build()
            }.catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }.combine(apolloClient.getStoredCards().asFlow()) { updatedUser, cards ->
                user = updatedUser
                storedCards = cards
            }.catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }.collectLatest {
                emitCurrentState(isLoading = false)
            }
        }
    }

    private fun sendPageViewedEvent() {
        if (checkoutData != null && pledgeData != null) {
            if (pledgeData?.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE)
                analytics.trackCheckoutScreenViewed(requireNotNull(checkoutData), requireNotNull(pledgeData))
            else analytics.trackUpdatePledgePageViewed(requireNotNull(checkoutData), requireNotNull(pledgeData))
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        _crowdfundCheckoutUIState.emit(
            CheckoutUIState(
                storeCards = storedCards.toList(),
                userEmail = user?.email() ?: "",
                isLoading = isLoading,
                selectedRewards = selectedRewards,
                shippingAmount = shippingAmount,
                checkoutTotal = totalAmount,
                isPledgeButtonEnabled = !isLoading,
                selectedPaymentMethod = selectedPaymentMethod,
                bonusAmount = bonusAmount,
                shippingRule = shippingRule,
                plotEligible = plotEligible,
                showPlotWidget = showPlotWidget,
                paymentIncrements = paymentIncrements,
                isIncrementalPledge = incrementalPledge
            )
        )
    }

    /**
     * Should be called from PaymentSheet `PaymentOptionCallback`
     * it will provide on @param paymentMethodSelected enough information
     * to update the UI of available payment methods.
     *
     * The payment method will not be saved on the backend user profile
     * until a successful pledge/update pledge is successfully performed
     */
    fun newlyAddedPaymentMethod(paymentMethodSelected: StoredCard?) {
        paymentMethodSelected?.let {
            selectedPaymentMethod = it

            // - Update the list of available payment methods with the newly added one
            if (paymentMethodSelected.isFromPaymentSheet()) {
                val updatedCards = mutableListOf(paymentMethodSelected)
                updatedCards.addAll(storedCards)
                storedCards = updatedCards
            }

            scope.launch {
                emitCurrentState(isLoading = true)
            }
        }
    }

    fun collectionPlanSelected(collectionOption: CollectionOptions) {
        incrementalPledge =
            when (collectionOption) {
                CollectionOptions.PLEDGE_IN_FULL -> false
                CollectionOptions.PLEDGE_OVER_TIME -> true
            }

        scope.launch {
            emitCurrentState(isLoading = false)
        }
    }

    fun userChangedPaymentMethodSelected(paymentMethodSelected: StoredCard?) {
        paymentMethodSelected?.let {
            selectedPaymentMethod = it
        }

        // - Send event on background thread
        scope.launch(dispatcher) {
            SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
                .sendThirdPartyEvent(
                    project = Observable.just(project),
                    currentUser = requireNotNull(environment.currentUserV2()),
                    apolloClient = apolloClient,
                    draftPledge = Pair(pledgeData?.pledgeAmountTotal(), shippingAmount),
                    checkoutAndPledgeData = Observable.just(Pair(checkoutData, pledgeData)),
                    eventName = ThirdPartyEventValues.EventName.ADD_PAYMENT_INFO
                ).asFlow().collect {
                    thirdPartyEventSent = it
                }
        }
    }

    fun isThirdPartyEventSent(): Pair<Boolean, String> = this.thirdPartyEventSent

    /**
     * Called when the user hits the pledge button.
     *
     * This function updates the selected payment method and incremental pledge status
     * based on the provided parameters. It then emits the current state of the UI.
     *
     * Finally, it triggers either `createBacking()` or `updateBacking()` based on the
     * `pledgeReason`.
     *
     * @param selectedCard The card selected by the user for payment. Can be null if no card is selected.
     * @param isIncremental A boolean indicating whether the pledge is incremental.
     *                      This is only considered if the project allows incremental pledges.
     *                      Can be null.
     */
    fun pledgeOrUpdatePledge(selectedCard: StoredCard?, isIncremental: Boolean?) {
        selectedCard?.let {
            selectedPaymentMethod = it
        }
        incrementalPledge = if (project.isPledgeOverTimeAllowed() == true) {
            isIncremental
        } else {
            null
        }
        scope.launch {
            emitCurrentState()
        }

        scope.launch(dispatcher) {
            when (pledgeReason) {
                PledgeReason.PLEDGE -> createBacking()
                PledgeReason.UPDATE_PLEDGE,
                PledgeReason.UPDATE_REWARD,
                PledgeReason.UPDATE_PAYMENT,
                PledgeReason.FIX_PLEDGE -> updateBacking()
                else -> {
                    errorAction.invoke(null)
                }
            }
        }
    }

    private suspend fun createBacking() {
        if (checkoutData != null && pledgeData != null) {
            analytics.trackPledgeSubmitCTA(requireNotNull(checkoutData), requireNotNull(pledgeData))
        }

        val shouldNotSendId = pledgeData?.reward()?.let {
            RewardUtils.isDigital(it) || RewardUtils.isNoReward(it) || RewardUtils.isLocalPickup(it)
        } ?: true

        val locationID = pledgeData?.shippingRule()?.location()?.id()?.toString()
        val backingData = selectedPaymentMethod.getBackingData(
            proj = project,
            amount = pledgeData?.checkoutTotalAmount().toString(),
            locationId = if (shouldNotSendId) null else locationID,
            rewards = RewardUtils.extendAddOns(pledgeData?.rewardsAndAddOnsList() ?: emptyList<Reward>()),
            cookieRefTag = refTag,
            incremental = incrementalPledge
        )

        this.apolloClient.createBacking(backingData).asFlow()
            .onStart {
                emitCurrentState(isLoading = true)
            }.catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }
            .collectLatest {
                checkoutData = checkoutData?.toBuilder()?.id(it.id())?.build()
                _checkoutResultState.emit(Pair(checkoutData, pledgeData))
                emitCurrentState(isLoading = false)
            }
    }

    private suspend fun updateBacking() {
        project.backing()?.let { backing ->
            val backingData = when (pledgeReason) {
                PledgeReason.UPDATE_PAYMENT -> {
                    if (project.isPledgeOverTimeAllowed().isTrue()) {
                        // PLOT projects: Only send the payment method
                        // Backend expects NO amount, NO rewards, NO location — sending any of these
                        // may result in 'Feature unavailable' error.
                        getUpdateBackingData(
                            backing,
                            amount = null,
                            locationId = null,
                            rewardsList = null,
                            pMethod = selectedPaymentMethod,
                            incremental = null
                        )
                    } else {
                        // Non-PLOT: send the payment method, locationId, and rewards
                        val locationId = backing.locationId()?.toString()
                        val rwl = mutableListOf<Reward>()
                        backing.reward()?.let {
                            rwl.add(it)
                        }
                        backing.addOns()?.let {
                            rwl.addAll(RewardUtils.extendAddOns(it))
                        }

                        getUpdateBackingData(
                            backing,
                            amount = null,
                            locationId = locationId,
                            rwl,
                            pMethod = selectedPaymentMethod,
                            incremental = null
                        )
                    }
                }

                PledgeReason.UPDATE_REWARD,
                PledgeReason.UPDATE_PLEDGE -> {
                    // - Update Reward/Pledge should send ALL newly selected rewards/Locations
                    val isShippable = pledgeData?.reward()?.let { RewardUtils.isShippable(it) } ?: false
                    val locationIdOrNull =
                        if (isShippable) pledgeData?.shippingRule()?.location()?.id().toString()
                        else null
                    val isNoRw = pledgeData?.reward()?.let { RewardUtils.isNoReward(it) } ?: false
                    val rwListOrEmpty = if (isNoRw) emptyList<Reward>()
                    else pledgeData?.expandedRewardsAndAddOnsList() ?: emptyList()

                    getUpdateBackingData(
                        backing,
                        pledgeData?.checkoutTotalAmount().toString(),
                        locationId = locationIdOrNull,
                        rwListOrEmpty,
                        null,
                        incremental = incrementalPledge
                    )
                }
                PledgeReason.FIX_PLEDGE -> {
                    // - Fix pledge should NOT send amounts/rewardId's/locationId ONLY selected paymentMethod
                    getUpdateBackingData(
                        backing = backing,
                        pMethod = selectedPaymentMethod
                    )
                }
                PledgeReason.PLEDGE, // Error
                PledgeReason.LATE_PLEDGE, // Error
                null -> { null }
            }

            backingData?.let {
                apolloClient.updateBacking(it).asFlow()
                    .onStart {
                        emitCurrentState(isLoading = true)
                    }.catch {
                        errorAction.invoke(it.message)
                        emitCurrentState(isLoading = false)
                    }.collectLatest {
                        checkoutData = checkoutData?.toBuilder()?.id(it.id())?.build()
                        _checkoutResultState.emit(Pair(checkoutData, pledgeData))
                        emitCurrentState(isLoading = false)
                    }
            }
        }
    }

    /**
     * PaymentSheet has been presented to the user, stop loading until
     * a new payment method is received. Will cover as well the case of
     * an user dismissing PaymentSheet without adding a payment method
     */
    fun paymentSheetPresented(state: Boolean) {
        scope.launch {
            emitCurrentState(isLoading = !state)
        }
    }

    /**
     * Required to present the Stripe PaymentSheet to the user
     */
    fun getSetupIntent() {
        scope.launch(dispatcher) {
            apolloClient.createSetupIntent(project).asFlow()
                .onStart { emitCurrentState(isLoading = true) }
                .catch {
                    emitCurrentState(isLoading = false)
                    errorAction.invoke(it.message)
                }
                .collectLatest {
                    _presentPaymentSheet.emit(PaymentSheetPresenterState(it))
                }
        }
    }

    /**
     * If @param = PaymentSheetResult.Failed or PaymentSheetResult.Canceled
     * reload remove the payment methods added via payment sheet and keep only those
     * obtained via `apolloClient.getStoredCards()`. PaymentSheetResult.Canceled will be produce
     * by a failed/abandoned 3DS challenge
     *
     * If @PaymentSheetResult.Completed stop loading state
     */
    fun paymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            PaymentSheetResult.Canceled,
            is PaymentSheetResult.Failed -> {
                scope.launch {
                    val updatedList = storedCards.filter { !it.isFromPaymentSheet() }
                    storedCards = updatedList
                    emitCurrentState(isLoading = false)
                }
            }
            PaymentSheetResult.Completed -> {
                scope.launch {
                    emitCurrentState(isLoading = false)
                }
            }
        }
    }

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrowdfundCheckoutViewModel(environment, bundle) as T
        }
    }
}
