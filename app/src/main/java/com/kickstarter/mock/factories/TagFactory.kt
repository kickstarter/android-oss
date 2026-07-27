package com.kickstarter.mock.factories

import com.kickstarter.models.Tag

object TagFactory {
    @JvmStatic
    fun tag(): Tag {
        return make100()
    }

    @JvmStatic
    fun make100(): Tag {
        return Tag.builder()
            .id(1L)
            .name("Make 100")
            .slug("make-100")
            .url("https://www.kickstarter.com/discover/advanced?tag_id=1")
            .build()
    }

    @JvmStatic
    fun zineQuest(): Tag {
        return Tag.builder()
            .id(2L)
            .name("Zine Quest")
            .slug("zine-quest")
            .url("https://www.kickstarter.com/discover/advanced?tag_id=2")
            .build()
    }

    @JvmStatic
    fun witchstarter(): Tag {
        return Tag.builder()
            .id(3L)
            .name("Witchstarter")
            .slug("witchstarter")
            .url("https://www.kickstarter.com/discover/advanced?tag_id=3")
            .build()
    }

    @JvmStatic
    fun tags(): List<Tag> {
        return listOf(make100(), zineQuest(), witchstarter())
    }
}
