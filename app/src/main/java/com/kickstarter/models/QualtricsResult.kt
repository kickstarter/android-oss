package com.kickstarter.models

import com.qualtrics.digital.TargetingResult

open class QualtricsResult(private val targetingResult: TargetingResult?) {
    constructor() : this(null)

    fun recordClick() = this.targetingResult?.recordClick()

    fun recordImpression() = this.targetingResult?.recordImpression()

    open fun resultPassed(): Boolean = this.targetingResult?.passed() ?: false

    open fun surveyUrl(): String = this.targetingResult?.surveyUrl ?: ""
}
