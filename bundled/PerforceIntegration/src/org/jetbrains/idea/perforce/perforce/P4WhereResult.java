package org.jetbrains.idea.perforce.perforce;

import org.jetbrains.annotations.NotNull;

public class P4WhereResult {
  // "C:\depot\..."
  private final String myLocal;
  // "//unit-206/..."
  private final String myLocalRootDependent;
  // "\\depot\..."
  private final String myDepot;

  public P4WhereResult(final @NotNull String local, final @NotNull String localRootDependent, final @NotNull String depot) {
    myLocal = local;
    myLocalRootDependent = localRootDependent;
    myDepot = depot;
  }

  public String getLocal() {
    return myLocal;
  }

  public String getLocalRootDependent() {
    return myLocalRootDependent;
  }

  public String getDepot() {
    return myDepot;
  }
}
