package com.kickstarter.libs.utils

import com.kickstarter.services.DiscoveryParams

object DiscoveryUtils {
    /**
     * Return the corresponding sort for a given tab position.
     */
    fun sortFromPosition(position: Int): DiscoveryParams.Sort {
        return DiscoveryParams.Sort.defaultSorts[position]
    }
}
