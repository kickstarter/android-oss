package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DiscoverDrawerUtilsTests extends KSRobolectricTestCase {

  static final List<Category> categories = Arrays.asList(
    CategoryFactory.artCategory(),
    CategoryFactory.ceramicsCategory(),
    CategoryFactory.textilesCategory(),
    CategoryFactory.photographyCategory(),
    CategoryFactory.musicCategory(),
    CategoryFactory.bluesCategory(),
    CategoryFactory.worldMusicCategory()
  );

  @Test
  public void testDeriveNavigationDrawerData_LoggedOut_DefaultSelected() {

    final NavigationDrawerData data = DiscoveryDrawerUtils.deriveNavigationDrawerData(
      categories,
      DiscoveryParams.builder().build(),
      null,
      null
    );

    assertEquals(5, data.sections().size());

    assertEquals(1, data.sections().get(0).rows().size());
    assertEquals(1, data.sections().get(1).rows().size());
    assertEquals(1, data.sections().get(2).rows().size());
    assertEquals(1, data.sections().get(3).rows().size());
    assertEquals(1, data.sections().get(4).rows().size());
  }

  @Test
  public void testDeriveNavigationDrawerData_LoggedIn_DefaultSelected() {

    final NavigationDrawerData data = DiscoveryDrawerUtils.deriveNavigationDrawerData(
      categories,
      DiscoveryParams.builder().build(),
      null,
      UserFactory.user()
    );

    assertEquals(7, data.sections().size());

    assertEquals(1, data.sections().get(0).rows().size());
    assertEquals(1, data.sections().get(1).rows().size());
    assertEquals(1, data.sections().get(2).rows().size());
    assertEquals(1, data.sections().get(3).rows().size());
    assertEquals(1, data.sections().get(4).rows().size());
    assertEquals(1, data.sections().get(5).rows().size());
    assertEquals(1, data.sections().get(6).rows().size());
  }

  @Test
  public void testDeriveNavigationDrawerData_LoggedIn_NoRecommendations_DefaultSelected() {

    final NavigationDrawerData data = DiscoveryDrawerUtils.deriveNavigationDrawerData(
      categories,
      DiscoveryParams.builder().build(),
      null,
      UserFactory.noRecommendations()
    );

    assertEquals(6, data.sections().size());

    assertEquals(1, data.sections().get(0).rows().size());
    assertEquals(1, data.sections().get(1).rows().size());
    assertEquals(1, data.sections().get(2).rows().size());
    assertEquals(1, data.sections().get(3).rows().size());
    assertEquals(1, data.sections().get(4).rows().size());
    assertEquals(1, data.sections().get(5).rows().size());
  }

  @Test
  public void testDeriveNavigationDrawerData_LoggedIn_Social_DefaultSelected() {

    final NavigationDrawerData data = DiscoveryDrawerUtils.deriveNavigationDrawerData(
      categories,
      DiscoveryParams.builder().build(),
      null,
      UserFactory.socialUser()
    );

    assertEquals(8, data.sections().size());

    assertEquals(1, data.sections().get(0).rows().size());
    assertEquals(1, data.sections().get(1).rows().size());
    assertEquals(1, data.sections().get(2).rows().size());
    assertEquals(1, data.sections().get(3).rows().size());
    assertEquals(1, data.sections().get(4).rows().size());
    assertEquals(1, data.sections().get(5).rows().size());
    assertEquals(1, data.sections().get(6).rows().size());
    assertEquals(1, data.sections().get(7).rows().size());
  }

  @Test
  public void testDeriveNavigationDrawerData_LoggedOut_ArtExpanded() {

    final NavigationDrawerData data = DiscoveryDrawerUtils.deriveNavigationDrawerData(
      categories,
      DiscoveryParams.builder().build(),
      CategoryFactory.artCategory(),
      null
    );

    assertEquals(5, data.sections().size());

    assertEquals(1, data.sections().get(0).rows().size());
    assertEquals(1, data.sections().get(1).rows().size());
    assertEquals(4, data.sections().get(2).rows().size());
    assertEquals(1, data.sections().get(3).rows().size());
    assertEquals(1, data.sections().get(4).rows().size());
  }
}
