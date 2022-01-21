package com.kickstarter.services

import CancelBackingMutation
import ClearUserUnseenActivityMutation
import CreateBackingMutation
import CreatePasswordMutation
import DeletePaymentSourceMutation
import ErroredBackingsQuery
import FetchProjectsQuery
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
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.toBoolean
import com.kickstarter.libs.utils.extensions.toProjectSort
import com.kickstarter.models.Avatar
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.models.Comment
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.services.transformers.backingTransformer
import com.kickstarter.services.transformers.complexRewardItemsTransformer
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.projectTransformer
import com.kickstarter.services.transformers.rewardTransformer
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import type.BackingState
import type.CurrencyCode
import type.PaymentTypes

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
                                .map { backingObj -> backingTransformer(backingObj?.fragments()?.backing()) }
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

    override fun getProjects(discoveryParams: DiscoveryParams, slug: String?): Observable<DiscoverEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<DiscoverEnvelope>()
            this.service.query(
                buildFetchProjectsQuery(discoveryParams, slug)
            ).enqueue(object : ApolloCall.Callback<FetchProjectsQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ps.onError(e)
                }

                override fun onResponse(response: Response<FetchProjectsQuery.Data>) {
                    response.data?.let { responseData ->
                        val projects = responseData.projects()?.edges()?.map {
                            projectTransformer(it.node()?.fragments()?.projectCard())
                        }
                        val pageInfoEnvelope = responseData.projects()?.pageInfo()?.fragments()?.pageInfo()?.let {
                            createPageInfoObject(it)
                        }
                        val discoverEnvelope = DiscoverEnvelope.builder()
                            .projects(projects)
                            .pageInfoEnvelope(pageInfoEnvelope)
                            .build()
                        Observable.just(discoverEnvelope)
                            .subscribeOn(Schedulers.io())
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

    private fun buildFetchProjectsQuery(discoveryParams: DiscoveryParams, slug: String?): FetchProjectsQuery {
        val query = FetchProjectsQuery.builder()
            .sort(discoveryParams.sort()?.toProjectSort())
            .apply {
                slug?.let { cursor -> this.cursor(cursor) }
                discoveryParams.category()?.id()?.let { id -> this.categoryId(id.toString()) }
                discoveryParams.recommended()?.let { isRecommended -> this.recommended(isRecommended) }
                discoveryParams.starred()?.let { isStarred -> this.starred(isStarred.toBoolean()) }
                discoveryParams.backed()?.let { isBacked -> this.backed(isBacked.toBoolean()) }
                discoveryParams.staffPicks()?.let { isPicked -> this.staffPicks(isPicked) }
            }
            .build()

        return query
    }

    override fun getProjects(isMember: Boolean): Observable<DiscoverEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<DiscoverEnvelope>()
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
                                .map { backingObj -> backingTransformer(backingObj?.fragments()?.backing()) }
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
            rewardTransformer(
                node.fragments().reward(),
                shippingRulesGr,
                addOnItems = complexRewardItemsTransformer(node.items()?.fragments()?.rewardItems())
            )
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
