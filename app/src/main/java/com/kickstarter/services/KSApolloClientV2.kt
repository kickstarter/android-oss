package com.kickstarter.services

import CreatePaymentIntentMutation
import android.util.Pair
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Comment
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.services.transformers.backingTransformer
import com.kickstarter.services.transformers.categoryTransformer
import com.kickstarter.services.transformers.commentTransformer
import com.kickstarter.services.transformers.complexRewardItemsTransformer
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.getTriggerThirdPartyEventMutation
import com.kickstarter.services.transformers.projectTransformer
import com.kickstarter.services.transformers.rewardTransformer
import com.kickstarter.services.transformers.shippingRulesListTransformer
import com.kickstarter.services.transformers.updateTransformer
import com.kickstarter.services.transformers.userPrivacyTransformer
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import type.BackingState
import type.CurrencyCode
import type.FlaggingKind
import type.PaymentTypes

interface ApolloClientTypeV2 {
    fun getProject(project: Project): Observable<Project>
    fun getProject(slug: String): Observable<Project>
    fun createSetupIntent(project: Project? = null): Observable<String>
    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>
    fun getStoredCards(): Observable<List<StoredCard>>
    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>
    fun createFlagging(
        project: Project? = null,
        details: String,
        flaggingKind: String
    ): Observable<String>

    fun userPrivacy(): Observable<UserPrivacy>
    fun watchProject(project: Project): Observable<Project>
    fun unWatchProject(project: Project): Observable<Project>
    fun updateUserPassword(
        currentPassword: String = "",
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data>

    fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>
    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>
    fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope>
    fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>>
    fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout>
    fun createBacking(createBackingData: CreateBackingData): Observable<Checkout>
    fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>>
    fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data>

    fun creatorDetails(slug: String): Observable<CreatorDetails>
    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>
    fun cancelBacking(backing: Backing, note: String): Observable<Any>
    fun fetchCategory(param: String): Observable<Category?>
    fun getBacking(backingId: String): Observable<Backing>
    fun fetchCategories(): Observable<List<Category>>

    fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int = PAGE_SIZE
    ): Observable<UpdatesGraphQlEnvelope>

    fun getComment(commentableId: String): Observable<Comment>
    fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int = PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun getProjectComments(
        slug: String,
        cursor: String,
        limit: Int = PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun getRepliesForComment(
        comment: Comment,
        cursor: String? = null,
        pageSize: Int = REPLIES_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun createComment(comment: PostCommentData): Observable<Comment>
    fun erroredBackings(): Observable<List<ErroredBacking>>

    fun clearUnseenActivity(): Observable<Int>

    fun getProjectBacking(slug: String): Observable<Backing>

    fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment>

    fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String>
}

private const val PAGE_SIZE = 25
private const val REPLIES_PAGE_SIZE = 7

class KSApolloClientV2(val service: ApolloClient) : ApolloClientTypeV2 {
    override fun getProject(project: Project): Observable<Project> {
        return getProject(project.slug() ?: "")
    }

    override fun getProject(slug: String): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            this.service.query(
                FetchProjectQuery.builder()
                    .slug(slug)
                    .build()
            ).enqueue(object : ApolloCall.Callback<FetchProjectQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<FetchProjectQuery.Data>) {
                    if (response.hasErrors()) ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    else {
                        response.data?.let { responseData ->
                            ps.onNext(
                                projectTransformer(
                                    responseData.project()?.fragments()?.fullProject()
                                )
                            )
                        }
                        ps.onComplete()
                    }
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun createSetupIntent(project: Project?): Observable<String> {
        return Observable.defer {
            val createSetupIntentMut = CreateSetupIntentMutation.builder()
                .apply {
                    if (project != null) this.projectId(encodeRelayId(project))
                }
                .build()

            val ps = PublishSubject.create<String>()
            this.service.mutate(createSetupIntentMut)
                .enqueue(object : ApolloCall.Callback<CreateSetupIntentMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreateSetupIntentMutation.Data>) {
                        if (response.hasErrors()) ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        else {
                            ps.onNext(response.data?.createSetupIntent()?.clientSecret() ?: "")
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.defer {
            val ps = PublishSubject.create<StoredCard>()
            service.mutate(
                SavePaymentMethodMutation.builder()
                    .paymentType(savePaymentMethodData.paymentType)
                    .stripeToken(savePaymentMethodData.stripeToken)
                    .stripeCardId(savePaymentMethodData.stripeCardId)
                    .reusable(savePaymentMethodData.reusable)
                    .intentClientSecret(savePaymentMethodData.intentClientSecret)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<SavePaymentMethodMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<SavePaymentMethodMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        val paymentSource = response.data?.createPaymentSource()?.paymentSource()
                        paymentSource?.let {
                            val storedCard = StoredCard.builder()
                                .expiration(it.expirationDate())
                                .id(it.id())
                                .lastFourDigits(it.lastFour())
                                .type(it.type())
                                .build()
                            ps.onNext(storedCard)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<StoredCard>>()
            this.service.query(UserPaymentsQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<UserPaymentsQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UserPaymentsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            val cardsList = mutableListOf<StoredCard>()
                            response.data?.me()?.storedCards()?.nodes()?.map {
                                it?.let { cardData ->
                                    val card = StoredCard.builder()
                                        .expiration(cardData.expirationDate())
                                        .id(cardData.id())
                                        .lastFourDigits(cardData.lastFour())
                                        .type(it.type())
                                        .build()
                                    cardsList.add(card)
                                }
                            }
                            ps.onNext(cardsList)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<DeletePaymentSourceMutation.Data>()
            service.mutate(
                DeletePaymentSourceMutation.builder()
                    .paymentSourceId(paymentSourceId)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<DeletePaymentSourceMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<DeletePaymentSourceMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        response.data?.let { ps.onNext(it) }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun createFlagging(
        project: Project?,
        details: String,
        flaggingKind: String
    ): Observable<String> {
        return Observable.defer {
            project?.let {
                val ps = PublishSubject.create<String>()
                val flagging = FlaggingKind.safeValueOf(flaggingKind)
                val mutation = CreateFlaggingMutation.builder()
                    .contentId(encodeRelayId(it))
                    .details(details)
                    .kind(flagging)
                    .build()

                service.mutate(
                    mutation
                ).enqueue(object : ApolloCall.Callback<CreateFlaggingMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreateFlaggingMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let { data ->
                            data.createFlagging()?.flagging()?.kind()?.name?.let { kindString ->
                                ps.onNext(kindString)
                            }
                        }
                        ps.onComplete()
                    }
                })
                return@defer ps
            }
        }
    }

    override fun userPrivacy(): Observable<UserPrivacy> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacy>()
            service.query(UserPrivacyQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                        response.data?.me()?.let {
                            ps.onNext(userPrivacyTransformer(it))
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun watchProject(project: Project): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            this.service.mutate(
                WatchProjectMutation.builder().id(encodeRelayId(project)).build()
            )
                .enqueue(object : ApolloCall.Callback<WatchProjectMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<WatchProjectMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        }
                        /* make a copy of what you posted. just in case
                         * we want to update the list without doing
                         * a full refresh.
                         */
                        ps.onNext(
                            projectTransformer(
                                response.data?.watchProject()?.project()?.fragments()?.fullProject()
                            )
                        )
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun unWatchProject(project: Project): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            this.service.mutate(
                UnwatchProjectMutation.builder().id(encodeRelayId(project)).build()
            )
                .enqueue(object : ApolloCall.Callback<UnwatchProjectMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UnwatchProjectMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        }
                        /* make a copy of what you posted. just in case
                         * we want to update the list without doing
                         * a full refresh.
                         */
                        ps.onNext(
                            projectTransformer(
                                response.data?.watchProject()?.project()?.fragments()?.fullProject()
                            )
                        )
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun updateUserPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserPasswordMutation.Data>()
            service.mutate(
                UpdateUserPasswordMutation.builder()
                    .currentPassword(currentPassword)
                    .password(newPassword)
                    .passwordConfirmation(confirmPassword)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<UpdateUserPasswordMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UpdateUserPasswordMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let {
                            ps.onNext(it)
                        }

                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserEmailMutation.Data>()
            service.mutate(
                UpdateUserEmailMutation.builder()
                    .email(email)
                    .currentPassword(currentPassword)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<UpdateUserEmailMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UpdateUserEmailMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let { data ->
                            ps.onNext(data)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<SendEmailVerificationMutation.Data>()
            service.mutate(
                SendEmailVerificationMutation.builder()
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<SendEmailVerificationMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<SendEmailVerificationMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let { data ->
                            ps.onNext(data)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserCurrencyMutation.Data>()
            service.mutate(
                UpdateUserCurrencyMutation.builder()
                    .chosenCurrency(currency)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<UpdateUserCurrencyMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UpdateUserCurrencyMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let {
                            ps.onNext(it)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<ShippingRulesEnvelope>()
            val query = GetShippingRulesForRewardIdQuery.builder()
                .rewardId(encodeRelayId(reward))
                .build()

            this.service.query(query)
                .enqueue(object : ApolloCall.Callback<GetShippingRulesForRewardIdQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetShippingRulesForRewardIdQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        response.data?.let { data ->
                            Observable.just(data?.node() as? GetShippingRulesForRewardIdQuery.AsReward)
                                .filter { !it?.shippingRulesExpanded()?.nodes().isNullOrEmpty() }
                                .map {
                                    it?.shippingRulesExpanded()?.nodes()?.mapNotNull { node ->
                                        node.fragments().shippingRule()
                                    }
                                }
                                .filter { it.isNotNull() }
                                .subscribe { shippingList ->
                                    val shippingEnvelope =
                                        shippingRulesListTransformer(shippingList ?: emptyList())
                                    ps.onNext(shippingEnvelope)
                                }.dispose()
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun getAddOnsFromProject(addOnsGr: GetProjectAddOnsQuery.AddOns): List<Reward> {
        return addOnsGr.nodes()?.map { node ->
            val shippingRulesGr =
                node.shippingRulesExpanded()?.nodes()?.map { it.fragments().shippingRule() }
                    ?: emptyList()
            rewardTransformer(
                node.fragments().reward(),
                shippingRulesGr,
                addOnItems = complexRewardItemsTransformer(node.items()?.fragments()?.rewardItems())
            )
        }?.toList() ?: emptyList()
    }

    override fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<Reward>>()
            val query = GetProjectAddOnsQuery.builder()
                .slug(slug)
                .locationId(encodeRelayId(locationId))
                .build()

            this.service.query(query)
                .enqueue(object : ApolloCall.Callback<GetProjectAddOnsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectAddOnsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let { data ->
                            Observable.just(data.project()?.addOns())
                                .filter { it?.nodes() != null }
                                .map<List<Reward>> { addOnsList ->
                                    addOnsList?.let {
                                        getAddOnsFromProject(
                                            it
                                        )
                                    } ?: emptyList()
                                }
                                .subscribe {
                                    ps.onNext(it)
                                }.dispose()
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
        return Observable.defer {
            val updateBackingMutation = UpdateBackingMutation.builder()
                .backingId(encodeRelayId(updateBackingData.backing))
                .amount(updateBackingData.amount.toString())
                .locationId(updateBackingData.locationId)
                .rewardIds(updateBackingData.rewardsIds?.let { list -> list.map { encodeRelayId(it) } })
                .apply {
                    updateBackingData.paymentSourceId?.let { this.paymentSourceId(it) }
                    updateBackingData.intentClientSecret?.let { this.intentClientSecret(it) }
                }
                .build()

            val ps = PublishSubject.create<Checkout>()
            service.mutate(updateBackingMutation)
                .enqueue(object : ApolloCall.Callback<UpdateBackingMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UpdateBackingMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        } else {
                            val checkoutPayload = response.data?.updateBacking()?.checkout()
                            val backing = Checkout.Backing.builder()
                                .clientSecret(
                                    checkoutPayload?.backing()?.fragments()?.checkoutBacking()
                                        ?.clientSecret()
                                )
                                .requiresAction(
                                    checkoutPayload?.backing()?.fragments()?.checkoutBacking()
                                        ?.requiresAction() ?: false
                                )
                                .build()

                            val checkout = Checkout.builder()
                                .id(decodeRelayId(checkoutPayload?.id()))
                                .backing(backing)
                                .build()
                            ps.onNext(checkout)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
        return Observable.defer {
            val createBackingMutation = CreateBackingMutation.builder()
                .projectId(encodeRelayId(createBackingData.project))
                .amount(createBackingData.amount)
                .paymentType(PaymentTypes.CREDIT_CARD.rawValue())
                .paymentSourceId(createBackingData.paymentSourceId)
                .setupIntentClientSecret(createBackingData.setupIntentClientSecret)
                .locationId(createBackingData.locationId?.let { it })
                .rewardIds(createBackingData.rewardsIds?.let { list -> list.map { encodeRelayId(it) } })
                .refParam(createBackingData.refTag?.tag())
                .build()

            val ps = PublishSubject.create<Checkout>()

            this.service.mutate(createBackingMutation)
                .enqueue(object : ApolloCall.Callback<CreateBackingMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreateBackingMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        } else {

                            val checkoutPayload = response.data?.createBacking()?.checkout()

                            // TODO: Add new status field to backing model
                            val backing = Checkout.Backing.builder()
                                .clientSecret(
                                    checkoutPayload?.backing()?.fragments()?.checkoutBacking()
                                        ?.clientSecret()
                                )
                                .requiresAction(
                                    checkoutPayload?.backing()?.fragments()?.checkoutBacking()
                                        ?.requiresAction() ?: false
                                )
                                .build()

                            val checkout = Checkout.builder()
                                .id(decodeRelayId(checkoutPayload?.id()))
                                .backing(backing)
                                .build()
                            ps.onNext(checkout)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
        return Observable.defer {
            val ps = PublishSubject.create<Pair<Boolean, String>>()

            val mutation = getTriggerThirdPartyEventMutation(eventInput)

            service.mutate(mutation)
                .enqueue(object : ApolloCall.Callback<TriggerThirdPartyEventMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<TriggerThirdPartyEventMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message ?: ""))
                        }

                        response.data?.let {
                            val message = it.triggerThirdPartyEvent()?.message() ?: ""
                            val isSuccess = it.triggerThirdPartyEvent()?.success() ?: false
                            ps.onNext(Pair(isSuccess, message))
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatePasswordMutation.Data>()
            service.mutate(
                CreatePasswordMutation.builder()
                    .password(password)
                    .passwordConfirmation(confirmPassword)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<CreatePasswordMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreatePasswordMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        }
                        response.data?.let {
                            ps.onNext(it)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun creatorDetails(slug: String): Observable<CreatorDetails> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatorDetails>()
            service.query(
                ProjectCreatorDetailsQuery.builder()
                    .slug(slug)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<ProjectCreatorDetailsQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<ProjectCreatorDetailsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        response.data?.project()?.creator()?.let {
                            ps.onNext(
                                CreatorDetails.builder()
                                    .backingsCount(it.backingsCount())
                                    .launchedProjectsCount(it.launchedProjects()?.totalCount() ?: 1)
                                    .build()
                            )
                            ps.onComplete()
                        }
                    }
                })
            return@defer ps
        }
    }

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.defer {
            val ps = PublishSubject.create<Long>()
            service.mutate(
                SendMessageMutation.builder()
                    .projectId(encodeRelayId(project))
                    .recipientId(encodeRelayId(recipient))
                    .body(body)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<SendMessageMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<SendMessageMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }

                        response.data?.let {
                            decodeRelayId(
                                response.data?.sendMessage()?.conversation()?.id()
                            )?.let {
                                ps.onNext(it)
                            } ?: ps.onError(Exception())
                        }

                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.defer {
            val ps = PublishSubject.create<Any>()
            service.mutate(
                CancelBackingMutation.builder()
                    .backingId(encodeRelayId(backing))
                    .note(note)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<CancelBackingMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CancelBackingMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onNext(Exception(response.errors?.first()?.message ?: ""))
                        } else {
                            val state = response.data?.cancelBacking()?.backing()?.status()
                            val success = state == BackingState.CANCELED
                            ps.onNext(success)
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun fetchCategory(categoryParam: String): Observable<Category?> {
        return Observable.defer {
            val ps = PublishSubject.create<Category>()
            this.service.query(
                FetchCategoryQuery.builder()
                    .categoryParam(categoryParam)
                    .build()
            ).enqueue(object : ApolloCall.Callback<FetchCategoryQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<FetchCategoryQuery.Data>) {
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        val category =
                            categoryTransformer(response.data?.category()?.fragments()?.category())
                        ps.onNext(category)
                    }
                    ps.onComplete()
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getBacking(backingId: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()

            this.service.query(
                GetBackingQuery.builder()
                    .backingId(backingId).build()
            )
                .enqueue(object : ApolloCall.Callback<GetBackingQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetBackingQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            response.data?.let {
                                it.backing()?.fragments()?.let { backingFragments ->
                                    backingTransformer(
                                        backingFragments.backing()
                                    )?.let { backingObject ->
                                        ps.onNext(backingObject)
                                    }
                                }
                            }
                            ps.onComplete()
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun fetchCategories(): Observable<List<Category>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<Category>>()
            this.service.query(
                GetRootCategoriesQuery.builder()
                    .build()
            ).enqueue(object : ApolloCall.Callback<GetRootCategoriesQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<GetRootCategoriesQuery.Data>) {
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            val subCategories = responseData.rootCategories()
                                .flatMap { it.subcategories()?.nodes().orEmpty() }
                                .map {
                                    categoryTransformer(it.fragments().category())
                                }
                            val rootCategories = responseData.rootCategories()
                                .map { categoryTransformer(it.fragments().category()) }
                                .toMutableList()
                                .apply {
                                    addAll(subCategories)
                                }
                            ps.onNext(rootCategories)
                        }
                        ps.onComplete()
                    }
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int
    ): Observable<UpdatesGraphQlEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdatesGraphQlEnvelope>()

            this.service.query(
                GetProjectUpdatesQuery.builder()
                    .cursor(cursor)
                    .slug(slug)
                    .limit(limit)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectUpdatesQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectUpdatesQuery.Data>) {
                        response.data?.let { data ->
                            Observable.just(data.project())
                                .filter { it?.posts() != null }
                                .map { project ->

                                    val updates = project?.posts()?.edges()?.map { edge ->
                                        updateTransformer(
                                            edge?.node()?.fragments()?.post()
                                        ).toBuilder()
                                            .build()
                                    }

                                    UpdatesGraphQlEnvelope.builder()
                                        .updates(updates)
                                        .totalCount(project?.posts()?.totalCount() ?: 0)
                                        .pageInfoEnvelope(
                                            createPageInfoObject(
                                                project?.posts()?.pageInfo()?.fragments()
                                                    ?.pageInfo()
                                            )
                                        )
                                        .build()
                                }
                                .filter { it.isNotNull() }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onComplete()
                                }
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun createPageInfoObject(pageFr: fragment.PageInfo?): PageInfoEnvelope {
        return PageInfoEnvelope.builder()
            .endCursor(pageFr?.endCursor() ?: "")
            .hasNextPage(pageFr?.hasNextPage() ?: false)
            .hasPreviousPage(pageFr?.hasPreviousPage() ?: false)
            .startCursor(pageFr?.startCursor() ?: "")
            .build()
    }

    override fun getComment(commentableId: String): Observable<Comment> {
        return Observable.defer {
            val ps = PublishSubject.create<Comment>()
            this.service.query(
                GetCommentQuery.builder()
                    .commentableId(commentableId)
                    .build()
            ).enqueue(object : ApolloCall.Callback<GetCommentQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<GetCommentQuery.Data>) {
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            val comment = mapGetCommentQueryResponseToComment(responseData)
                            ps.onNext(comment)
                        }
                    }
                    ps.onComplete()
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun mapGetCommentQueryResponseToComment(responseData: GetCommentQuery.Data): Comment {
        val commentFragment =
            (responseData.commentable() as? GetCommentQuery.AsComment)?.fragments()?.comment()
        return commentTransformer(commentFragment)
    }

    override fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()

            this.service.query(
                GetProjectUpdateCommentsQuery.builder()
                    .cursor(cursor.ifEmpty { null })
                    .id(updateId)
                    .limit(limit)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectUpdateCommentsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectUpdateCommentsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            response.data?.let { data ->
                                data.post()?.fragments()?.freeformPost()?.comments()
                                    ?.let { graphComments ->
                                        val comments = graphComments.edges()?.map { edge ->
                                            commentTransformer(edge?.node()?.fragments()?.comment())
                                                .toBuilder()
                                                .cursor(edge?.cursor())
                                                .build()
                                        }

                                        val envelope = CommentEnvelope.builder()
                                            .comments(comments)
                                            .commentableId(data.post()?.id() ?: "")
                                            .totalCount(
                                                data.post()?.fragments()?.freeformPost()?.comments()
                                                    ?.totalCount() ?: 0
                                            )
                                            .pageInfoEnvelope(
                                                createPageInfoObject(
                                                    data.post()?.fragments()?.freeformPost()
                                                        ?.comments()
                                                        ?.pageInfo()?.fragments()?.pageInfo()
                                                )
                                            )
                                            .build()
                                        ps.onNext(envelope)
                                    }
                            }
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getProjectComments(
        slug: String,
        cursor: String,
        limit: Int
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()
            this.service.query(
                GetProjectCommentsQuery.builder()
                    .cursor(cursor.ifEmpty { null })
                    .slug(slug)
                    .limit(limit)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectCommentsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectCommentsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            response.data?.let { data ->
                                data.project()?.comments()?.let { graphComments ->
                                    val comments = graphComments.edges()?.map { edge ->
                                        commentTransformer(
                                            edge?.node()?.fragments()?.comment()
                                        ).toBuilder()
                                            .cursor(edge?.cursor())
                                            .build()
                                    }

                                    val envelope = CommentEnvelope.builder()
                                        .commentableId(data.project()?.id())
                                        .comments(comments)
                                        .totalCount(data.project()?.comments()?.totalCount() ?: 0)
                                        .pageInfoEnvelope(
                                            createPageInfoObject(
                                                data.project()?.comments()?.pageInfo()?.fragments()
                                                    ?.pageInfo()
                                            )
                                        )
                                        .build()
                                    ps.onNext(envelope)
                                }
                            }
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getRepliesForComment(
        comment: Comment,
        cursor: String?,
        pageSize: Int
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()
            this.service.query(
                GetRepliesForCommentQuery.builder()
                    .commentableId(encodeRelayId(comment))
                    .cursor(cursor)
                    .pageSize(pageSize)
                    .build()
            ).enqueue(object : ApolloCall.Callback<GetRepliesForCommentQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<GetRepliesForCommentQuery.Data>) {
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            Observable.just(createCommentEnvelop(responseData))
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onComplete()
                                }
                        }
                    }
                    ps.onComplete()
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun createCommentEnvelop(responseData: GetRepliesForCommentQuery.Data): CommentEnvelope {
        val replies =
            (responseData.commentable() as? GetRepliesForCommentQuery.AsComment)?.replies()
        val listOfComments = replies?.nodes()?.map { commentFragment ->
            commentTransformer(commentFragment.fragments().comment())
        } ?: emptyList()
        val totalCount = replies?.totalCount() ?: 0
        val pageInfo = createPageInfoObject(replies?.pageInfo()?.fragments()?.pageInfo())

        return CommentEnvelope.builder()
            .comments(listOfComments)
            .pageInfoEnvelope(pageInfo)
            .totalCount(totalCount)
            .build()
    }

    override fun createComment(comment: PostCommentData): Observable<Comment> {
        return Observable.defer {
            val ps = PublishSubject.create<Comment>()
            this.service.mutate(
                CreateCommentMutation.builder()
                    .parentId(comment.parent?.let { encodeRelayId(it) })
                    .commentableId(comment.commentableId)
                    .clientMutationId(comment.clientMutationId)
                    .body(comment.body)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<CreateCommentMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreateCommentMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        } else {
                            /* make a copy of what you posted. just in case
                         * we want to update the list without doing
                         * a full refresh.
                         */
                            ps.onNext(
                                commentTransformer(
                                    response.data?.createComment()?.comment()?.fragments()
                                        ?.comment()
                                )
                            )
                        }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun erroredBackings(): Observable<List<ErroredBacking>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<ErroredBacking>>()
            this.service.query(ErroredBackingsQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<ErroredBackingsQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<ErroredBackingsQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            Observable.just(response.data)
                                .map { cards -> cards?.me()?.backings()?.nodes() }
                                .map { list ->
                                    val erroredBackings = list?.asSequence()?.map {
                                        val project = ErroredBacking.Project.builder()
                                            .finalCollectionDate(
                                                it.project()?.finalCollectionDate()
                                            )
                                            .name(it.project()?.name())
                                            .slug(it.project()?.slug())
                                            .build()
                                        ErroredBacking.builder()
                                            .project(project)
                                            .build()
                                    }
                                    erroredBackings?.toList() ?: listOf()
                                }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onComplete()
                                }.dispose()
                        }
                    }
                })
            return@defer ps
        }
    }

    override fun clearUnseenActivity(): Observable<Int> {
        return Observable.defer {
            val ps = PublishSubject.create<Int>()
            service.mutate(
                ClearUserUnseenActivityMutation.builder()
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<ClearUserUnseenActivityMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<ClearUserUnseenActivityMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        }
                        response.data?.clearUserUnseenActivity()?.activityIndicatorCount()?.let {
                            ps.onNext(it)
                        } ?: ps.onError(Exception())

                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun getProjectBacking(slug: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()

            this.service.query(
                GetProjectBackingQuery.builder()
                    .slug(slug)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectBackingQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectBackingQuery.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        } else {
                            response.data?.let { data ->
                                data.project()?.backing()?.fragments()?.backing()?.let { backingObj ->
                                    val backing = backingTransformer(
                                        backingObj
                                    )

                                    ps.onNext(backing)
                                    ps.onComplete()
                                }
                            }
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
        return Observable.defer {
            val ps = PublishSubject.create<CheckoutPayment>()

            this.service.mutate(
                CreateCheckoutMutation.builder()
                    .projectId(encodeRelayId(createCheckoutData.project))
                    .amount(createCheckoutData.amount)
                    .rewardIds(
                        createCheckoutData.rewardsIds?.let { list ->
                            list.map { encodeRelayId(it) }
                        }
                    )
                    .locationId(createCheckoutData.locationId)
                    .refParam(createCheckoutData.refTag?.tag())
                    .build()
            ).enqueue(object : ApolloCall.Callback<CreateCheckoutMutation.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<CreateCheckoutMutation.Data>) {
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.createCheckout()?.checkout()?.let { checkoutObj ->
                                decodeRelayId(checkoutObj.id())?.let { id ->
                                    val checkout = CheckoutPayment(
                                        id,
                                        checkoutObj.paymentUrl()
                                    )
                                    ps.onNext(checkout)
                                    ps.onComplete()
                                } ?: ps.onError(Exception("CreateCheckout could not decode ID"))
                            }
                        }
                    }
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String> {
        return Observable.defer {
            val ps = PublishSubject.create<String>()

            this.service.mutate(
                CreatePaymentIntentMutation.builder()
                    .projectId(encodeRelayId(createPaymentIntentInput.project))
                    .amountDollars(createPaymentIntentInput.amountDollars)
                    .build()
            ).enqueue(object : ApolloCall.Callback<CreatePaymentIntentMutation.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<CreatePaymentIntentMutation.Data>) {
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        ps.onNext(response.data?.createPaymentIntent()?.clientSecret() ?: "")
                    }
                    ps.onComplete()
                }
            })
            return@defer ps
        }
    }
}
