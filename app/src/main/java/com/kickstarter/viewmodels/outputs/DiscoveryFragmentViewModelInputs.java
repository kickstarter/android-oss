package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.DiscoveryAdapter;

import java.util.List;

public interface DiscoveryFragmentViewModelInputs extends DiscoveryAdapter.Delegate {
  /**
   * Call when params from Discovery Activity change.
   */
  void paramsFromActivity(final DiscoveryParams params);

  /**
   * Call when the page content should be cleared.
   */
  void clearPage();

  /**
   * Call for project pagination.
   */
  void nextPage();

  /**
   * Call when we should load the root categories.
   */
  void rootCategories(final List<Category> rootCategories);
}
