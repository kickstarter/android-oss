package com.kickstarter.ui.adapters;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class SearchAdapterTest extends KSRobolectricTestCase implements SearchAdapter.Delegate {

  private SearchAdapter adapter;

  @Before
  public void init() {
    adapter = new SearchAdapter(this);
  }

  @Test
  public void load3PopularProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    adapter.loadPopularProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(projects.get(0), getProjectFromSection(SearchAdapter.SECTION_FEATURED_PROJECT, 0));
    Assert.assertEquals(2, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
    Assert.assertEquals(projects.get(1), getProjectFromSection(SearchAdapter.SECTION_PROJECT, 0));
    Assert.assertEquals(projects.get(2), getProjectFromSection(SearchAdapter.SECTION_PROJECT, 1));

  }

  @Test
  public void load1PopularProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject()
    );

    adapter.loadPopularProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(projects.get(0), getProjectFromSection(SearchAdapter.SECTION_FEATURED_PROJECT, 0));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load0PopularProjects() throws Exception {
    final List<Project> projects = Arrays.asList();

    adapter.loadPopularProjects(projects);
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load3SearchProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    adapter.loadSearchProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(projects.get(0), getProjectFromSection(SearchAdapter.SECTION_FEATURED_PROJECT, 0));
    Assert.assertEquals(2, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
    Assert.assertEquals(projects.get(1), getProjectFromSection(SearchAdapter.SECTION_PROJECT, 0));
    Assert.assertEquals(projects.get(2), getProjectFromSection(SearchAdapter.SECTION_PROJECT, 1));
  }


  @Test
  public void load1SearchProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject()
    );

    adapter.loadSearchProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(projects.get(0), getProjectFromSection(SearchAdapter.SECTION_FEATURED_PROJECT, 0));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load0SearchProjects() throws Exception {
    final List<Project> projects = Arrays.asList();

    adapter.loadSearchProjects(projects);
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  // helper method to make tests easier to read
  private Project getProjectFromSection(final @NonNull int section, final @NonNull int i) {
    return ((ProjectSearchResultHolderViewModel.Data) adapter.sections().get(section).get(i)).project;
  }

  @Override
  public void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project) {
    // nothing to do here
  }
}