package com.kickstarter.ui.data

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ActivityResult(
    val requestCode: Int,
    val resultCode: Int,
    val intent: Intent?
) : Parcelable {
    fun requestCode() = this.requestCode
    fun resultCode() = this.resultCode
    fun intent() = this.intent

    @Parcelize
    data class Builder(
        private var requestCode: Int = 0,
        private var resultCode: Int = 0,
        private var intent: Intent? = null
    ) : Parcelable {
        fun requestCode(requestCode: Int) = apply { this.requestCode = requestCode }
        fun resultCode(resultCode: Int) = apply { this.resultCode = resultCode }
        fun intent(intent: Intent?) = apply { this.intent = intent }
        fun build() = ActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            intent = intent
        )
    }

    fun toBuilder() = Builder(
        requestCode = requestCode,
        resultCode = resultCode,
        intent = intent
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is ActivityResult) {
            equals = requestCode() == obj.requestCode() &&
                resultCode() == obj.resultCode() &&
                intent() == obj.intent()
        }
        return equals
    }

    val isCanceled: Boolean
        get() = resultCode() == Activity.RESULT_CANCELED
    val isOk: Boolean
        get() = resultCode() == Activity.RESULT_OK

    fun isRequestCode(v: Int): Boolean {
        return requestCode() == v
    }

    companion object {
        @JvmStatic
        fun create(requestCode: Int, resultCode: Int, intent: Intent?): ActivityResult {
            return builder()
                .requestCode(requestCode)
                .resultCode(resultCode)
                .intent(intent)
                .build()
        }

        @JvmStatic
        fun builder() = Builder()
    }
}
