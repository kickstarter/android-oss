package com.kickstarter.ui.adapters;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class SearchAdapterTest extends KSRobolectricTestCase implements SearchAdapter.Delegate {

  @Test
  public void load3PopularProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    SearchAdapter adapter = new SearchAdapter(this);
    adapter.loadPopularProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(2, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load1PopularProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject()
    );

    SearchAdapter adapter = new SearchAdapter(this);
    adapter.loadPopularProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load0PopularProjects() throws Exception {
    final List<Project> projects = Arrays.asList();

    SearchAdapter adapter = new SearchAdapter(this);
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

    SearchAdapter adapter = new SearchAdapter(this);
    adapter.loadSearchProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(2, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load1SearchProjects() throws Exception {
    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject()
    );

    SearchAdapter adapter = new SearchAdapter(this);
    adapter.loadSearchProjects(projects);
    Assert.assertEquals(1, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Test
  public void load0SearchProjects() throws Exception {
    final List<Project> projects = Arrays.asList();

    SearchAdapter adapter = new SearchAdapter(this);
    adapter.loadSearchProjects(projects);
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_FEATURED_PROJECT));
    Assert.assertEquals(0, adapter.sectionCount(SearchAdapter.SECTION_PROJECT));
  }

  @Override
  public void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project) {
    // nothing to do here
  }
}