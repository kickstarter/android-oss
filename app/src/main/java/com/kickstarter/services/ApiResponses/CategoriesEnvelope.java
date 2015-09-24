package com.kickstarter.services.apiresponses;

import com.kickstarter.models.Category;

import java.util.List;

public class CategoriesEnvelope {
  private List<Category> categories;

  public List<Category> categories() {
    return categories;
  }
}
