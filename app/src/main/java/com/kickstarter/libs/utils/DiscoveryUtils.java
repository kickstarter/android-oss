package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public final class DiscoveryUtils {
  private DiscoveryUtils() {}

  /**
   * Return the corresponding tab position for a given sort param.
   */
  public static int positionFromSort(final @Nullable DiscoveryParams.Sort sort) {
    if (sort == null) {
      return 0;
    }
    switch (sort) {
      case HOME:
        return 0;
      case POPULAR:
        return 1;
      case NEWEST:
        return 2;
      case ENDING_SOON:
        return 3;
      default:
        return 0;
    }
  }

  /**
   * Return the corresponding sort for a given tab position.
   */
  public static @NonNull DiscoveryParams.Sort sortFromPosition(final int position) {
    return DiscoveryParams.Sort.values()[position];
  }

  /**
   * Given a list of projects and root categories this will determine if the first project is featured
   * and is in need of its root category. If that is the case we will find its root and fill in that
   * data and return a new list of projects.
   */
  public static List<Project> fillRootCategoryForFeaturedProjects(final @NonNull List<Project> projects,
    final @NonNull List<Category> rootCategories) {

    // Guard against no projects
    if (projects.size() == 0) {
      return ListUtils.empty();
    }

    final Project firstProject = projects.get(0);

    // Guard against bad category data on first project
    final Category category = firstProject.category();
    if (category == null) {
      return projects;
    }
    final Long categoryParentId = category.parentId();
    if (categoryParentId == null) {
      return projects;
    }

    // Guard against not needing to find the root category
    if (!projectNeedsRootCategory(firstProject, category)) {
      return projects;
    }

    // Find the root category for the featured project's category
    final Category projectRootCategory = Observable.from(rootCategories)
      .filter(rootCategory -> rootCategory.id() == categoryParentId)
      .take(1)
      .toBlocking().single();

    // Sub in the found root category in our featured project.
    final Category newCategory = category.toBuilder().parent(projectRootCategory).build();
    final Project newProject = firstProject.toBuilder().category(newCategory).build();

    return ListUtils.replaced(projects, 0, newProject);
  }

  /**
   * Determines if the project and supplied require us to find the root category.
   */
  public static boolean projectNeedsRootCategory(final @NonNull Project project, final @NonNull Category category) {
    return !category.isRoot() && category.parent() == null && project.isFeaturedToday();
  }
}
