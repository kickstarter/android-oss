package com.kickstarter.libs

import android.os.Parcelable
import com.kickstarter.services.DiscoveryParams
import kotlinx.parcelize.Parcelize

/**
 * A [RefTag] is a string identifier that Kickstarter uses to credit a pledge to a source of traffic, e.g. discovery,
 * activity, search, etc. This class represents all possible ref tags we support in the app.
 */

@Parcelize
class RefTag private constructor(
    private val tag: String
) : Parcelable {
    fun tag() = this.tag

    @Parcelize
    data class Builder(
        private var tag: String = ""
    ) : Parcelable {
        fun tag(tag: String) = apply { this.tag = tag }
        fun build() = RefTag(
            tag = tag
        )
    }
    fun toBuilder() = Builder(
        tag = tag
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()

        @JvmStatic
        fun from(tag: String): RefTag {
            return builder().tag(tag).build()
        }

        fun activity(): RefTag {
            return builder().tag("activity").build()
        }

        fun activitySample(): RefTag {
            return builder().tag("discovery_activity_sample").build()
        }

        @JvmStatic
        fun category(): RefTag {
            return builder().tag("category").build()
        }

        @JvmStatic
        fun category(sort: DiscoveryParams.Sort): RefTag {
            return builder().tag("category" + sort.refTagSuffix()).build()
        }

        @JvmStatic
        fun categoryFeatured(): RefTag {
            return builder().tag("category_featured").build()
        }

        fun city(): RefTag {
            return builder().tag("city").build()
        }

        @JvmStatic
        fun collection(tagId: Int): RefTag {
            return builder().tag("android_project_collection_tag_$tagId").build()
        }

        @JvmStatic
        fun dashboard(): RefTag {
            return builder().tag("dashboard").build()
        }

        fun deepLink(): RefTag {
            return builder().tag("android_deep_link").build()
        }

        @JvmStatic
        fun discovery(): RefTag {
            return builder().tag("discovery").build()
        }

        fun pledgeInfo(): RefTag {
            return builder().tag("pledge_info").build()
        }

        fun projectShare(): RefTag {
            return builder().tag("android_project_share").build()
        }

        @JvmStatic
        fun push(): RefTag {
            return builder().tag("push").build()
        }

        @JvmStatic
        fun recommended(): RefTag {
            return builder().tag("recommended").build()
        }

        @JvmStatic
        fun recommended(sort: DiscoveryParams.Sort): RefTag {
            return builder().tag("recommended" + sort.refTagSuffix()).build()
        }

        @JvmStatic
        fun search(): RefTag {
            return builder().tag("search").build()
        }

        @JvmStatic
        fun searchFeatured(): RefTag {
            return builder().tag("search_featured").build()
        }

        @JvmStatic
        fun searchPopular(): RefTag {
            return builder().tag("search_popular_title_view").build()
        }

        @JvmStatic
        fun searchPopularFeatured(): RefTag {
            return builder().tag("search_popular_featured").build()
        }

        fun social(): RefTag {
            return builder().tag("social").build()
        }

        fun survey(): RefTag {
            return builder().tag("survey").build()
        }

        @JvmStatic
        fun thanks(): RefTag {
            return builder().tag("thanks").build()
        }

        @JvmStatic
        fun thanksFacebookShare(): RefTag {
            return builder().tag("android_thanks_facebook_share").build()
        }

        @JvmStatic
        fun thanksTwitterShare(): RefTag {
            return builder().tag("android_thanks_twitter_share").build()
        }

        @JvmStatic
        fun thanksShare(): RefTag {
            return builder().tag("android_thanks_share").build()
        }

        @JvmStatic
        fun update(): RefTag {
            return builder().tag("update").build()
        }

        fun updateShare(): RefTag {
            return builder().tag("android_update_share").build()
        }
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is RefTag) {
            equals = tag() == obj.tag()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
