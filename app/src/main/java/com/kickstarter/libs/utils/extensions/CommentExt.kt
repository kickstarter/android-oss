package com.kickstarter.libs.utils.extensions

import com.kickstarter.models.Comment

fun Comment.isReply() = this.parentId() > 0
