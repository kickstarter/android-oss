package com.kickstarter.services.apiresponses;

import com.kickstarter.models.Comment;

import java.util.List;

public class CommentsEnvelope {
  public final List<Comment> comments;
  public final UrlsEnvelope urls;

  private CommentsEnvelope(final List<Comment> comments, final UrlsEnvelope urls) {
    this.comments = comments;
    this.urls = urls;
  }

  public static class UrlsEnvelope {
    public final ApiEnvelope api;
    private UrlsEnvelope(final ApiEnvelope api) {
      this.api = api;
    }

    public static class ApiEnvelope {
      public final String moreComments;
      public final String newerComments;

      private ApiEnvelope(final String moreComments, final String newerComments) {
        this.moreComments = moreComments;
        this.newerComments = newerComments;
      }
    }
  }
}
