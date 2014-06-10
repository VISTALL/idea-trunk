package org.jetbrains.idea.perforce.perforce.jobs;

import org.jetbrains.annotations.Nullable;

public enum PerforceJobPersistenceType {
  optional("optional"),
  _default("default"),
  required("required"),
  once("once"),
  always("always");

  private final String myName;

  private PerforceJobPersistenceType(final String name) {
    myName = name;
  }

  @Nullable
  public static PerforceJobPersistenceType parse(final String s) {
    final String l = s.toLowerCase();
    if (optional.myName.equals(l)) return optional;
    if (_default.myName.equals(l)) return _default;
    if (required.myName.equals(l)) return required;
    if (once.myName.equals(l)) return once;
    if (always.myName.equals(l)) return always;
    return null;
  }
}
