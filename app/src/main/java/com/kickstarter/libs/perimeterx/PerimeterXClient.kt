package com.kickstarter.libs.perimeterx

import com.perimeterx.msdk.PXManager
import com.perimeterx.msdk.PXResponse
import okhttp3.Request

class PerimeterXClient(
        private val manager: PXManager?
):PerimeterXClientType {

    override fun getClient() = this.manager

    override fun addHeaderTo(builder: Request.Builder?) {
        val headers = PXManager.httpHeaders()?.let { it.toMap() } ?: emptyMap()

        headers.forEach { (key, value) ->
            builder?.addHeader(key, value)
        }
    }

    override fun checkError(body: String): PXResponse = PXManager.checkError(body)
}