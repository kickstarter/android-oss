package com.kickstarter.libs.perimeterx

import com.perimeterx.msdk.PXManager
import okhttp3.Request

class PerimeterXClient(
        private val manager: PXManager?
):PerimeterXClientType {

    override fun manager() = this.manager

    override fun addHeaderTo(builder: Request.Builder?) {
        val headers = PXManager.httpHeaders()?.let { it.toMap() } ?: emptyMap()

        headers.forEach { (key, value) ->
            builder?.addHeader(key, value)
        }
    }
}