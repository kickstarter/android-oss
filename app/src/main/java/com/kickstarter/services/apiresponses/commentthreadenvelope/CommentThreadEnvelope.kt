package com.kickstarter.services.apiresponses.commentthreadenvelope

import com.kickstarter.models.CommentThread

data class CommentThreadEnvelope(val comments: List<CommentThread>?, val page: PageInfoEnvelope?, val totalCount: Int, val cursor: String?)
