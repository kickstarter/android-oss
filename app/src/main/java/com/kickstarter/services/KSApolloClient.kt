package com.kickstarter.services

import CancelBackingMutation
import ClearUserUnseenActivityMutation
import CreateBackingMutation
import CreatePasswordMutation
import DeletePaymentSourceMutation
import ErroredBackingsQuery
import GetProjectBackingQuery
import ProjectCreatorDetailsQuery
import SavePaymentMethodMutation
import SendEmailVerificationMutation
import SendMessageMutation
import UpdateBackingMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.libs.Permission
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Avatar
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Checkout
import com.kickstarter.models.Comment
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Item
import com.kickstarter.models.Location
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.Relay
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.Video
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import fragment.FullProject
import org.joda.time.DateTime
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import type.BackingState
import type.CollaboratorPermission
import type.CreditCardPaymentType
import type.CurrencyCode
import type.PaymentTypes
import type.RewardType
import type.ShippingPreference
import java.nio.charset.Charset
import kotlin.math.absoluteValue

class KSApolloClient(val service: ApolloClient) : ApolloClientType {

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
                            ps.onNext(response.errors?.first()?.message)
                        } else {
                            val state = response.data?.cancelBacking()?.backing()?.status()
                            val success = state == BackingState.CANCELED
                            ps.onNext(success)
                        }
                        ps.onCompleted()
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
                        }

                        val checkoutPayload = response.data?.createBacking()?.checkout()

                        // TODO: Add new status field to backing model
                        val backing = Checkout.Backing.builder()
                            .clientSecret(checkoutPayload?.backing()?.fragments()?.checkoutBacking()?.clientSecret())
                            .requiresAction(checkoutPayload?.backing()?.fragments()?.checkoutBacking()?.requiresAction() ?: false)
                            .build()

                        ps.onNext(
                            Checkout.builder()
                                .id(decodeRelayId(checkoutPayload?.id()))
                                .backing(backing)
                                .build()
                        )
                        ps.onCompleted()
                    }
                })
            return@defer ps
        }
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
                        response.data?.let { data ->
                            Observable.just(data.backing())
                                .filter { it?.fragments()?.backing() != null }
                                .map { backingObj -> createBackingObject(backingObj?.fragments()?.backing()) }
                                .filter { ObjectUtils.isNotNull(it) }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onCompleted()
                                }
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
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
                        response.data?.clearUserUnseenActivity()?.activityIndicatorCount().let {
                            handleResponse(it, ps)
                        }
                    }
                })
            return@defer ps
        }
    }

    override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()

            this.service.query(
                GetProjectCommentsQuery.builder()
                    .cursor(cursor)
                    .slug(slug)
                    .limit(limit)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectCommentsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectCommentsQuery.Data>) {
                        response.data?.let { data ->
                            Observable.just(data.project())
                                .filter { it?.comments() != null }
                                .map { project ->

                                    val comments = project?.comments()?.edges()?.map { edge ->
                                        createCommentObject(edge?.node()?.fragments()?.comment()).toBuilder()
                                            .cursor(edge?.cursor())
                                            .build()
                                    }

                                    CommentEnvelope.builder()
                                        .commentableId(project?.id())
                                        .comments(comments)
                                        .totalCount(project?.comments()?.totalCount() ?: 0)
                                        .pageInfoEnvelope(createPageInfoObject(project?.comments()?.pageInfo()?.fragments()?.pageInfo()))
                                        .build()
                                }
                                .filter { ObjectUtils.isNotNull(it) }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onCompleted()
                                }
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getProjectUpdateComments(updateId: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()

            this.service.query(
                GetProjectUpdateCommentsQuery.builder()
                    .cursor(cursor)
                    .id(updateId)
                    .limit(limit)
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectUpdateCommentsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectUpdateCommentsQuery.Data>) {
                        response.data?.let { data ->
                            Observable.just(data.post())
                                .filter { it?.fragments()?.freeformPost()?.comments() != null }
                                .map { post ->

                                    val comments = post?.fragments()?.freeformPost()?.comments()?.edges()?.map { edge ->
                                        createCommentObject(edge?.node()?.fragments()?.comment()).toBuilder()
                                            .cursor(edge?.cursor())
                                            .build()
                                    }

                                    CommentEnvelope.builder()
                                        .comments(comments)
                                        .commentableId(post?.id())
                                        .totalCount(post?.fragments()?.freeformPost()?.comments()?.totalCount() ?: 0)
                                        .pageInfoEnvelope(createPageInfoObject(post?.fragments()?.freeformPost()?.comments()?.pageInfo()?.fragments()?.pageInfo()))
                                        .build()
                                }
                                .filter { ObjectUtils.isNotNull(it) }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onCompleted()
                                }
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getRepliesForComment(comment: Comment, cursor: String?, pageSize: Int): Observable<CommentEnvelope> {
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
                    response.data?.let { responseData ->
                        Observable.just(createCommentEnvelop(responseData))
                            .subscribe {
                                ps.onNext(it)
                                ps.onCompleted()
                            }
                    }
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

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
                    response.data?.let { responseData ->
                        Observable.just(projectTransformer(responseData.project()?.fragments()?.fullProject()))
                            .subscribe {
                                ps.onNext(it)
                                ps.onCompleted()
                            }
                    }
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
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
                    response.data?.let { responseData ->
                        Observable.just(mapGetCommentQueryResponseToComment(responseData))
                            .subscribe {
                                ps.onNext(it)
                                ps.onCompleted()
                            }
                    }
                }
            })
            return@defer ps
        }.subscribeOn(Schedulers.io())
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
                        }
                        /* make a copy of what you posted. just in case
                         * we want to update the list without doing
                         * a full refresh.
                         */
                        ps.onNext(
                            createCommentObject(response.data?.createComment()?.comment()?.fragments()?.comment())
                        )
                        ps.onCompleted()
                    }
                })
            return@defer ps
        }
    }

    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
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
                        ps.onNext(response.data)
                        ps.onCompleted()
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
                            ps.onCompleted()
                        }
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
                        ps.onNext(response.data)
                        ps.onCompleted()
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
                                            .finalCollectionDate(it.project()?.finalCollectionDate())
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
                                    ps.onCompleted()
                                }
                        }
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
                        response.data?.let { data ->
                            Observable.just(data.project()?.backing())
                                .filter { it?.fragments()?.backing() != null }
                                .map { backingObj -> createBackingObject(backingObj?.fragments()?.backing()) }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onCompleted()
                                }
                        }
                    }
                })
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    override fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<Reward>>()

            this.service.query(
                GetProjectAddOnsQuery.builder()
                    .slug(slug)
                    .locationId(encodeRelayId(locationId))
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<GetProjectAddOnsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        ps.onError(e)
                    }

                    override fun onResponse(response: Response<GetProjectAddOnsQuery.Data>) {
                        response.data?.let { data ->
                            Observable.just(data.project()?.addOns())
                                .filter { it?.nodes() != null }
                                .map <List<Reward>> { addOnsList -> addOnsList?.let { getAddOnsFromProject(it) } ?: emptyList() }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onCompleted()
                                }
                        }
                    }
                })
            return@defer ps
        }
    }

    private fun getAddOnsFromProject(addOnsGr: GetProjectAddOnsQuery.AddOns): List<Reward> {
        return addOnsGr.nodes()?.map { node ->
            val shippingRulesGr = node.shippingRulesExpanded()?.nodes()?.map { it.fragments().shippingRule() } ?: emptyList()
            rewardTransformer(node.fragments().reward(), shippingRulesGr)
        }?.toList() ?: emptyList()
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
                            Observable.just(response.data)
                                .map { cards -> cards?.me()?.storedCards()?.nodes() }
                                .map { list ->
                                    val storedCards = list?.asSequence()?.map {
                                        StoredCard.builder()
                                            .expiration(it.expirationDate())
                                            .id(it.id())
                                            .lastFourDigits(it.lastFour())
                                            .type(it.type())
                                            .build()
                                    }
                                    storedCards?.toList() ?: listOf()
                                }
                                .subscribe {
                                    ps.onNext(it)
                                    ps.onCompleted()
                                }
                        }
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
                            ps.onCompleted()
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
                        handleResponse(decodeRelayId(response.data?.sendMessage()?.conversation()?.id()), ps)
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
                        ps.onNext(response.data)
                        ps.onCompleted()
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
                .paymentSourceId(updateBackingData.paymentSourceId)
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
                        }

                        val checkoutPayload = response.data?.updateBacking()?.checkout()
                        val backing = Checkout.Backing.builder()
                            .clientSecret(checkoutPayload?.backing()?.fragments()?.checkoutBacking()?.clientSecret())
                            .requiresAction(checkoutPayload?.backing()?.fragments()?.checkoutBacking()?.requiresAction() ?: false)
                            .build()

                        ps.onNext(
                            Checkout.builder()
                                .id(decodeRelayId(checkoutPayload?.id()))
                                .backing(backing)
                                .build()
                        )
                        ps.onCompleted()
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
                        ps.onNext(response.data)
                        ps.onCompleted()
                    }
                })
            return@defer ps
        }
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
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
                        ps.onNext(response.data)
                        ps.onCompleted()
                    }
                })
            return@defer ps
        }
    }

    override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
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
                        ps.onNext(response.data)
                        ps.onCompleted()
                    }
                })
            return@defer ps
        }
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacyQuery.Data>()
            service.query(UserPrivacyQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                        ps.onNext(response.data)
                        ps.onCompleted()
                    }
                })
            return@defer ps
        }
    }
}

private fun createCommentObject(commentFr: fragment.Comment?): Comment {

    val badges: List<String>? = commentFr?.authorBadges()?.map { badge ->
        badge?.rawValue() ?: ""
    }

    val author = User.builder()
        .id(decodeRelayId(commentFr?.author()?.fragments()?.user()?.id()) ?: -1)
        .name(commentFr?.author()?.fragments()?.user()?.name() ?: "")
        .avatar(
            Avatar.builder()
                .medium(commentFr?.author()?.fragments()?.user()?.imageUrl())
                .build()
        )
        .build()

    return Comment.builder()
        .id(decodeRelayId(commentFr?.id()) ?: -1)
        .author(author)
        .repliesCount(commentFr?.replies()?.totalCount() ?: 0)
        .body(commentFr?.body())
        .authorBadges(badges)
        .cursor("")
        .createdAt(commentFr?.createdAt())
        .deleted(commentFr?.deleted())
        .authorCanceledPledge(commentFr?.authorCanceledPledge())
        .parentId(decodeRelayId(commentFr?.parentId()) ?: -1)
        .build()
}

private fun createPageInfoObject(pageFr: fragment.PageInfo?): PageInfoEnvelope {
    return PageInfoEnvelope.builder()
        .endCursor(pageFr?.endCursor() ?: "")
        .hasNextPage(pageFr?.hasNextPage() ?: false)
        .hasPreviousPage(pageFr?.hasPreviousPage() ?: false)
        .startCursor(pageFr?.startCursor() ?: "")
        .build()
}

private fun createCommentEnvelop(responseData: GetRepliesForCommentQuery.Data): CommentEnvelope {
    val replies = (responseData.commentable() as? GetRepliesForCommentQuery.AsComment)?.replies()
    val listOfComments = replies?.nodes()?.map { commentFragment ->
        createCommentObject(commentFragment.fragments().comment())
    } ?: emptyList()
    val totalCount = replies?.totalCount() ?: 0
    val pageInfo = createPageInfoObject(replies?.pageInfo()?.fragments()?.pageInfo())

    return CommentEnvelope.builder()
        .comments(listOfComments)
        .pageInfoEnvelope(pageInfo)
        .totalCount(totalCount)
        .build()
}

private fun mapGetCommentQueryResponseToComment(responseData: GetCommentQuery.Data): Comment {
    val commentFragment = (responseData.commentable() as? GetCommentQuery.AsComment)?.fragments()?.comment()
    return createCommentObject(commentFragment)
}

private fun createBackingObject(backingGr: fragment.Backing?): Backing {
    val payment = backingGr?.paymentSource()?.fragments()?.payment()?.let { payment ->
        Backing.PaymentSource.builder()
            .state(payment.state().toString())
            .type(payment.type().rawValue())
            .paymentType(CreditCardPaymentType.CREDIT_CARD.rawValue())
            .id(payment.id())
            .expirationDate(payment.expirationDate())
            .lastFour(payment.lastFour())
            .build()
    }

    val addOns = backingGr?.addOns()?.let {
        return@let getAddOnsList(it)
    }

    val id = decodeRelayId(backingGr?.id())?.let { it } ?: 0

    val location = backingGr?.location()?.fragments()?.location()
    val locationId = decodeRelayId(location?.id())
    val projectId = decodeRelayId(backingGr?.project()?.fragments()?.project()?.id()) ?: -1
    val shippingAmount = backingGr?.shippingAmount()?.fragments()

    val reward = backingGr?.reward()?.fragments()?.reward()?.let { reward ->
        return@let rewardTransformer(reward)
    }

    val backerData = backingGr?.backer()?.fragments()?.user()
    val nameBacker = backerData?.let { it.name() } ?: ""
    val backerId = decodeRelayId(backerData?.id()) ?: -1
    val avatar = Avatar.builder()
        .medium(backerData?.imageUrl())
        .build()
    val completedByBacker = backingGr?.backerCompleted() ?: false

    val backer = User.builder()
        .id(backerId)
        .name(nameBacker)
        .avatar(avatar)
        .build()
    val status = backingGr?.status()?.rawValue() ?: ""

    return Backing.builder()
        .amount(backingGr?.amount()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0)
        .bonusAmount(backingGr?.bonusAmount()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0)
        .paymentSource(payment)
        .backerId(backerId)
        .backerUrl(backerData?.imageUrl())
        .backerName(nameBacker)
        .backer(backer)
        .id(id)
        .reward(reward)
        .addOns(addOns)
        .rewardId(reward?.id())
        .locationId(locationId)
        .locationName(location?.displayableName())
        .pledgedAt(backingGr?.pledgedOn())
        .projectId(projectId)
        .sequence(backingGr?.sequence()?.toLong() ?: 0)
        .shippingAmount(shippingAmount?.amount()?.amount()?.toFloat() ?: 0f)
        .status(status)
        .cancelable(backingGr?.cancelable() ?: false)
        .completedByBacker(completedByBacker)
        .build()
}

fun decodeRelayId(encodedRelayId: String?): Long? {
    return try {
        String(Base64Utils.decode(encodedRelayId), Charset.defaultCharset())
            .replaceBeforeLast("-", "", "")
            .toLong()
            .absoluteValue
    } catch (e: Exception) {
        null
    }
}

fun <T : Relay> encodeRelayId(relay: T): String {
    val classSimpleName = relay.javaClass.simpleName.replaceFirst("AutoParcel_", "")
    val id = relay.id()
    return Base64Utils.encodeUrlSafe(("$classSimpleName-$id").toByteArray(Charset.defaultCharset()))
}

private fun <T : Any?> handleResponse(it: T, ps: PublishSubject<T>) {
    when {
        ObjectUtils.isNull(it) -> {
            ps.onError(Exception())
        }
        else -> {
            ps.onNext(it)
            ps.onCompleted()
        }
    }
}

/**
 * For addOns we receive this kind of data structure :[D, D, D, D, D, C, E, E]
 * and we need to transform it in : D(5),C(1),E(2)
 */
fun getAddOnsList(addOns: fragment.Backing.AddOns): List<Reward> {

    val rewardsList = addOns.nodes()?.map { node ->
        rewardTransformer(node.fragments().reward())
    }

    val mapHolder = mutableMapOf<Long, Reward>()

    rewardsList?.forEach {
        val q = mapHolder[it.id()]?.quantity() ?: 0
        mapHolder[it.id()] = it.toBuilder().quantity(q + 1).build()
    }

    return mapHolder.values.toList()
}

/**
 * Transform the Project GraphQL data structure into our own Project data model
 * @param fragment.FullProject projectFragment
 * @return Project
 */
private fun projectTransformer(projectFragment: FullProject?): Project {

    val availableCards = projectFragment?.availableCardTypes() ?: emptyList()
    val backersCount = projectFragment?.backersCount() ?: 0
    val blurb = projectFragment?.description() ?: ""
    val backing = if (projectFragment?.backing()?.fragments()?.backing() != null) {
        createBackingObject(projectFragment?.backing()?.fragments()?.backing())
    } else null
    val category = if (projectFragment?.category()?.fragments()?.category() != null) {
        categoryTransformer(projectFragment?.category()?.fragments()?.category())
    } else null
    val commentsCount = projectFragment?.commentsCount() ?: 0
    val country = projectFragment?.country()?.fragments()?.country()?.name() ?: ""
    val createdAt = projectFragment?.createdAt()
    val creator = userTransformer(projectFragment?.creator()?.fragments()?.user())
    val currency = projectFragment?.currency()?.name ?: ""
    val currencySymbol = projectFragment?.goal()?.fragments()?.amount()?.symbol()
    val prelaunchActivated = projectFragment?.prelaunchActivated()
    val featuredAt = projectFragment?.projectOfTheDayAt()
    val friends =
        projectFragment?.friends()?.nodes()?.map { userTransformer(it.fragments().user()) }
            ?: emptyList()
    val fxRate = projectFragment?.fxRate()?.toFloat()
    val deadline = projectFragment?.deadlineAt()
    val goal = projectFragment?.goal()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val id = decodeRelayId(projectFragment?.id()) ?: -1
    val isBacking = projectFragment?.backing()?.fragments()?.backing()?.let { true } ?: false
    val isStarred = projectFragment?.isWatched ?: false
    val launchedAt = projectFragment?.launchedAt()
    val location = locationTransformer(projectFragment?.location()?.fragments()?.location())
    val name = projectFragment?.name()
    val permission = projectFragment?.collaboratorPermissions()?.map {
        when (it) {
            CollaboratorPermission.COMMENT -> Permission.COMMENT
            CollaboratorPermission.EDIT_FAQ -> Permission.EDIT_FAQ
            CollaboratorPermission.EDIT_PROJECT -> Permission.EDIT_PROJECT
            CollaboratorPermission.FULFILLMENT -> Permission.FULFILLMENT
            CollaboratorPermission.POST -> Permission.POST
            CollaboratorPermission.VIEW_PLEDGES -> Permission.VIEW_PLEDGES
            else -> Permission.UNKNOWN
        }
    }
    val pledged = projectFragment?.pledged()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val photoUrl = projectFragment?.fragments()?.full()?.image()?.url()
    val photo = Photo.builder()
        .ed(photoUrl)
        .full(photoUrl)
        .little(photoUrl)
        .med(photoUrl)
        .small(photoUrl)
        .thumb(photoUrl)
        .build()
    val tags = mutableListOf<String>()
    projectFragment?.fragments()?.tagsCreative()?.tags()?.map { tags.add(it.id()) }
    projectFragment?.fragments()?.tagsDiscovery()?.tags()?.map { tags.add(it.id()) }

    val minPledge = projectFragment?.minPledge()?.toDouble() ?: 1.0
    val rewards =
        projectFragment?.rewards()?.nodes()?.map { rewardTransformer(it.fragments().reward()) }

    // - GraphQL does not provide the Reward no reward, we need to add it first
    val modifiedRewards = rewards?.toMutableList()
    modifiedRewards?.add(0, RewardFactory.noReward().toBuilder().minimum(minPledge).build())
    modifiedRewards?.toList()

    val slug = projectFragment?.slug()
    val staffPicked = projectFragment?.isProjectWeLove ?: false
    val state = projectFragment?.state()?.name?.lowercase()
    val stateChangedAt = projectFragment?.stateChangedAt()
    val staticUSDRate = projectFragment?.usdExchangeRate()?.toFloat()
    val usdExchangeRate = projectFragment?.usdExchangeRate()?.toFloat()
    val updatedAt = projectFragment?.posts()?.fragments()?.updates()?.nodes()?.let {
        if (it.isNotEmpty()) return@let it.first()?.updatedAt()
        else null
    }
    val updatesCount = projectFragment?.posts()?.fragments()?.updates()?.nodes()?.size
    val url = projectFragment?.url()
    val urlsWeb = Project.Urls.Web.builder()
        .project(url)
        .rewards("$url/rewards")
        .build()
    val urls = Project.Urls.builder().web(urlsWeb).build()
    val video = if (projectFragment?.video()?.fragments()?.video() != null) {
        videoTransformer(projectFragment?.video()?.fragments()?.video())
    } else null
    val displayPrelaunch = BooleanUtils.negate(projectFragment?.isLaunched ?: false)

    return Project.builder()
        .availableCardTypes(availableCards.map { it.name })
        .backersCount(backersCount)
        .blurb(blurb)
        .backing(backing)
        .category(category)
        .commentsCount(commentsCount)
        .country(country)
        .createdAt(createdAt)
        .creator(creator)
        .currency(currency)
        .currencySymbol(currencySymbol)
        .currentCurrency(currency) // TODO: selected currency can be fetched form the User/Configuration Object
        .currencyTrailingCode(false) // TODO: This field is available on V1 Configuration Object
        .displayPrelaunch(displayPrelaunch)
        .featuredAt(featuredAt)
        .friends(friends)
        .fxRate(fxRate)
        .deadline(deadline)
        .goal(goal)
        .id(id)
        .isBacking(isBacking)
        .isStarred(isStarred)
        .lastUpdatePublishedAt(updatedAt)
        .launchedAt(launchedAt)
        .location(location)
        .name(name)
        .permissions(permission)
        .pledged(pledged)
        .photo(photo) // TODO: now we get the full size for everything same as iOS, but V1 provided several image sizes
        .prelaunchActivated(prelaunchActivated)
        .tags(tags)
        .rewards(modifiedRewards)
        .slug(slug)
        .staffPick(staffPicked)
        .state(state)
        .stateChangedAt(stateChangedAt)
        .staticUsdRate(staticUSDRate)
        .usdExchangeRate(usdExchangeRate)
        .updatedAt(updatedAt)
        // .unreadMessagesCount() TODO: unread messages can be fetched form the User Object
        // .unseenActivityCount() TODO: unseen activity can be fetched form the User Object
        .updatesCount(updatesCount)
        .urls(urls)
        .video(video)
        .build()
}

/**
 * Transform the Video GraphQL data structure into our own Video data model
 * @param fragment.Video video
 * @return Project
 */
private fun videoTransformer(video: fragment.Video?): Video {
    val frame = video?.previewImageUrl()
    val base = video?.videoSources()?.base()?.src()
    val high = video?.videoSources()?.high()?.src()
    val hls = video?.videoSources()?.hls()?.src()

    return Video.builder()
        .base(base)
        .frame(frame)
        .high(high)
        .hls(hls)
        .build()
}

/**
 * Transform the User GraphQL data structure into our own User data model
 * @param fragment.User user
 * @return Project
 */
private fun userTransformer(user: fragment.User?): User {

    val id = decodeRelayId(user?.id()) ?: -1
    val name = user?.name()
    val avatar = Avatar.builder()
        .medium(user?.imageUrl())
        .build()
    val chosenCurrency = user?.chosenCurrency()

    return User.builder()
        .id(id)
        .name(name)
        .avatar(avatar)
        .chosenCurrency(chosenCurrency)
        .build()
}

/**
 * Transform the Category GraphQL data structure into our own Categroy data model
 * @param fragment.Category category
 * @return Project
 */
private fun categoryTransformer(categoryFragment: fragment.Category?): Category {
    val analyticsName = categoryFragment?.analyticsName() ?: ""
    val name = categoryFragment?.name() ?: ""
    val id = decodeRelayId(categoryFragment?.id()) ?: -1
    val slug = categoryFragment?.slug()
    val parentId = decodeRelayId(categoryFragment?.parentCategory()?.id()) ?: -1
    val parentName = categoryFragment?.parentCategory()?.name()
    val parentSlug = categoryFragment?.parentCategory()?.slug()
    val parentAnalyticName = categoryFragment?.parentCategory()?.analyticsName() ?: ""

    val parentCategory = if (parentId > 0) {
        Category.builder()
            .slug(parentSlug)
            .analyticsName(parentAnalyticName)
            .id(parentId)
            .name(parentName)
            .build()
    } else null

    return Category.builder()
        .analyticsName(name)
        .id(id)
        .name(name)
        .slug(slug)
        .parent(parentCategory)
        .parentId(parentId)
        .parentName(parentName)
        .build()
}

/**
 * Transform the Reward GraphQL data structure into our own Reward data model
 * @param fragment.reward rewardGr
 * @return Reward
 */
private fun rewardTransformer(rewardGr: fragment.Reward, shippingRulesExpanded: List<fragment.ShippingRule> = emptyList()): Reward {
    val amount = rewardGr.amount().fragments().amount().amount()?.toDouble() ?: 0.0
    val convertedAmount = rewardGr.convertedAmount().fragments().amount().amount()?.toDouble() ?: 0.0
    val desc = rewardGr.description()
    val title = rewardGr.name()
    val estimatedDelivery = rewardGr.estimatedDeliveryOn()?.let { DateTime(it) }
    val remaining = rewardGr.remainingQuantity()
    val endsAt = rewardGr.endsAt()?.let { DateTime(it) }
    val startsAt = rewardGr.startsAt()?.let { DateTime(it) }
    val rewardId = decodeRelayId(rewardGr.id()) ?: -1
    val available = rewardGr.available()
    val isAddOn = rewardGr.rewardType() == RewardType.ADDON
    val backersCount = rewardGr.backersCount()
    val shippingPreference = when (rewardGr.shippingPreference()) {
        ShippingPreference.NONE -> Reward.ShippingPreference.NONE
        ShippingPreference.RESTRICTED -> Reward.ShippingPreference.RESTRICTED
        ShippingPreference.UNRESTRICTED -> Reward.ShippingPreference.UNRESTRICTED
        else -> Reward.ShippingPreference.UNKNOWN
    }

    val limit = if (isAddOn) chooseLimit(rewardGr.limit(), rewardGr.limitPerBacker())
    else rewardGr.limit()

    val items = rewardGr.items()?.let {
        rewardItemsTransformer(it)
    }

    val shippingRules = shippingRulesExpanded.map {
        shippingRuleTransformer(it)
    }

    return Reward.builder()
        .title(title)
        .convertedMinimum(convertedAmount)
        .minimum(amount)
        .limit(limit)
        .remaining(remaining)
        .endsAt(endsAt)
        .startsAt(startsAt)
        .description(desc)
        .estimatedDeliveryOn(estimatedDelivery)
        .isAddOn(isAddOn)
        .addOnsItems(items)
        .id(rewardId)
        .shippingPreference(shippingPreference.name)
        .shippingPreferenceType(shippingPreference)
        .shippingType(shippingPreference.name)
        .shippingRules(shippingRules)
        .isAvailable(available)
        .backersCount(backersCount)
        .build()
}

/**
 * Choose the available limit being the smallest one, we can have limit by backer available just in add-ons
 * or limit by reward, available in V1 and Graphql and for both add-ons and Rewards
 * @return limit
 */
fun chooseLimit(limitReward: Int?, limitPerBacker: Int?): Int {
    var limit = limitReward?.let { it } ?: -1
    var limitBacker = limitPerBacker?.let { it } ?: -1

    if (limit < 0) limit = limitBacker
    if (limitBacker < 0) limitBacker = limit

    return when (limit <= limitBacker) {
        true -> limit
        else -> limitBacker
    }
}

/**
 * Transform the fragment.ShippingRule GraphQL data structure into our own ShippingRules data model
 * @param fragment.ShippingRule
 * @return ShippingRule
 */
fun shippingRuleTransformer(rule: fragment.ShippingRule): ShippingRule {
    val cost = rule.cost()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val location = rule.location()?.let {
        locationTransformer(it.fragments().location())
    }

    return ShippingRule.builder()
        .cost(cost)
        .location(location)
        .build()
}

/**
 * Transform the fragment.Location GraphQL data structure into our own Location data model
 * @param fragment.Location
 * @return Location
 */
fun locationTransformer(locationGR: fragment.Location?): Location {
    val id = decodeRelayId(locationGR?.id()) ?: -1
    val country = locationGR?.county() ?: ""
    val displayName = locationGR?.displayableName()
    val name = locationGR?.name()

    return Location.builder()
        .id(id)
        .country(country)
        .displayableName(displayName)
        .name(name)
        .build()
}

/**
 * Transform the Reward.Items GraphQL data structure into our own RewardsItems data model
 * @param fragment.Reward.items
 * @return List<RewardItem>
 */
fun rewardItemsTransformer(items: fragment.Reward.Items): List<RewardsItem> {
    val rewardItems = items.edges()?.map { edge ->
        val quantity = edge.quantity()
        val description = edge.node()?.name()
        val hasBackers = edge.node()?.hasBackers() ?: false
        val id = decodeRelayId(edge.node()?.id()) ?: -1
        val projectId = decodeRelayId(edge.node()?.project()?.id()) ?: -1
        val name = edge.node()?.name() ?: ""

        val item = Item.builder()
            .name(name)
            .description(description)
            .id(id)
            .projectId(projectId)
            .build()

        return@map RewardsItem.builder()
            .id(id)
            .itemId(item.id())
            .item(item)
            .rewardId(0) // - Discrepancy between V1 and Graph, the Graph object do not have the rewardID
            .hasBackers(hasBackers)
            .quantity(quantity)
            .build()
    } ?: emptyList()
    return rewardItems.toList()
}
