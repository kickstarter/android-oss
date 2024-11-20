package com.kickstarter.services

import android.util.Pair
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.Gson
import com.kickstarter.CancelBackingMutation
import com.kickstarter.ClearUserUnseenActivityMutation
import com.kickstarter.CompleteOnSessionCheckoutMutation
import com.kickstarter.CompleteOrderMutation
import com.kickstarter.CreateBackingMutation
import com.kickstarter.CreateCheckoutMutation
import com.kickstarter.CreateCommentMutation
import com.kickstarter.CreateFlaggingMutation
import com.kickstarter.CreatePasswordMutation
import com.kickstarter.CreatePaymentIntentMutation
import com.kickstarter.CreateSetupIntentMutation
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.ErroredBackingsQuery
import com.kickstarter.FetchCategoryQuery
import com.kickstarter.FetchProjectQuery
import com.kickstarter.FetchProjectsQuery
import com.kickstarter.GetBackingQuery
import com.kickstarter.GetCommentQuery
import com.kickstarter.GetProjectAddOnsQuery
import com.kickstarter.GetProjectBackingQuery
import com.kickstarter.GetProjectCommentsQuery
import com.kickstarter.GetProjectUpdateCommentsQuery
import com.kickstarter.GetProjectUpdatesQuery
import com.kickstarter.GetRepliesForCommentQuery
import com.kickstarter.GetRootCategoriesQuery
import com.kickstarter.GetShippingRulesForRewardIdQuery
import com.kickstarter.ProjectCreatorDetailsQuery
import com.kickstarter.SavePaymentMethodMutation
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.SendMessageMutation
import com.kickstarter.UnwatchProjectMutation
import com.kickstarter.UpdateBackingMutation
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.UserPaymentsQuery
import com.kickstarter.UserPrivacyQuery
import com.kickstarter.ValidateCheckoutQuery
import com.kickstarter.WatchProjectMutation
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.toBoolean
import com.kickstarter.libs.utils.extensions.toProjectSort
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Comment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.services.transformers.backingTransformer
import com.kickstarter.services.transformers.categoryTransformer
import com.kickstarter.services.transformers.commentTransformer
import com.kickstarter.services.transformers.complexRewardItemsTransformer
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.getCreateAttributionEventMutation
import com.kickstarter.services.transformers.getCreateOrUpdateBackingAddressMutation
import com.kickstarter.services.transformers.getPledgedProjectsOverviewQuery
import com.kickstarter.services.transformers.pledgedProjectsOverviewEnvelopeTransformer
import com.kickstarter.services.transformers.projectTransformer
import com.kickstarter.services.transformers.rewardTransformer
import com.kickstarter.services.transformers.shippingRulesListTransformer
import com.kickstarter.services.transformers.updateTransformer
import com.kickstarter.services.transformers.userPrivacyTransformer
import com.kickstarter.type.BackingState
import com.kickstarter.type.CurrencyCode
import com.kickstarter.type.NonDeprecatedFlaggingKind
import com.kickstarter.type.PaymentTypes
import com.kickstarter.type.StripeIntentContextTypes
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable
import java.nio.charset.Charset

interface ApolloClientTypeV2 {
    fun getProject(project: Project): Observable<Project>
    fun getProject(slug: String): Observable<Project>
    fun getProjects(discoveryParams: DiscoveryParams, slug: String?): Observable<DiscoverEnvelope>
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

    fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse>

    fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>>

    fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean>
    fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean>
    fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload>
    fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope>
}

private const val PAGE_SIZE = 25
private const val REPLIES_PAGE_SIZE = 7

class KSApolloClientV2(val service: ApolloClient, val gson: Gson) : ApolloClientTypeV2 {
    override fun getProject(project: Project): Observable<Project> {
        return getProject(project.slug() ?: "")
    }

    override fun getProject(slug: String): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            val query = FetchProjectQuery(slug)
            this.service.query(
                query
            ).toFlow()
                .catch { throwable ->
                    ps.onError(throwable)
                }
                .map { response ->
                    if (response.hasErrors()) {
                        if (response.hasErrors()) ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            ps.onNext(
                                projectTransformer(
                                    responseData.project?.fullProject
                                )
                            )
                        }
                        ps.onComplete()
                    }
                }
                .asObservable()
                .subscribe()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getProjects(
        discoveryParams: DiscoveryParams,
        slug: String?
    ): Observable<DiscoverEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<DiscoverEnvelope>()
            this.service.query(
                buildFetchProjectsQuery(discoveryParams, slug)
            ).toFlow()
                .catch { throwable ->
                    ps.onError(throwable)
                }
                .map { response ->
                    response.data?.let { responseData ->
                        val projects = responseData.projects?.edges?.map {
                            projectTransformer(it?.node?.projectCard)
                        }
                        val pageInfoEnvelope =
                            responseData.projects?.pageInfo?.pageInfo?.let {
                                createPageInfoObject(it)
                            }
                        val discoverEnvelope = DiscoverEnvelope.builder()
                            .projects(projects)
                            .pageInfoEnvelope(pageInfoEnvelope)
                            .build()
                        ps.onNext(discoverEnvelope)
                    }
                    ps.onComplete()
                }
                .asObservable()
                .subscribe()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun buildFetchProjectsQuery(
        discoveryParams: DiscoveryParams,
        slug: String?
    ): FetchProjectsQuery {
        // TODO: improve nullability here
        return FetchProjectsQuery(
            sort = Optional.present(discoveryParams.sort()?.toProjectSort()),
            cursor = Optional.present(slug),
            categoryId = Optional.present(discoveryParams.category()?.id().toString()),
            recommended = Optional.present(discoveryParams.recommended()),
            starred = Optional.present(discoveryParams.starred().toBoolean()),
            backed = Optional.present(discoveryParams.backed().toBoolean())
        )
    }

    override fun createSetupIntent(project: Project?): Observable<String> {
        return Observable.defer {
            val ps = PublishSubject.create<String>()
            project?.let { proj ->
                val mutation = CreateSetupIntentMutation(
                    projectId = Optional.present(encodeRelayId(proj)),
                    setupIntentContext = Optional.present(StripeIntentContextTypes.CROWDFUNDING_CHECKOUT)
                )

                this.service.mutation(mutation)
                    .toFlow()
                    .catch { throwable ->
                        ps.onError(throwable)
                    }
                    .map { response ->
                        if (response.hasErrors())
                            ps.onError(java.lang.Exception(response.errors?.first()?.message))
                        else {
                            ps.onNext(response.data?.createSetupIntent?.clientSecret ?: "")
                        }
                        ps.onComplete()
                    }
                    .asObservable()
                    .subscribe()
            }
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.defer {
            val ps = PublishSubject.create<StoredCard>()
            val mutation = SavePaymentMethodMutation(
                paymentType = Optional.present(savePaymentMethodData.paymentType),
                stripeToken = Optional.present(savePaymentMethodData.stripeToken),
                stripeCardId = Optional.present(savePaymentMethodData.stripeCardId),
                reusable = Optional.present(savePaymentMethodData.reusable),
                intentClientSecret = Optional.present(savePaymentMethodData.intentClientSecret)
            )
            service.mutation(
                mutation
            )
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }

                    val paymentSource = response.data?.createPaymentSource?.paymentSource
                    paymentSource?.let {
                        // TODO: review the type for dates probably the Custom mapping requires some additions here
                        val storedCard = StoredCard.builder()
                            .expiration(it.expirationDate as java.util.Date?)
                            .id(it.id)
                            .lastFourDigits(it.lastFour)
                            .type(it.type)
                            .build()
                        ps.onNext(storedCard)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<StoredCard>>()

            val query = UserPaymentsQuery()
            this.service
                .query(query)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        val cardsList = mutableListOf<StoredCard>()
                        response.data?.me?.storedCards?.nodes?.map {
                            it?.let { cardData ->
                                // TODO: review the type for dates probably the Custom mapping requires some additions here
                                val card = StoredCard.builder()
                                    .expiration(cardData.expirationDate as java.util.Date?)
                                    .id(cardData.id)
                                    .lastFourDigits(cardData.lastFour)
                                    .type(it.type)
                                    .stripeCardId(it.stripeCardId)
                                    .build()
                                cardsList.add(card)
                            }
                        }
                        ps.onNext(cardsList)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<DeletePaymentSourceMutation.Data>()
            val mutation = DeletePaymentSourceMutation(
                paymentSourceId = paymentSourceId,
            )

            service.mutation(
                mutation
            )
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }

                    response.data?.let { ps.onNext(it) }
                    ps.onComplete()
                }.dispose()
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
                val flagging = NonDeprecatedFlaggingKind.safeValueOf(flaggingKind)
                val mutation = CreateFlaggingMutation(
                    contentId = encodeRelayId(it),
                    details = Optional.present(details),
                    kind = flagging
                )

                service.mutation(
                    mutation
                ).toFlow()
                    .asObservable()
                    .doOnError { throwable ->
                        ps.onError(throwable)
                    }
                    .subscribe { response ->
                        if (response.hasErrors()) {
                            ps.onError(Exception(response.errors?.first()?.message))
                        }
                        response.data?.let { data ->
                            data.createFlagging?.flagging?.kind?.name?.let { kindString ->
                                ps.onNext(kindString)
                            }
                        }
                        ps.onComplete()
                    }.dispose()
                return@defer ps
            }
        }
    }

    override fun userPrivacy(): Observable<UserPrivacy> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacy>()
            val query = UserPrivacyQuery()
            service.query(
                query = query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    response.data?.me?.let {
                        ps.onNext(userPrivacyTransformer(it))
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun watchProject(project: Project): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            val mutation = WatchProjectMutation(
                id = encodeRelayId(project)
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }

                    /* make a copy of what you posted. just in case
                     * we want to update the list without doing
                     * a full refresh.
                     */
                    ps.onNext(
                        projectTransformer(
                            response.data?.watchProject?.project?.fullProject
                        )
                    )
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun unWatchProject(project: Project): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            val mutation = UnwatchProjectMutation(
                id = encodeRelayId(project)
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    /* make a copy of what you posted. just in case
                     * we want to update the list without doing
                     * a full refresh.
                     */
                    ps.onNext(
                        projectTransformer(
                            response.data?.watchProject?.project?.fullProject
                        )
                    )
                    ps.onComplete()
                }.dispose()
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
            val mutation = UpdateUserPasswordMutation(
                currentPassword = currentPassword,
                password = newPassword,
                passwordConfirmation = confirmPassword

            )
            service.mutation(mutation)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let {
                        ps.onNext(it)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserEmailMutation.Data>()
            val mutation = UpdateUserEmailMutation(
                email = email,
                currentPassword = currentPassword
            )
            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        ps.onNext(data)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<SendEmailVerificationMutation.Data>()
            val mutation = SendEmailVerificationMutation()
            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        ps.onNext(data)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserCurrencyMutation.Data>()
            val mutation = UpdateUserCurrencyMutation(
                chosenCurrency = currency
            )
            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let {
                        ps.onNext(it)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<ShippingRulesEnvelope>()

            val query = GetShippingRulesForRewardIdQuery(
                rewardId = encodeRelayId(reward)
            )
            this.service
                .query(query)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }

                    response.data?.let { data ->
                        Observable.just(data.node)
                            .map { it.onReward }
                            .filter { !it.shippingRulesExpanded?.nodes.isNullOrEmpty() }
                            .map {
                                it.shippingRulesExpanded?.nodes?.mapNotNull { node ->
                                    node?.shippingRule
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
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun getAddOnsFromProject(addOnsGr: GetProjectAddOnsQuery.AddOns): List<Reward> {
        // TODO: Review the nulabillity of all of these pieces
        return addOnsGr.nodes?.map { node ->
            val shippingRulesGr =
                node?.shippingRulesExpanded?.nodes?.map { requireNotNull(it?.shippingRule) }
                    ?: emptyList()
            rewardTransformer(
                requireNotNull(node?.reward),
                shippingRulesGr,
                addOnItems = complexRewardItemsTransformer(node?.items?.rewardItems)
            )
        }?.toList() ?: emptyList()
    }

    override fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<Reward>>()

            val query = GetProjectAddOnsQuery(
                slug = slug,
                locationId = encodeRelayId(locationId)
            )

            this.service
                .query(query)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        val addOns = getAddOnsFromProject(requireNotNull(data.project?.addOns))
                        ps.onNext(addOns)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
        return Observable.defer {
            // TODO: Review nullability here for updateBacking mutation

            val mutation = UpdateBackingMutation(
                backingId = encodeRelayId(updateBackingData.backing),
                amount = Optional.present(updateBackingData.amount),
                locationId = Optional.present(updateBackingData.locationId),
                rewardIds = Optional.present(updateBackingData.rewardsIds?.let { list -> list.map { encodeRelayId(it) } }),
                paymentSourceId = Optional.present(updateBackingData.paymentSourceId),
                intentClientSecret = Optional.present(updateBackingData.intentClientSecret)
            )
            val ps = PublishSubject.create<Checkout>()
            service
                .mutation(mutation)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    } else {
                        val checkoutPayload = response.data?.updateBacking?.checkout
                        val backing = Checkout.Backing.builder()
                            .clientSecret(
                                checkoutPayload?.backing?.checkoutBacking?.clientSecret
                            )
                            .requiresAction(
                                checkoutPayload?.backing?.checkoutBacking?.requiresAction ?: false
                            )
                            .build()

                        val checkout = Checkout.builder()
                            .id(decodeRelayId(checkoutPayload?.id))
                            .backing(backing)
                            .build()
                        ps.onNext(checkout)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
        return Observable.defer {
            // TODO: Review nullability for this mutation

            val ps = PublishSubject.create<Checkout>()
            val mutation = CreateBackingMutation(
                projectId = encodeRelayId(createBackingData.project),
                amount = createBackingData.amount,
                paymentType = PaymentTypes.CREDIT_CARD.rawValue,
                paymentSourceId = Optional.present(createBackingData.paymentSourceId),
                setupIntentClientSecret = Optional.present(createBackingData.setupIntentClientSecret),
                locationId = Optional.present(createBackingData.locationId),
                rewardIds = Optional.present(createBackingData.rewardsIds?.let { list -> list.map { encodeRelayId(it) } }),
                refParam = Optional.present(createBackingData.refTag?.tag())
            )

            this.service.mutation(mutation)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    } else {

                        val checkoutPayload = response.data?.createBacking?.checkout

                        // TODO: Add new status field to backing model
                        val backing = Checkout.Backing.builder()
                            .clientSecret(
                                checkoutPayload?.backing?.checkoutBacking?.clientSecret
                            )
                            .requiresAction(
                                checkoutPayload?.backing?.checkoutBacking
                                    ?.requiresAction ?: false
                            )
                            .build()

                        val checkout = Checkout.builder()
                            .id(decodeRelayId(checkoutPayload?.id))
                            .backing(backing)
                            .build()
                        ps.onNext(checkout)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
        return Observable.defer {
            val ps = PublishSubject.create<Pair<Boolean, String>>()
// TODO: rewrite this query on the thirdPartyEvents.graphQL file, something is off here
//            val mutation = getTriggerThirdPartyEventMutation(eventInput)
//
//            service.mutate(mutation)
//                .enqueue(object : ApolloCall.Callback<TriggerThirdPartyEventMutation.Data>() {
//                    override fun onFailure(exception: ApolloException) {
//                        ps.onError(exception)
//                    }
//
//                    override fun onResponse(response: Response<TriggerThirdPartyEventMutation.Data>) {
//                        if (response.hasErrors()) {
//                            ps.onError(Exception(response.errors?.first()?.message ?: ""))
//                        }
//
//                        response.data?.let {
//                            val message = it.triggerThirdPartyEvent()?.message() ?: ""
//                            val isSuccess = it.triggerThirdPartyEvent()?.success() ?: false
//                            ps.onNext(Pair(isSuccess, message))
//                        }
//                        ps.onComplete()
//                    }
//                })
            return@defer ps
        }
    }

    override fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatePasswordMutation.Data>()
            val mutation = CreatePasswordMutation(
                password = password,
                passwordConfirmation = confirmPassword
            )
            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    response.data?.let {
                        ps.onNext(it)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun creatorDetails(slug: String): Observable<CreatorDetails> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatorDetails>()

            val query = ProjectCreatorDetailsQuery(
                slug = slug
            )
            service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }

                    response.data?.project?.creator?.let {
                        ps.onNext(
                            CreatorDetails.builder()
                                .backingsCount(it.backingsCount)
                                .launchedProjectsCount(it.launchedProjects?.totalCount ?: 1)
                                .build()
                        )
                        ps.onComplete()
                    }
                }.dispose()
            return@defer ps
        }
    }

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.defer {
            val ps = PublishSubject.create<Long>()
            val mutation = SendMessageMutation(
                projectId = encodeRelayId(project),
                recipientId = encodeRelayId(recipient),
                body = body
            )

            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }

                    response.data?.let {
                        decodeRelayId(
                            response.data?.sendMessage?.conversation?.id
                        )?.let {
                            ps.onNext(it)
                        } ?: ps.onError(Exception())
                    }

                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.defer {
            val ps = PublishSubject.create<Any>()
            val mutation = CancelBackingMutation(
                backingId = encodeRelayId(backing),
                note = Optional.present(note)
            )
            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onNext(Exception(response.errors?.first()?.message ?: ""))
                    } else {
                        val state = response.data?.cancelBacking?.backing?.status
                        val success = state == BackingState.canceled
                        ps.onNext(success)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun fetchCategory(categoryParam: String): Observable<Category?> {
        return Observable.defer {
            val ps = PublishSubject.create<Category>()
            val query = FetchCategoryQuery(
                categoryParam = categoryParam
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        val category =
                            categoryTransformer(response.data?.category?.category)
                        ps.onNext(category)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getBacking(backingId: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()
            val query = GetBackingQuery(
                backingId = backingId
            )
            this.service.query(
                query
            ).toFlow()
                .catch { throwable ->
                    ps.onError(throwable)
                }
                .map { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let {
                            it.backing?.let { backingFragments ->
                                backingTransformer(
                                    backingFragments.backing
                                )?.let { backingObject ->
                                    ps.onNext(backingObject)
                                }
                            }
                        }
                        ps.onComplete()
                    }
                }
                .asObservable()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun fetchCategories(): Observable<List<Category>> {
        return Observable.defer {
            val query = GetRootCategoriesQuery()
            val ps = PublishSubject.create<List<Category>>()

            service.query(
                query
            )
                .toFlow()
                .map { response: ApolloResponse<GetRootCategoriesQuery.Data> ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            val subCategories = responseData.rootCategories
                                .flatMap { it.subcategories?.nodes.orEmpty() }
                                .map {
                                    categoryTransformer(it?.category)
                                }
                            val rootCategories = responseData.rootCategories
                                .map { categoryTransformer(it.category) }
                                .toMutableList()
                                .apply {
                                    addAll(subCategories)
                                }
                            ps.onNext(rootCategories)
                        }
                        ps.onComplete()
                    }
                }
                .catch { throwable ->
                    ps.onError(throwable)
                }
                .asObservable()
                .subscribe()
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

            val query = GetProjectUpdatesQuery(
                cursor = Optional.present(cursor),
                slug = slug,
                limit = limit
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    response.data?.let { data ->
                        // TODO: Remove this observable it does not makes sense
                        Observable.just(data.project)
                            .filter { it?.posts != null }
                            .map { project ->

                                val updates = project?.posts?.edges?.map { edge ->
                                    updateTransformer(
                                        edge?.node?.post
                                    ).toBuilder()
                                        .build()
                                }

                                UpdatesGraphQlEnvelope.builder()
                                    .updates(updates)
                                    .totalCount(project?.posts?.totalCount ?: 0)
                                    .pageInfoEnvelope(
                                        createPageInfoObject(
                                            project?.posts?.pageInfo?.pageInfo
                                        )
                                    )
                                    .build()
                            }
                            .filter { it.isNotNull() }
                            .subscribe {
                                ps.onNext(it)
                                ps.onComplete()
                            }.dispose()
                    }
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun createPageInfoObject(pageFr: com.kickstarter.fragment.PageInfo?): PageInfoEnvelope {
        return PageInfoEnvelope.builder()
            .endCursor(pageFr?.endCursor ?: "")
            .hasNextPage(pageFr?.hasNextPage ?: false)
            .hasPreviousPage(pageFr?.hasPreviousPage ?: false)
            .startCursor(pageFr?.startCursor ?: "")
            .build()
    }

    override fun getComment(commentableId: String): Observable<Comment> {
        return Observable.defer {
            val ps = PublishSubject.create<Comment>()
            val query = GetCommentQuery(
                commentableId = commentableId
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            val comment = mapGetCommentQueryResponseToComment(responseData)
                            ps.onNext(comment)
                        }
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun mapGetCommentQueryResponseToComment(responseData: GetCommentQuery.Data): Comment {
        val commentFragment = responseData.commentable?.onComment?.comment
        return commentTransformer(commentFragment)
    }

    override fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()

            val query = GetProjectUpdateCommentsQuery(
                cursor = Optional.present(cursor), // TODO: Review was this before -> .cursor(cursor.ifEmpty { null })
                id = updateId,
                limit = limit
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.post?.freeformPost?.comments
                                ?.let { graphComments ->
                                    val comments = graphComments.edges?.map { edge ->
                                        commentTransformer(edge?.node?.comment)
                                            .toBuilder()
                                            .cursor(edge?.cursor)
                                            .build()
                                    }

                                    val envelope = CommentEnvelope.builder()
                                        .comments(comments)
                                        .commentableId(data.post?.id ?: "")
                                        .totalCount(
                                            data.post?.freeformPost?.comments?.totalCount ?: 0
                                        )
                                        .pageInfoEnvelope(
                                            createPageInfoObject(
                                                data.post?.freeformPost?.comments?.pageInfo?.pageInfo
                                            )
                                        )
                                        .build()
                                    ps.onNext(envelope)
                                }
                        }
                    }
                    ps.onComplete()
                }.dispose()
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
            val query = GetProjectCommentsQuery(
                cursor = Optional.present(cursor), // TODO: review! before it was -> cursor.ifEmpty { null }
                slug = slug,
                limit = limit
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.project?.comments?.let { graphComments ->
                                val comments = graphComments.edges?.map { edge ->
                                    commentTransformer(
                                        edge?.node?.comment
                                    ).toBuilder()
                                        .cursor(edge?.cursor)
                                        .build()
                                }

                                val envelope = CommentEnvelope.builder()
                                    .commentableId(data.project?.id)
                                    .comments(comments)
                                    .totalCount(data.project?.comments?.totalCount ?: 0)
                                    .pageInfoEnvelope(
                                        createPageInfoObject(
                                            data.project?.comments?.pageInfo?.pageInfo
                                        )
                                    )
                                    .build()
                                ps.onNext(envelope)
                            }
                        }
                    }
                    ps.onComplete()
                }.dispose()
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
            val query = GetRepliesForCommentQuery(
                commentableId = encodeRelayId(comment),
                cursor = Optional.present(cursor),
                pageSize = Optional.present(pageSize),
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
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
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun createCommentEnvelop(responseData: GetRepliesForCommentQuery.Data): CommentEnvelope {
        val replies = responseData.commentable?.onComment?.replies
        val listOfComments = replies?.nodes?.map { commentFragment ->
            commentTransformer(commentFragment?.comment)
        } ?: emptyList()
        val totalCount = replies?.totalCount ?: 0
        val pageInfo = createPageInfoObject(replies?.pageInfo?.pageInfo)

        return CommentEnvelope.builder()
            .comments(listOfComments)
            .pageInfoEnvelope(pageInfo)
            .totalCount(totalCount)
            .build()
    }

    override fun createComment(comment: PostCommentData): Observable<Comment> {
        return Observable.defer {
            val ps = PublishSubject.create<Comment>()
            val mutation = CreateCommentMutation(
                parentId = Optional.present(comment.parent?.let { encodeRelayId(it) }),
                commentableId = comment.commentableId,
                clientMutationId = Optional.present(comment.clientMutationId),
                body = comment.body
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    } else {
                        /* make a copy of what you posted. just in case
                     * we want to update the list without doing
                     * a full refresh.
                     */
                        ps.onNext(
                            commentTransformer(
                                response.data?.createComment?.comment?.comment
                            )
                        )
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun erroredBackings(): Observable<List<ErroredBacking>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<ErroredBacking>>()
            val query = ErroredBackingsQuery()
            this.service
                .query(query)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        val erroredBackings = response.data?.me?.backings?.nodes?.map {
                            val project = ErroredBacking.Project.builder()
                                .finalCollectionDate(it?.project?.finalCollectionDate as org.joda.time.DateTime) // TODO: Dates stuff
                                .name(it.project?.name)
                                .slug(it.project?.slug)
                                .build()
                            return@map ErroredBacking.builder()
                                .project(project)
                                .build()
                        } ?: listOf()
                        ps.onNext(erroredBackings)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun clearUnseenActivity(): Observable<Int> {
        return Observable.defer {
            val ps = PublishSubject.create<Int>()
            val mutation = ClearUserUnseenActivityMutation()
            service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    response.data?.clearUserUnseenActivity?.activityIndicatorCount?.let {
                        ps.onNext(it)
                    } ?: ps.onError(Exception())

                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun getProjectBacking(slug: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()

            val query = GetProjectBackingQuery(
                slug = slug
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.project?.backing?.backing?.let { backingObj ->
                                val backing = backingTransformer(
                                    backingObj
                                )

                                ps.onNext(backing)
                                ps.onComplete()
                            }
                        }
                    }
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
        return Observable.defer {
            val ps = PublishSubject.create<CheckoutPayment>()

            val mutation = CreateCheckoutMutation(
                projectId = encodeRelayId(createCheckoutData.project),
                amount = createCheckoutData.amount,
                rewardIds = Optional.present(
                    createCheckoutData.rewardsIds?.let { list ->
                        list.map { encodeRelayId(it) }
                    }
                ),
                locationId = Optional.present(createCheckoutData.locationId),
                refParam = Optional.present(createCheckoutData.refTag?.tag())
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.createCheckout?.checkout?.let { checkoutObj ->
                                val backingId = decodeRelayId(checkoutObj.id) ?: 0L
                                val backing = Backing.builder().id(backingId).build()
                                decodeRelayId(checkoutObj.id)?.let { id ->
                                    val checkout = CheckoutPayment(
                                        id,
                                        checkoutObj.paymentUrl,
                                        backing = backing
                                    )
                                    ps.onNext(checkout)
                                } ?: ps.onError(Exception("CreateCheckout could not decode ID"))
                            }
                        }
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String> {
        return Observable.defer {
            val ps = PublishSubject.create<String>()

            val checkoutId = createPaymentIntentInput.checkoutId
            val backingId = encodeRelayId(createPaymentIntentInput.backing)
            val mutation = CreatePaymentIntentMutation(
                projectId = encodeRelayId(createPaymentIntentInput.project),
                amount = createPaymentIntentInput.amount,
                paymentIntentContext = Optional.present(StripeIntentContextTypes.POST_CAMPAIGN_CHECKOUT),
                checkoutId = (Base64Utils.encodeUrlSafe(("Checkout-$checkoutId").toByteArray(Charset.defaultCharset()))),
                backingId = Optional.present(backingId)
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.createPaymentIntent?.clientSecret?.let {
                            ps.onNext(it)
                        } ?: ps.onError(Exception("Client Secret was Null"))
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse> {
        return Observable.defer {
            val ps = PublishSubject.create<PaymentValidationResponse>()
            val query = ValidateCheckoutQuery(
                checkoutId = checkoutId,
                paymentIntentClientSecret = paymentIntentClientSecret,
                paymentSourceId = paymentSourceId
            )
            this.service.query(
                query
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            val validation = PaymentValidationResponse(
                                data.checkout?.isValidForOnSessionCheckout?.valid ?: false,
                                data.checkout?.isValidForOnSessionCheckout?.messages ?: listOf()
                            )
                            ps.onNext(validation)
                        }
                        ps.onComplete()
                    }
                }.dispose()
            return@defer ps
        }
    }

    override fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>> {
        return Observable.defer {
            val ps = PublishSubject.create<Pair<String, Boolean>>()

            val mutation = CompleteOnSessionCheckoutMutation(
                checkoutId = Base64Utils.encodeUrlSafe(("Checkout-$checkoutId").toByteArray(Charset.defaultCharset())),
                paymentIntentClientSecret = paymentIntentClientSecret,
                paymentSourceId = Optional.present(paymentSourceId),
                paymentSourceReusable = Optional.present(paymentSourceReusable)
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.completeOnSessionCheckout?.checkout?.id?.let { checkoutId ->
                            response.data?.completeOnSessionCheckout?.checkout?.backing?.requiresAction?.let { requiresAction ->
                                ps.onNext(Pair(checkoutId, requiresAction))
                            }
                        } ?: ps.onError(Exception("Checkout ID was null"))
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean> {
        return Observable.defer {
            val ps = PublishSubject.create<Boolean>()

            val mutation = getCreateAttributionEventMutation(eventInput, gson)
            service.mutation(mutation)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }

                    response.data?.let {
                        val isSuccess = it.createAttributionEvent?.successful ?: false
                        ps.onNext(isSuccess)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> {
        return Observable.defer {
            val ps = PublishSubject.create<Boolean>()

            val mutation = getCreateOrUpdateBackingAddressMutation(eventInput)

            service.mutation(mutation)
                .toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }

                    response.data?.let {
                        val isSuccess = it.createOrUpdateBackingAddress?.success ?: false
                        ps.onNext(isSuccess)
                    }
                    ps.onComplete()
                }.dispose()
            return@defer ps
        }
    }

    override fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload> {
        return Observable.defer {
            val ps = PublishSubject.create<CompleteOrderPayload>()

            val mutation = CompleteOrderMutation(
                orderId = "",
                stripePaymentMethodId = Optional.present(orderInput.stripePaymentMethodId),
                paymentSourceReusable = Optional.present(orderInput.paymentSourceReusable)
            )
            this.service.mutation(
                mutation
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }

                    response.data?.completeOrder?.let {
                        val payload = CompleteOrderPayload(
                            status = it.status,
                            clientSecret = it.clientSecret ?: ""
                        )
                        ps.onNext(payload)
                    }
                    ps.onComplete()
                }.dispose()

            return@defer ps
        }
    }

    override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<PledgedProjectsOverviewEnvelope>()

            this.service.query(
                getPledgedProjectsOverviewQuery(inputData)
            ).toFlow()
                .asObservable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }

                    response.data?.let { data ->
                        // TODO: Remove this extra observable
                        Observable.just(data.pledgeProjectsOverview)
                            .filter { it.pledges != null }
                            .map { pledgeProjectsOverview ->
                                pledgedProjectsOverviewEnvelopeTransformer(pledgeProjectsOverview)
                            }
                            .filter { it.isNotNull() }
                            .subscribe {
                                ps.onNext(it)
                                ps.onComplete()
                            }.dispose()
                    }
                }.dispose()
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }
}
