package com.kickstarter.models;

public class Project {
  private final Integer id;
  private final String name;

  public Project(final Integer id,
                 final String name) {
    this.id = id;
    this.name = name;
  }

  // TODO: there's gotta be a better way to do getters in Java.
  public Integer id () {
    return this.id;
  }
  public String name () {
    return this.name;
  }
}
