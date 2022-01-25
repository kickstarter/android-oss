package com.kickstarter.mock.factories

import com.kickstarter.models.Photo
import com.kickstarter.models.Photo.Companion.builder

object PhotoFactory {
    @JvmStatic
    fun photo(): Photo {
        val url =
            "https://ksr-ugc.imgix.net/assets/012/032/069/46817a8c099133d5bf8b64aad282a696_original.png?crop=faces&w=1552&h=873&fit=crop&v=1463725702&auto=format&q=92&s=72501d155e4a5e399276632687c77959"
        return builder()
            .ed(url)
            .full(url)
            .little(url)
            .med(url)
            .small(url)
            .thumb(url)
            .build()
    }
}
