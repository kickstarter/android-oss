package com.kickstarter.mock.factories

import com.kickstarter.models.Photo
import com.kickstarter.models.Photo.Companion.builder

object PhotoFactory {
    @JvmStatic
    fun photo(): Photo {
        val url =
            "https://i.kickstarter.com/assets/012/032/069/46817a8c099133d5bf8b64aad282a696_original.png?fit=crop&gravity=auto&height=873&origin=ugc&q=92&width=1552&sig=M0CDstmWdpd%2FPjzZc8qmMmfj2BiCgtKiH15ugun8qwk%3D"
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
