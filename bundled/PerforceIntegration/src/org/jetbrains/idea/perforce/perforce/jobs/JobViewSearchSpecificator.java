package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.text.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobViewSearchSpecificator implements JobsSearchSpecificator {
  private final String myJobView;
  private final String myCustomFilter;

  public JobViewSearchSpecificator(final String jobView, final String customFilter) {
    myJobView = jobView;
    myCustomFilter = customFilter;
  }

  public String[] addParams(final String[] s) {
    final List<String> list = new ArrayList<String>(s.length + 2);
    list.addAll(Arrays.asList(s));
    list.add("-m");
    list.add("" + (FullSearchSpecificator.ourMaxLines + 1));

    final boolean notEmptyJobView = ! StringUtil.isEmptyOrSpaces(myJobView);
    final boolean notEmptyCustomFilter = ! StringUtil.isEmptyOrSpaces(myCustomFilter);
    if (notEmptyJobView || notEmptyCustomFilter) {
      list.add("-e");
      final StringBuilder sb = new StringBuilder();
      if (notEmptyJobView) {
        sb.append("(").append(myJobView).append(")");
      }
      if (notEmptyCustomFilter) {
        if (notEmptyJobView) {
          sb.append(" & ");
        }
        sb.append("(").append(myCustomFilter).append(")");
      }
      list.add(sb.toString());
    }
    return list.toArray(new String[list.size()]);
  }

  public int getMaxCount() {
    return 50;
  }
}
