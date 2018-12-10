package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Category;

import java.util.Arrays;
import java.util.List;

public final class CategoryFactory {
  private CategoryFactory() {}

  public static @NonNull Category category() {
    return musicCategory();
  }

  public static @NonNull Category artCategory() {
    return Category.builder()
      .color(16760235)
      .id(1)
      .name("Art")
      .position(1)
      .projectsCount(367)
      .slug("art")
      .build();
  }

  public static @NonNull Category bluesCategory() {
    return Category.builder()
      .color(10878931)
      .id(316)
      .name("Blues")
      .parent(musicCategory())
      .parentId(musicCategory().id())
      .position(1)
      .projectsCount(5)
      .slug("music/blues")
      .build();
  }

  public static @NonNull Category ceramicsCategory() {
    return Category.builder()
      .color(16760235)
      .id(287)
      .name("Ceramics")
      .parent(artCategory())
      .parentId(artCategory().id())
      .position(1)
      .projectsCount(6)
      .slug("art/ceramics")
      .build();
  }

  public static @NonNull Category gamesCategory() {
    return Category.builder()
      .color(51627)
      .id(12)
      .name("Games")
      .position(9)
      .projectsCount(595)
      .slug("games")
      .build();
  }

  public static @NonNull Category musicCategory() {
    return Category.builder()
      .color(10878931)
      .id(14)
      .name("Music")
      .position(11)
      .projectsCount(641)
      .slug("music")
      .build();
  }

  public static @NonNull Category photographyCategory() {
    return Category.builder()
      .color(58341)
      .id(12)
      .name("Photography")
      .position(12)
      .projectsCount(160)
      .slug("photography")
      .build();
  }

  public static @NonNull List<Category> rootCategories() {
    return Arrays.asList(artCategory(), gamesCategory(), musicCategory(), photographyCategory());
  }

  public static @NonNull Category tabletopGamesCategory() {
    return Category.builder()
      .color(51627)
      .id(34)
      .name("Tabletop Games")
      .parent(gamesCategory())
      .parentId(gamesCategory().id())
      .position(6)
      .projectsCount(226)
      .slug("games/tabletop games")
      .build();
  }

  public static @NonNull Category textilesCategory() {
    return Category.builder()
      .color(16760235)
      .id(289)
      .name("Textiles")
      .parent(artCategory())
      .parentId(artCategory().id())
      .position(11)
      .projectsCount(7)
      .slug("art/textiles")
      .build();
  }

  public static @NonNull Category worldMusicCategory() {
    return Category.builder()
      .color(10878931)
      .id(44)
      .name("World Music")
      .parent(musicCategory())
      .parentId(musicCategory().id())
      .position(17)
      .projectsCount(28)
      .slug("music/world music")
      .build();
  }
}
