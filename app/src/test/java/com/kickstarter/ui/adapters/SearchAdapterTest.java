package com.kickstarter.ui.adapters;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchAdapterTest extends KSRobolectricTestCase implements SearchAdapter.Delegate {

  private SearchAdapter adapter = new SearchAdapter(this);

  @Test
  public void load3PopularProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();
    final Project project1 = ProjectFactory.almostCompletedProject();
    final Project project2 = ProjectFactory.backedProject();

    adapter.loadPopularProjects(Arrays.asList(project0, project1, project2));

    final List<List<ProjectSearchResultHolderViewModel.Data>> data = Arrays.asList(
      Collections.singletonList(
        null
      ),
      Collections.singletonList(
        new ProjectSearchResultHolderViewModel.Data(project0, true)
      ),
      Arrays.asList(
        new ProjectSearchResultHolderViewModel.Data(project1, false),
        new ProjectSearchResultHolderViewModel.Data(project2, false)
      )
    );

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void load1PopularProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();

    adapter.loadPopularProjects(Collections.singletonList(project0));

    final List<List<ProjectSearchResultHolderViewModel.Data>> data = Arrays.asList(
      Collections.singletonList(
        null
      ),
      Collections.singletonList(
        new ProjectSearchResultHolderViewModel.Data(project0, true)
      ),
        Collections.emptyList()
      );

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void load0PopularProjects() throws Exception {
    adapter.loadPopularProjects(Collections.emptyList());

    final List<List<ProjectSearchResultHolderViewModel.Data>> data = Collections.emptyList();

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void load3SearchProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();
    final Project project1 = ProjectFactory.almostCompletedProject();
    final Project project2 = ProjectFactory.backedProject();

    adapter.loadSearchProjects(Arrays.asList(project0, project1, project2));

    final List<List<ProjectSearchResultHolderViewModel.Data>> data = Arrays.asList(
      Collections.emptyList(),
      Collections.singletonList(
        new ProjectSearchResultHolderViewModel.Data(project0, true)
      ),
      Arrays.asList(
        new ProjectSearchResultHolderViewModel.Data(project1, false),
        new ProjectSearchResultHolderViewModel.Data(project2, false)
      )
    );

    Assert.assertEquals(data, adapter.sections());
  }


  @Test
  public void load1SearchProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();

    adapter.loadSearchProjects(Arrays.asList(project0));

    final List<List<ProjectSearchResultHolderViewModel.Data>> data = Arrays.asList(
      Collections.emptyList(),
      Collections.singletonList(
        new ProjectSearchResultHolderViewModel.Data(project0, true)
      ),
      Collections.emptyList()
    );

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void load0SearchProjects() throws Exception {
    adapter.loadSearchProjects(Collections.emptyList());

    final List<List<ProjectSearchResultHolderViewModel.Data>> data = Collections.emptyList();

    Assert.assertEquals(data, adapter.sections());
  }

  @Override
  public void projectSearchResultClick(final ProjectSearchResultViewHolder viewHolder, final Project project) {
    // nothing to do here
  }
}
