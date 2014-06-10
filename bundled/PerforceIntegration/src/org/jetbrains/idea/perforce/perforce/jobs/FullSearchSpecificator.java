package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class FullSearchSpecificator implements JobsSearchSpecificator {
  // todo setting
  public final static int ourMaxLines = 500;
  private final List<Pair<Parts, String>> myStandard;
  private final List<FreePart> myFree;

  public FullSearchSpecificator() {
    myStandard = new ArrayList<Pair<Parts, String>>();
    myFree = new ArrayList<FreePart>();
  }

  public void addStandardConstraint(final Parts part, final String pattern) {
    myStandard.add(new Pair<Parts, String>(part, pattern));
  }

  public void addOtherFieldConstraint(final String fieldName, final String sign, final String pattern) {
    myFree.add(new FreePart(fieldName, sign, pattern));
  }

  public String[] addParams(String[] s) {
    final List<String> list = new ArrayList<String>(s.length + 2);
    list.addAll(Arrays.asList(s));
    list.add("-m");
    list.add("" + (ourMaxLines + 1));
    if (! (myStandard.isEmpty() && myFree.isEmpty())) {
      list.add("-e");
      list.add(createPatterns());
    }
    return list.toArray(new String[list.size()]);
  }

  public int getMaxCount() {
    return ourMaxLines;
  }

  private String createPatterns() {
    final StringBuilder sb = new StringBuilder();
    for (Pair<Parts, String> pair : myStandard) {
      sb.append("(");
      pair.getFirst().add(sb, pair.getSecond());
      sb.append(") ");
    }
    for (FreePart part : myFree) {
      sb.append("(");
      part.add(sb);
      sb.append(") ");
    }
    return sb.toString();
  }

  public static enum Parts {
    jobname("Job") {
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }},
    status("Status") {
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }},
    user("User") {
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }},
    dateBefore("Date") {
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("<=").append(pattern);
      }},
    dateAfter("Date") {
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append(">=").append(pattern);
      }},
    description("Description") {
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }};

    private final String myName;

    private Parts(final String pattern) {
      myName = pattern;
    }

    public void add(final StringBuilder sb, final String pattern) {
      sb.append(myName);
      addImpl(sb, pattern);
    }

    protected abstract void addImpl(final StringBuilder sb, final String pattern);
  }

  private static class FreePart extends Part {
    private final String myFieldName;
    private final String mySign;

    private FreePart(final String fieldName, final String sign, final String pattern) {
      super(pattern);
      myFieldName = fieldName;
      mySign = sign;
    }

    protected void add(final StringBuilder sb) {
      sb.append(myFieldName).append(mySign).append(myPattern);
    }
  }

  private static abstract class Part {
    protected final String myPattern;

    private Part(String pattern) {
      myPattern = pattern;
    }

    protected abstract void add(final StringBuilder sb);
  }
}
