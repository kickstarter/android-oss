package com.kickstarter.mock.factories

import com.kickstarter.ui.data.CommentCardData

class CommentCardDataFactory {
    companion object {
        fun commentCardData(): CommentCardData = CommentCardData.builder()
            .comment(CommentFactory.comment())
            .project(ProjectFactory.backedProject())
            .commentableId(null)
            .build()
    }
}
