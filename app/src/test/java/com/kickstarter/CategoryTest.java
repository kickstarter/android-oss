package com.kickstarter;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.models.Category;

import junit.framework.TestCase;

public class CategoryTest extends TestCase {
  public void testComparableRootCategories() {
    final Category artCategory = CategoryFactory.artCategory();
    final Category musicCategory = CategoryFactory.musicCategory();

    assertTrue(artCategory.compareTo(musicCategory) <= -1);
    assertTrue(musicCategory.compareTo(artCategory) >= 1);
  }

  public void testComparableRootAndSelf() {
    final Category artCategory = CategoryFactory.artCategory();
    assertTrue(artCategory.compareTo(artCategory) == 0);
  }

  public void testComparableChildAndSelf() {
    final Category bluesCategory = CategoryFactory.bluesCategory();
    assertTrue(bluesCategory.compareTo(bluesCategory) == 0);
  }

  public void testComparableParentAndChildren() {
    final Category musicCategory = CategoryFactory.musicCategory();
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(musicCategory.compareTo(bluesCategory) <= -1);
    assertTrue(bluesCategory.compareTo(musicCategory) >= 1);

    assertTrue(musicCategory.compareTo(worldMusicCategory) <= -1);
    assertTrue(worldMusicCategory.compareTo(musicCategory) >= -1);
  }

  public void testComparableChildrenAndOtherRoot() {
    final Category photographyCategory = CategoryFactory.photographyCategory();
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(bluesCategory.compareTo(photographyCategory) <= -1);
    assertTrue(worldMusicCategory.compareTo(photographyCategory) <= -1);

    assertTrue(photographyCategory.compareTo(bluesCategory) >= 1);
    assertTrue(photographyCategory.compareTo(worldMusicCategory) >= 1);
  }

  public void testComparableChildrenDifferentRoots() {
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category textilesCategory = CategoryFactory.textilesCategory();

    assertTrue(bluesCategory.compareTo(textilesCategory) >= 1);
    assertTrue(textilesCategory.compareTo(bluesCategory) <= -1);

    final Category ceramicsCategory = CategoryFactory.ceramicsCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(ceramicsCategory.compareTo(worldMusicCategory) <= -1);
    assertTrue(worldMusicCategory.compareTo(ceramicsCategory) >= 1);
  }
}
