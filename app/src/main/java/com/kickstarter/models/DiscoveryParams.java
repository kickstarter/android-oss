package com.kickstarter.models;

import com.google.common.collect.ImmutableMap;

public class DiscoveryParams {

  public enum Sort {
    MAGIC, POPULAR, ENDING_SOON, NEWEST, MOST_FUNDED;
    @Override
    public String toString() {
      switch (this) {
        case MAGIC:       return "magic";
        case POPULAR:     return "popularity";
        case ENDING_SOON: return "end_date";
        case NEWEST:      return "newest";
        case MOST_FUNDED: return "most_funded";
      }
      return "";
    }
  }

  private final Boolean staff_picks;
  private final Sort sort;

  public DiscoveryParams(Boolean staff_picks, Sort sort) {
    this.staff_picks = staff_picks;
    this.sort = sort;
  }

  public ImmutableMap<String, String> queryParams () {
    return ImmutableMap.of(
      "staff_picks", staff_picks.toString(),
      "sort", sort.toString(),
      "include_potd", staff_picks ? "true" : ""
    );
  }
}
