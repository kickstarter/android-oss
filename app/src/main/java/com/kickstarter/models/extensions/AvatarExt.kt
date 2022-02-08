package com.kickstarter.models.extensions

import com.kickstarter.models.Avatar

fun Avatar.replaceSmallImageWithMediumIfEmpty(): String = this.small().ifEmpty { this.medium() }
