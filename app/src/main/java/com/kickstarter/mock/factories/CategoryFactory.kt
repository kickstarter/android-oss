package com.kickstarter.mock.factories

import com.kickstarter.models.Category
import com.kickstarter.models.Category.Companion.builder

object CategoryFactory {
    @JvmStatic
    fun category(): Category {
        return musicCategory()
    }

    @JvmStatic
    fun artCategory(): Category {
        return builder()
            .color(16760235)
            .id(1)
            .name("Art")
            .analyticsName("categoryName")
            .position(1)
            .projectsCount(367)
            .slug("art")
            .build()
    }

    @JvmStatic
    fun bluesCategory(): Category {
        return builder()
            .color(10878931)
            .id(316)
            .name("Blues")
            .analyticsName("subcategoryName")
            .parent(musicCategory())
            .parentId(musicCategory().id())
            .parentName(musicCategory().name())
            .position(1)
            .projectsCount(5)
            .slug("music/blues")
            .build()
    }

    @JvmStatic
    fun ceramicsCategory(): Category {
        return builder()
            .color(16760235)
            .id(287)
            .name("Ceramics")
            .analyticsName("subcategoryName")
            .parent(artCategory())
            .parentId(artCategory().id())
            .parentName(artCategory().name())
            .parent(artCategory())
            .position(1)
            .projectsCount(6)
            .slug("art/ceramics")
            .build()
    }

    @JvmStatic
    fun gamesCategory(): Category {
        return builder()
            .color(51627)
            .id(12)
            .analyticsName("categoryName")
            .name("Games")
            .position(9)
            .projectsCount(595)
            .slug("games")
            .build()
    }

    @JvmStatic
    fun musicCategory(): Category {
        return builder()
            .color(10878931)
            .id(14)
            .name("Music")
            .analyticsName("categoryName")
            .position(11)
            .projectsCount(641)
            .slug("music")
            .build()
    }

    @JvmStatic
    fun photographyCategory(): Category {
        return builder()
            .color(58341)
            .id(12)
            .analyticsName("categoryName")
            .name("Photography")
            .position(12)
            .projectsCount(160)
            .slug("photography")
            .build()
    }

    @JvmStatic
    fun rootCategories(): List<Category> {
        return listOf(artCategory(), gamesCategory(), musicCategory(), photographyCategory())
    }

    @JvmStatic
    fun tabletopGamesCategory(): Category {
        return builder()
            .color(51627)
            .id(34)
            .name("Tabletop Games")
            .analyticsName("subcategoryName")
            .parent(gamesCategory())
            .parentId(gamesCategory().id())
            .parentName(gamesCategory().name())
            .position(6)
            .projectsCount(226)
            .slug("games/tabletop games")
            .build()
    }

    @JvmStatic
    fun textilesCategory(): Category {
        return builder()
            .color(16760235)
            .id(289)
            .name("Textiles")
            .analyticsName("subcategoryName")
            .parent(artCategory())
            .parentId(artCategory().id())
            .parentName(artCategory().name())
            .position(11)
            .projectsCount(7)
            .slug("art/textiles")
            .build()
    }

    @JvmStatic
    fun worldMusicCategory(): Category {
        return builder()
            .color(10878931)
            .id(44)
            .name("World Music")
            .analyticsName("subcategoryName")
            .parent(musicCategory())
            .parentId(musicCategory().id())
            .parentName(musicCategory().name())
            .position(17)
            .projectsCount(28)
            .slug("music/world music")
            .build()
    }
}
