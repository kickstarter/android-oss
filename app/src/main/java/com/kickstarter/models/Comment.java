package com.kickstarter.models;

public class Comment {
  public String body = null;
  public String created_at = null;
  public Author author = null;

  public static class Author {
    public String name = null;
    public Avatar avatar = null;

  }


}
