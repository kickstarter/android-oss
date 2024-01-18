@file:JvmName("AiDisclosureExt")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.models.AiDisclosure

/**
 * AiDisclosure model can be empty when all fields shown on the UI are empty, but and ID has been generated.
 */
fun AiDisclosure.isUIEmptyValues(): Boolean {
    return this.generatedByAiConsent.isEmpty() && this.generatedByAiDetails.isEmpty() && this.otherAiDetails.isEmpty() &&
        !this.fundingForAiConsent && !this.fundingForAiOption && !this.fundingForAiAttribution
}
