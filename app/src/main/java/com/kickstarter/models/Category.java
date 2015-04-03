package com.kickstarter.models;

public class Category {
  private final Integer id;
  private final String name;

  public Category(final Integer id,
                 final String name) {
    this.id = id;
    this.name = name;
  }

  public Integer id() { return id; }
  public String name() { return this.name; }
}