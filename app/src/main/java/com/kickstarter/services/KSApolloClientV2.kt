package com.kickstarter.services

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.projectTransformer
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import type.FlaggingKind
import type.TriggerThirdPartyEventInput

interface ApolloClientTypeV2 {
    fun getProject(project: Project): Observable<Project>
    fun getProject(slug: String): Observable<Project>
    fun createSetupIntent(project: Project? = null): Observable<String>
    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>
    fun getStoredCards(): Observable<List<StoredCard>>
    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>
    fun createFlagging(project: Project? = null, details: String, flaggingKind: String): Observable<String>
    fun userPrivacy(): Observable<UserPrivacyQuery.Data>
    fun watchProject(project: Project): Observable<Project>
    fun unWatchProject(project: Project): Observable<Project>
    fun triggerThirdPartyEvent(triggerThirdPartyEventInput: TriggerThirdPartyEventInput): Observable<TriggerThirdPartyEventMutation.Data>
    fun updateUserPassword(currentPassword: String = "", newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data>
    fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data>
    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>
}

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
                            ps.onComplete()
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

                        response.data?.let { ps.onNext(it) }
                        ps.onComplete()
                    }
                })
            return@defer ps
        }
    }

    override fun createFlagging(project: Project?, details: String, flaggingKind: String): Observable<String> {
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

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacyQuery.Data>()
            service.query(UserPrivacyQuery.builder().build())
                .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                        response.data?.let {
                            ps.onNext(it)
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
    override fun triggerThirdPartyEvent(triggerThirdPartyEventInput: TriggerThirdPartyEventInput): Observable<TriggerThirdPartyEventMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<TriggerThirdPartyEventMutation.Data>()
            service.mutate(TriggerThirdPartyEventMutation.builder().triggerThirdPartyEventInput(triggerThirdPartyEventInput).build())
                .enqueue(object : ApolloCall.Callback<TriggerThirdPartyEventMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<TriggerThirdPartyEventMutation.Data>) {
                        response.data?.let {
                            ps.onNext(it)
                        }
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
}
