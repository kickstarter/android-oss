package com.kickstarter.ui;

public class DiscoveryFilterStyle {
  private final boolean primary;

  public boolean primary() {
    return primary;
  }

  public static class Builder {
    private boolean primary = false;

    public Builder() {}

    public DiscoveryFilterStyle build() {
      return new DiscoveryFilterStyle(this);
    }

    public Builder primary(final boolean primary) {
      this.primary = primary;
      return this;
    }
  }

  private DiscoveryFilterStyle(final Builder builder) {
    primary = builder.primary;
  }

  public Builder builder() {
    return new Builder()
      .primary(primary);
  }
}
