package com.kickstarter.libs.utils.extensions

import com.kickstarter.libs.RefTag
import com.kickstarter.services.DiscoveryParams

/**
 * A `ref_tag` representation of some discovery params. This tag can be used to attribute a checkout when a user
 * pledges from discovery using these particular params.
 */
fun DiscoveryParams.refTag(): RefTag {
    if (this.isCategorySet) {
        val sort = this.sort()
        return if (sort != null) {
            RefTag.category(sort)
        } else
            RefTag.category()
    }

    if (this.location() != null) {
        return RefTag.city()
    }

    val staffPicks: Boolean = this.staffPicks().isTrue()

    if (staffPicks) {
        val sort = this.sort()
        return if (sort != null) {
            RefTag.recommended(sort)
        } else
            RefTag.recommended()
    }

    this.tagId()?.let {
        RefTag.collection(it)
    }

    if (this.social().isNonZero()) {
        return RefTag.social()
    }

    return if (this.term() != null) {
        RefTag.search()
    } else RefTag.discovery()
}
