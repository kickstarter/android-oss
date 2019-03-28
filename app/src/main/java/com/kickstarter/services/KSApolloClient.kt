package com.kickstarter.services

import CreatePasswordMutation
import DeletePaymentSourceMutation
import SavePaymentMethodMutation
import SendEmailVerificationMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.kickstarter.models.StoredCard
import rx.Observable
import rx.subjects.PublishSubject
import type.CurrencyCode
import type.PaymentTypes

class KSApolloClient(val service: ApolloClient) : ApolloClientType {
    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
        return Observable.defer{
            val ps = PublishSubject.create<CreatePasswordMutation.Data>()
        service.mutate(CreatePasswordMutation.builder()
                .password(password)
                .passwordConfirmation(confirmPassword)
                .build())
                .enqueue(object : ApolloCall.Callback<CreatePasswordMutation.Data>() {
                    override fun onFailure(exception: ApolloException) {
                        ps.onError(exception)
                    }

                    override fun onResponse(response: Response<CreatePasswordMutation.Data>) {
                        if (response.hasErrors()) {
                            ps.onError(java.lang.Exception(response.errors().first().message()))
                        }
                        ps.onNext(response.data())
                        ps.onCompleted()
                    }

                })
            return@defer ps
        }
    }


    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<DeletePaymentSourceMutation.Data>()
            service.mutate(DeletePaymentSourceMutation.builder()
                    .paymentSourceId(paymentSourceId)
                    .build())
                    .enqueue(object : ApolloCall.Callback<DeletePaymentSourceMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<DeletePaymentSourceMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
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
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            Observable.just(response.data())
                                    .map { cards -> cards?.me()?.storedCards()?.nodes() }
                                    .flatMap { list ->
                                        Observable.from(list).map {
                                            StoredCard.builder()
                                                    .expiration(it.expirationDate())
                                                    .id(it.id() ?: "")
                                                    .lastFourDigits(it.lastFour())
                                                    .type(it.type())
                                                    .build()
                                        }
                                                .toList()
                                    }
                                    .subscribe {
                                        ps.onNext(it)
                                        ps.onCompleted()
                                    }
                        }
                    })
            return@defer ps
        }
    }

    override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String): Observable<SavePaymentMethodMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<SavePaymentMethodMutation.Data>()
            service.mutate(SavePaymentMethodMutation.builder()
                    .paymentType(paymentTypes)
                    .stripeToken(stripeToken)
                    .stripeCardId(cardId)
                    .build())
                    .enqueue(object : ApolloCall.Callback<SavePaymentMethodMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<SavePaymentMethodMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            //why wouldn't this just be an error?
                            val createPaymentSource = response.data()?.createPaymentSource()
                            if (!createPaymentSource?.isSuccessful!!) {
                                ps.onError(Exception(createPaymentSource.errorMessage()))
                            } else {
                                ps.onNext(response.data())
                                ps.onCompleted()
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<SendEmailVerificationMutation.Data>()
            service.mutate(SendEmailVerificationMutation.builder()
                    .build())
                    .enqueue(object : ApolloCall.Callback<SendEmailVerificationMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<SendEmailVerificationMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserCurrencyMutation.Data>()
            service.mutate(UpdateUserCurrencyMutation.builder()
                    .chosenCurrency(currency)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserCurrencyMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserCurrencyMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserEmailMutation.Data>()
            service.mutate(UpdateUserEmailMutation.builder()
                    .email(email)
                    .currentPassword(currentPassword)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserEmailMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserEmailMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserPasswordMutation.Data>()
            service.mutate(UpdateUserPasswordMutation.builder()
                    .currentPassword(currentPassword)
                    .password(newPassword)
                    .passwordConfirmation(confirmPassword)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserPasswordMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserPasswordMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
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
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }
}
