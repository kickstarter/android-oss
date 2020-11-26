package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.EmailVerificationDeepLinkActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.IOException

class EmailVerificationDeepLinkViewModel {
    interface Inputs {
    }

    interface Outputs {
        fun openDiscoveryActivityWith(): Observable<Pair<Int, String>>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<EmailVerificationDeepLinkActivity>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val client: OkHttpClient = this.environment.okHttpClient()
        private val messageAndCode = PublishSubject.create<Pair<Int, String>>()

                init {
            val uriFromIntent = intent()
                    .map { obj: Intent -> obj.data }
                    .ofType(Uri::class.java)

            uriFromIntent
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .switchMap {
                        makeCall(it)
                    }
                    .compose(bindToLifecycle())
                    .subscribe { response ->
                        val body = response?.body()?.string()
                        val isRedirect = response?.priorResponse()?.isRedirect?.toString()
                        val responseCode = response?.code() ?: 0
                        val message = response?.message() ?: ""
                        Log.i("Response body: ", body)
                        Log.i("fromRedirect: ", isRedirect.toString())
                        Log.i("code: ", responseCode.toString())
                        Log.i("message: ", message)
                        messageAndCode.onNext(Pair(responseCode, message))
                    }
        }

        private fun makeCall(uri: Uri): Observable<Response?> {
            val url = uri.toString()
            val request = Request.Builder()
                    .url(url)
                    .build()
            return try {
                val response = client.newCall(request).execute()
                Observable.just(response)
            } catch (exception: IOException) {
                Observable.just(null)
            }
        }

        override fun openDiscoveryActivityWith(): Observable<Pair<Int, String>> = this.messageAndCode
    }

}