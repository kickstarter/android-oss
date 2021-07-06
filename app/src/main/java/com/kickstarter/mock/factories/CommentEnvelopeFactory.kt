package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import org.joda.time.DateTime

class CommentEnvelopeFactory {

    companion object {
        fun emptyCommentsEnvelope(): CommentEnvelope {
            return CommentEnvelope.builder()
                .totalCount(0)
                .comments(emptyList())
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

        fun repliesCommentsEnvelope(createdAt: DateTime): CommentEnvelope {
            return CommentEnvelope.builder()
                .totalCount(1)
                .comments(listOf(CommentFactory.reply(createdAt = createdAt)))
                .pageInfoEnvelope(PageInfoEnvelopeFactory.pageInfoEnvelope())
                .build()
        }

        fun repliesCommentsEnvelopeHasPrevious(createdAt: DateTime): CommentEnvelope {
            return CommentEnvelope.builder()
                .totalCount(1)
                .comments(listOf(CommentFactory.reply(createdAt = createdAt)))
                .pageInfoEnvelope(PageInfoEnvelopeFactory.pageInfoEnvelopeHasPrevious())
                .build()
        }
    }
}
