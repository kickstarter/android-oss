package com.kickstarter.services

import SavePaymentMethodMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import rx.Observable
import rx.subjects.PublishSubject
import type.CurrencyCode
import type.PaymentTypes

class KSApolloClient(val service: ApolloClient) : ApolloClientType {
    override fun getStoredCards(): Observable<UserPaymentsQuery.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPaymentsQuery.Data>()
            service.query(UserPaymentsQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<UserPaymentsQuery.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UserPaymentsQuery.Data>) {
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
