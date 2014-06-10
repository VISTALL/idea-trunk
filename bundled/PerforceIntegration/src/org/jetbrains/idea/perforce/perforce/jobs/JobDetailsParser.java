package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JobDetailsParser {
  private final static ParserLogger LOG = new ParserLogger("#org.jetbrains.idea.perforce.perforce.jobs.JobDetailsParser", "'p4 job' output parse error.");
  private final List<String> myLines;

  public JobDetailsParser(List<String> lines) {
    myLines = lines;
  }

  @NotNull
  public List<Pair<String, String>> parse() throws VcsException {
    final List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();

    for (String line : myLines) {
      if (line.startsWith("#")) {
        // comment
        continue;
      } else if (line.startsWith("\t")) {
        // a value
        if (result.isEmpty()) LOG.generateParseException("Cannot parse line: '" + line + "'");
        final int lastIdx = result.size() - 1;
        final Pair<String, String> prevVal = result.get(lastIdx);
        result.set(lastIdx, new Pair<String, String>(prevVal.getFirst(), prevVal.getSecond() + "\n" + line.trim()));
      } else {
        final int columnIdx = line.indexOf(":");
        if (columnIdx == -1) LOG.generateParseException("Cannot find field name in: '" + line + "'");
        result.add(new Pair<String, String>(line.substring(0, columnIdx), line.substring(columnIdx + 1).trim()));
      }
    }

    return result;
  }
}
