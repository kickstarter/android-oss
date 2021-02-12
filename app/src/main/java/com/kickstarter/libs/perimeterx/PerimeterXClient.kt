package com.kickstarter.libs.perimeterx

import com.perimeterx.msdk.PXManager

class PerimeterXClient(
        private val manager: PXManager?
):PerimeterXClientType {

    override fun manager() = this.manager

    override fun httpHeaders() = PXManager.httpHeaders()?.let { it.toMap() } ?: emptyMap()

}