package com.kickstarter.models;

public class Project {
  private final Category category;
  private final Integer id;
  private final String name;
  private final Photo photo;

  public Project(final Category category,
                 final Integer id,
                 final String name,
                 final Photo photo) {
    this.category = category;
    this.id = id;
    this.name = name;
    this.photo = photo;
  }

  public Category category() { return category; }
  public Integer id() { return id; }
  public String name() { return this.name; }
  public Photo photo() { return this.photo; }

  public class Photo {
    private final String full;

    public Photo(final String full) {
      this.full = full;
    }

    public String full() { return this.full; }
  }
}
