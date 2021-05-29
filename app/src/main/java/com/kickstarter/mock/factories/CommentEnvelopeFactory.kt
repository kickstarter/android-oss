package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope

class CommentEnvelopeFactory {

    companion object {
        fun emptyCommentsEnvelope(): CommentEnvelope {
            return CommentEnvelope.builder()
                .totalCount(0)
                .comments(listOf())
                .pageInfoEnvelope(null)
                .build()
        }

        fun commentsEnvelope(): CommentEnvelope {
            return CommentEnvelope.builder()
                .totalCount(1)
                .comments(listOf(CommentFactory.comment()))
                .pageInfoEnvelope(PageInfoEnvelopeFactory.pageInfoEnvelope())
                .build()
        }
    }
}
