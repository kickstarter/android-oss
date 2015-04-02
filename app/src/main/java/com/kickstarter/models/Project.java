package com.kickstarter.models;

public class Project {
  private final Integer id;
  private final String name;

  public Project(final Integer id,
                 final String name) {
    this.id = id;
    this.name = name;
  }

  public Integer id() { return id; }
  public String name() { return this.name; }
}
