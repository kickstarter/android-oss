package com.kickstarter.libs.rx.operators

import com.google.gson.Gson
import com.kickstarter.services.ApiException
import com.kickstarter.services.ResponseException
import com.kickstarter.services.apiresponses.ErrorEnvelope
import retrofit2.Response
import rx.Observable
import rx.Subscriber
import kotlin.Exception

/**
 * Takes a [retrofit2.Response], if it's successful send it to [Subscriber.onNext], otherwise
 * attempt to parse the error.
 *
 * Errors that conform to the API's error format are converted into an [ApiException] exception and sent to
 * [Subscriber.onError], otherwise a more generic [ResponseException] is sent to [Subscriber.onError].
 *
 * @param <T> The response type.
</T> */
class ApiErrorOperator<T>(private val gson: Gson?) : Observable.Operator<T, Response<T>> {
    override fun call(subscriber: Subscriber<in T?>): Subscriber<in Response<T>> {
        val gson = gson
        return object : Subscriber<Response<T>?>() {
            override fun onCompleted() {
                if (!subscriber.isUnsubscribed) {
                    subscriber.onCompleted()
                }
            }

            override fun onError(e: Throwable) {
                if (!subscriber.isUnsubscribed) {
                    subscriber.onError(e)
                }
            }

            override fun onNext(response: Response<T>?) {
                if (subscriber.isUnsubscribed) {
                    return
                }

                response?.let {
                    if (response != null && !response.isSuccessful) {
                        val envelope: ErrorEnvelope? = try {
                            gson?.fromJson(response.errorBody()?.string(), ErrorEnvelope::class.java)
                        } catch (e: Exception) {
                            null
                        }
                        envelope?.let {
                            subscriber.onError(ApiException(envelope, response))
                        } ?: subscriber.onError(ResponseException(response))
                    } else {
                        subscriber.onNext(response.body())
                        subscriber.onCompleted()
                    }
                } ?: subscriber.onError(Exception())
            }
        }
    }
}
