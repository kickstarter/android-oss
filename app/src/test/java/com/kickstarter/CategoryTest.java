package com.kickstarter;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.models.Category;

import junit.framework.TestCase;

public class CategoryTest extends TestCase {
  public void testComparableRootCategories() {
    final Category artCategory = CategoryFactory.artCategory();
    final Category musicCategory = CategoryFactory.musicCategory();

    assertTrue(artCategory.discoveryFilterCompareTo(musicCategory) <= -1);
    assertTrue(musicCategory.discoveryFilterCompareTo(artCategory) >= 1);
  }

  public void testComparableRootAndSelf() {
    final Category artCategory = CategoryFactory.artCategory();
    assertTrue(artCategory.discoveryFilterCompareTo(artCategory) == 0);
  }

  public void testComparableChildAndSelf() {
    final Category bluesCategory = CategoryFactory.bluesCategory();
    assertTrue(bluesCategory.discoveryFilterCompareTo(bluesCategory) == 0);
  }

  public void testComparableParentAndChildren() {
    final Category musicCategory = CategoryFactory.musicCategory();
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(musicCategory.discoveryFilterCompareTo(bluesCategory) <= -1);
    assertTrue(bluesCategory.discoveryFilterCompareTo(musicCategory) >= 1);

    assertTrue(musicCategory.discoveryFilterCompareTo(worldMusicCategory) <= -1);
    assertTrue(worldMusicCategory.discoveryFilterCompareTo(musicCategory) >= -1);
  }

  public void testComparableChildrenAndOtherRoot() {
    final Category photographyCategory = CategoryFactory.photographyCategory();
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(bluesCategory.discoveryFilterCompareTo(photographyCategory) <= -1);
    assertTrue(worldMusicCategory.discoveryFilterCompareTo(photographyCategory) <= -1);

    assertTrue(photographyCategory.discoveryFilterCompareTo(bluesCategory) >= 1);
    assertTrue(photographyCategory.discoveryFilterCompareTo(worldMusicCategory) >= 1);
  }

  public void testComparableChildrenDifferentRoots() {
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category textilesCategory = CategoryFactory.textilesCategory();

    assertTrue(bluesCategory.discoveryFilterCompareTo(textilesCategory) >= 1);
    assertTrue(textilesCategory.discoveryFilterCompareTo(bluesCategory) <= -1);

    final Category ceramicsCategory = CategoryFactory.ceramicsCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(ceramicsCategory.discoveryFilterCompareTo(worldMusicCategory) <= -1);
    assertTrue(worldMusicCategory.discoveryFilterCompareTo(ceramicsCategory) >= 1);
  }
}
