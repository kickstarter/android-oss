package com.kickstarter.services.ApiResponses;

public class InternalBuildEnvelope {
  Boolean newer_build_available;
  Integer build;

  public Boolean newerBuildAvailable() {
    return newer_build_available;
  }

  public Integer build() {
    return build;
  }
}
