package com.kickstarter.services;

import com.google.common.collect.ImmutableMap;
import com.kickstarter.models.Category;

public class DiscoveryParams {
  private final boolean staff_picks;
  private final boolean starred;
  private final boolean backed;
  private final boolean social;
  private final Category category;
  private final Sort sort;
  private final int page;
  private final int page_count;

  public enum Sort {
    MAGIC, POPULAR, ENDING_SOON, NEWEST, MOST_FUNDED;
    @Override
    public String toString() {
      switch (this) {
        case MAGIC:
          return "magic";
        case POPULAR:
          return "popularity";
        case ENDING_SOON:
          return "end_date";
        case NEWEST:
          return "newest";
        case MOST_FUNDED:
          return "most_funded";
      }
      throw new AssertionError("Unhandled sort");
    }
  }

  public static class Builder {
    private boolean staff_picks = false;
    private boolean starred = false;
    private boolean backed = false;
    private boolean social = false;
    private Category category = null;
    private Sort sort = Sort.MAGIC;
    private int page = 1;
    private int page_count = 15;

    public DiscoveryParams build() {
      return new DiscoveryParams(this);
    }

    public Builder() {
    }
    public Builder staff_picks(final boolean v) {
      staff_picks = v;
      return this;
    }
    public Builder starred(final boolean v) {
      starred = v;
      return this;
    }
    public Builder backed(final boolean v) {
      backed = v;
      return this;
    }
    public Builder social(final boolean v) {
      social = v;
      return this;
    }
    public Builder category(final Category v) {
      category = v;
      return this;
    }
    public Builder sort(final Sort v) {
      sort = v;
      return this;
    }
    public Builder page(final int v) {
      page = v;
      return this;
    }
    public Builder page_count(final int v) {
      page_count = v;
      return this;
    }
  }

  private DiscoveryParams(final Builder builder) {
    staff_picks = builder.staff_picks;
    starred = builder.starred;
    backed = builder.backed;
    social = builder.social;
    category = builder.category;
    sort = builder.sort;
    page = builder.page;
    page_count = builder.page_count;
  }

  public Builder builder() {
    return new Builder()
      .staff_picks(staff_picks)
      .starred(starred)
      .backed(backed)
      .social(social)
      .category(category)
      .sort(sort)
      .page(page)
      .page_count(page_count);
  }

  public static DiscoveryParams params() {
    return new Builder().build();
  }

  public DiscoveryParams nextPage () {
    return this.builder().page(page + 1).build();
  }

  public ImmutableMap<String, String> queryParams() {
    return ImmutableMap.<String, String>builder()
      .put("category_id", String.valueOf(category != null ? category.id() : ""))
      .put("staff_picks", String.valueOf(staff_picks))
      .put("starred", String.valueOf(starred))
      .put("backed", String.valueOf(backed))
      .put("sort", sort.toString())
      .put("page", String.valueOf(page))
      .put("page_count", String.valueOf(page_count))
      .put("include_potd", staff_picks && page == 0 ? "true" : "")
      .build();
  }
}
