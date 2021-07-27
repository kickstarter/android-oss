package com.kickstarter.mock.factories

import com.kickstarter.ui.data.CommentCardData

class CommentCardDataFactory {
    companion object {
        fun commentCardDataBacked(): CommentCardData = CommentCardData.builder()
            .comment(CommentFactory.comment())
            .project(ProjectFactory.backedProject())
            .commentableId(null)
            .build()

        fun commentCardData(): CommentCardData = CommentCardData.builder()
            .comment(CommentFactory.comment())
            .project(ProjectFactory.project())
            .commentCardState(0)
            .commentableId(null)
            .build()
    }
}
