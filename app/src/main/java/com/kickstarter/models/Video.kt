package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Video private constructor(
    private val base: String?,
    private val frame: String?,
    private val high: String?,
    private val hls: String?,
    private val webm: String?
) : Parcelable {
    fun base() = this.base
    fun frame() = this.frame
    fun high() = this.high
    fun hls() = this.hls
    fun webm() = this.webm

    @Parcelize
    data class Builder(
        private var base: String? = null,
        private var frame: String? = null,
        private var high: String? = null,
        private var hls: String? = null,
        private var webm: String? = null,
    ) : Parcelable {
        fun base(base: String?) = apply { this.base = base }
        fun frame(frame: String?) = apply { this.frame = frame }
        fun high(high: String?) = apply { this.high = high }
        fun hls(hls: String?) = apply { this.hls = hls }
        fun webm(webm: String?) = apply { this.webm = webm }

        fun build() = Video(
            base = base,
            frame = frame,
            high = high,
            hls = hls,
            webm = webm

        )
    }

    fun toBuilder() = Builder(
        base = base,
        frame = frame,
        high = high,
        hls = hls,
        webm = webm
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Video) {
            equals = base() == obj.base() &&
                frame() == obj.frame() &&
                high() == obj.high() &&
                hls() == obj.hls() &&
                webm() == obj.webm()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
