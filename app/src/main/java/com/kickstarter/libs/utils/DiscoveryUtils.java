package com.kickstarter.libs.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public class DiscoveryUtils {
  private DiscoveryUtils() {}

  public static @Nullable Drawable imageWithOrientation(@NonNull final Category category,
    @NonNull final Context context) {
    final String baseImageName = category.baseImageName();
    if (baseImageName == null) {
      return null;
    }

    final String name = "category_"
      + baseImageName
      + "_"
      + (ViewUtils.isPortrait(context) ? "portrait" : "landscape");

    final @DrawableRes int identifier = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    if (identifier == 0) {
      return null;
    }

    return ContextCompat.getDrawable(context, identifier);
  }

  public static @ColorInt int primaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().colorWithAlpha() :
      context.getResources().getColor(R.color.discovery_primary);
  }

  public static @ColorInt int secondaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().secondaryColor(context) :
      context.getResources().getColor(R.color.discovery_secondary);
  }

  public static boolean overlayShouldBeLight(@NonNull final DiscoveryParams params) {
    return params.category() == null || params.category().overlayShouldBeLight();
  }

  public static @ColorInt int overlayTextColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return overlayTextColor(context, overlayShouldBeLight(params));
  }

  public static @ColorInt int overlayTextColor(@NonNull final Context context, final boolean light) {
    final @ColorRes int color = light ? KSColorUtils.lightColorId() : KSColorUtils.darkColorId();
    return context.getResources().getColor(color);
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
