package org.jetbrains.idea.perforce.perforce.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByNamesConstraint implements JobsSearchSpecificator {
  private final List<String> myNames;

  public ByNamesConstraint(List<String> names) {
    myNames = names;
  }

  public String[] addParams(final String[] s) {
    if (myNames.isEmpty()) {
      return s;
    }
    final List<String> list = new ArrayList<String>(s.length + 2);
    list.addAll(Arrays.asList(s));
    list.add("-e");
    list.add(getNamesConstraint());
    return list.toArray(new String[list.size()]);
  }

  public int getMaxCount() {
    return -1;
  }

  private String getNamesConstraint() {
    final StringBuilder sb = new StringBuilder().append("Job=");
    for (int i = 0; i < myNames.size(); i++) {
      final String s = myNames.get(i);
      if (i > 0) {
        sb.append('|');
      }
      sb.append(s);
    }
    return sb.toString();
  }
}
