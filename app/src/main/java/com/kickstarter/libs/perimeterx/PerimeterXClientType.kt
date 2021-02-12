package com.kickstarter.libs.perimeterx

import com.perimeterx.msdk.PXManager

interface PerimeterXClientType {
    fun manager(): PXManager?
    fun httpHeaders(): Map<String, String>
}