package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;

public class ParserLogger {
  private final Logger myLogger;
  private final String myPrefix;

  public ParserLogger(final String category, final String prefix) {
    myPrefix = prefix + " ";
    myLogger = Logger.getInstance(category);
  }

  public void generateParseException(final String detailsText) throws VcsException {
    final String message = myPrefix + detailsText;
    myLogger.info(message);
    throw new VcsException(message);
  }
}
