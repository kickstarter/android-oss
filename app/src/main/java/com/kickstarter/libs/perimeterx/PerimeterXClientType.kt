package com.kickstarter.libs.perimeterx

import com.perimeterx.msdk.PXManager
import com.perimeterx.msdk.PXResponse
import okhttp3.Request

interface PerimeterXClientType {
    fun getClient(): PXManager?
    fun addHeaderTo(builder: Request.Builder?)
    fun checkError(body: String): PXResponse
}