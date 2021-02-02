package com.kickstarter.ui.adapters;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KSViewHolder;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchAdapterTest extends KSRobolectricTestCase implements SearchAdapter.Delegate {
  private final SearchAdapter adapter = new SearchAdapter(this);

  @Test
  public void load3PopularProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();
    final Project project1 = ProjectFactory.almostCompletedProject();
    final Project project2 = ProjectFactory.backedProject();

    this.adapter.loadPopularProjects(Arrays.asList(project0, project1, project2));

    final List<List<Pair<Project, Boolean>>> data = Arrays.asList(
      Collections.singletonList(null),
      Collections.singletonList(Pair.create(project0, true)),
      Arrays.asList(Pair.create(project1, false), Pair.create(project2, false))
    );

    assertEquals(data, this.adapter.sections());
  }

  @Test
  public void load1PopularProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();

    this.adapter.loadPopularProjects(Collections.singletonList(project0));

    final List<List<Pair<Project, Boolean>>> data = Arrays.asList(
      Collections.singletonList(null),
      Collections.singletonList(Pair.create(project0, true)),
      Collections.emptyList()
    );

    assertEquals(data, this.adapter.sections());
  }

  @Test
  public void load0PopularProjects() throws Exception {
    this.adapter.loadPopularProjects(Collections.emptyList());

    final List<List<Pair<Project, Boolean>>> data = Collections.emptyList();

    assertEquals(data, this.adapter.sections());
  }

  @Test
  public void load3SearchProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();
    final Project project1 = ProjectFactory.almostCompletedProject();
    final Project project2 = ProjectFactory.backedProject();

    this.adapter.loadSearchProjects(Arrays.asList(project0, project1, project2));

    final List<List<Pair<Project, Boolean>>> data = Arrays.asList(
      Collections.emptyList(),
      Collections.singletonList(Pair.create(project0, true)),
      Arrays.asList(Pair.create(project1, false), Pair.create(project2, false))
    );

    assertEquals(data, this.adapter.sections());
  }


  @Test
  public void load1SearchProjects() throws Exception {
    final Project project0 = ProjectFactory.allTheWayProject();

    this.adapter.loadSearchProjects(Collections.singletonList(project0));

    final List<List<Pair<Project, Boolean>>> data = Arrays.asList(
      Collections.emptyList(),
      Collections.singletonList(Pair.create(project0, true)),
      Collections.emptyList()
    );

    assertEquals(data, this.adapter.sections());
  }

  @Test
  public void load0SearchProjects() throws Exception {
    this.adapter.loadSearchProjects(Collections.emptyList());

    final List<List<Pair<Project, Boolean>>> data = Collections.emptyList();

    assertEquals(data, this.adapter.sections());
  }

  @Override
  public void projectSearchResultClick(final KSViewHolder viewHolder, final Project project) {
    // nothing to do here
  }
}
