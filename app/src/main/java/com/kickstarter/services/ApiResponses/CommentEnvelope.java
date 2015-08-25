package com.kickstarter.services.ApiResponses;

import com.kickstarter.models.Comment;

import java.util.List;

public class CommentEnvelope {
  public final List<Comment> comments;
  public final UrlsEnvelope urls;

  private CommentEnvelope(final List<Comment> comments, final UrlsEnvelope urls) {
    this.comments = comments;
    this.urls = urls;
  }

  public static class UrlsEnvelope {
    public final ApiEnvelope api;
    private UrlsEnvelope(final ApiEnvelope api) {
      this.api = api;
    }

    public static class ApiEnvelope {
      public final String more_comments;
      public final String newer_comments;

      private ApiEnvelope(final String more_comments, final String newer_comments) {
        this.more_comments = more_comments;
        this.newer_comments = newer_comments;
      }
    }
  }
}
