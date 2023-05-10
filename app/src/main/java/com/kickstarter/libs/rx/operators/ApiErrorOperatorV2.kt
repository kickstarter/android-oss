package com.kickstarter.libs.rx.operators

import com.google.gson.Gson
import com.kickstarter.services.ApiException
import com.kickstarter.services.ResponseException
import com.kickstarter.services.apiresponses.ErrorEnvelope
import io.reactivex.ObservableOperator
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.Response
import kotlin.Exception

/**
 * Takes a [retrofit2.Response], if it's successful send it to [Observer.onNext], otherwise
 * attempt to parse the error.
 *
 * Errors that conform to the API's error format are converted into an [ApiException] exception and sent to
 * [Observer.onError], otherwise a more generic [ResponseException] is sent to [Observer.onError].
 *
 * @param <T> The response type.
</T> */
class ApiErrorOperatorV2<T>(private val gson: Gson?) : ObservableOperator<T, Response<T>> {
    override fun apply(observer: Observer<in T>): Observer<in Response<T>> {
        val gson = gson

        return object : Observer<Response<T>> {
            override fun onSubscribe(d: Disposable) {
                if (!d.isDisposed) {
                    observer.onSubscribe(d)
                }
            }

            override fun onNext(response: Response<T>) {
                if (!response.isSuccessful) {
                    val envelope: ErrorEnvelope? = try {
                        gson?.fromJson(response.errorBody()?.string(), ErrorEnvelope::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    envelope?.let {
                        observer.onError(ApiException(envelope, response))
                    } ?: observer.onError(ResponseException(response))
                } else {
                    response.body()?.let {
                        observer.onNext(it)
                    }
                    observer.onComplete()
                }
            }

            override fun onError(e: Throwable) {
                observer.onError(e)
            }

            override fun onComplete() {
                observer.onComplete()
            }
        }
    }
}
