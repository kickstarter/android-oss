package com.kickstarter.factories;

import com.kickstarter.models.Category;

public class CategoryFactory {
  public static Category artCategory() {
    final Category category = new Category();
    category.color = 16760235;
    category.id = 1;
    category.name = "Art";
    category.position = 1;
    category.projectsCount = 367;
    category.slug = "art";

    return category;
  }

  public static Category bluesCategory() {
    final Category category = new Category();
    category.color = 10878931;
    category.id = 316;
    category.name = "Blues";
    category.parent(musicCategory());
    category.position = 1;
    category.projectsCount = 5;
    category.slug = "music/blues";

    return category;
  }

  public static Category ceramicsCategory() {
    final Category category = new Category();
    category.color = 16760235;
    category.id = 287;
    category.name = "Ceramics";
    category.parent(artCategory());
    category.position = 1;
    category.projectsCount = 6;
    category.slug = "art/ceramics";

    return category;
  }

  public static Category musicCategory() {
    final Category category = new Category();
    category.color = 10878931;
    category.id = 14;
    category.name = "Music";
    category.position = 11;
    category.projectsCount = 641;
    category.slug = "music";

    return category;
  }

  public static Category photographyCategory() {
    final Category category = new Category();
    category.color = 58341;
    category.id = 12;
    category.name = "Photography";
    category.position = 12;
    category.projectsCount = 160;
    category.slug = "photography";

    return category;
  }

  public static Category textilesCategory() {
    final Category category = new Category();
    category.color = 16760235;
    category.id = 289;
    category.name = "Textiles";
    category.parent(artCategory());
    category.position = 11;
    category.projectsCount = 7;
    category.slug = "art/textiles";
    return category;
  }

  public static Category worldMusicCategory() {
    final Category category = new Category();
    category.color = 10878931;
    category.id = 44;
    category.name = "World Music";
    category.parent(musicCategory());
    category.position = 17;
    category.projectsCount = 28;
    category.slug = "music/world music";

    return category;
  }
}
