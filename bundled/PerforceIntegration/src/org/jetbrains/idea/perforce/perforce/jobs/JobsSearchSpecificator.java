package org.jetbrains.idea.perforce.perforce.jobs;

public interface JobsSearchSpecificator {
  String[] addParams(final String[] s);

  /**
   * negative for not defined
   */
  int getMaxCount();

  public static JobsSearchSpecificator DUMMY = new JobsSearchSpecificator() {
    public String[] addParams(String[] s) {
      return s;
    }
    public int getMaxCount() {
      return -1;
    }
  };
}
