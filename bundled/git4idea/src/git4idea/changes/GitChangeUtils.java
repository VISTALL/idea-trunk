package git4idea.changes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeListImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitContentRevision;
import git4idea.GitRevisionNumber;
import git4idea.GitUtil;
import git4idea.commands.GitHandler;
import git4idea.commands.GitSimpleHandler;
import git4idea.commands.StringScanner;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.*;

/**
 * Change related utilities
 */
public class GitChangeUtils {
  /**
   * the pattern for committed changelist assumed by {@link #parseChangeList(Project, VirtualFile, StringScanner)}
   */
  public static final String COMMITTED_CHANGELIST_FORMAT = "%ct%n%H%n%P%n%an%x20%x3C%ae%x3E%n%cn%x20%x3C%ce%x3E%n%s%n%x00%n%b%n%x00";

  /**
   * A private constructor for utility class
   */
  private GitChangeUtils() {
  }

  /**
   * Parse changes from lines
   *
   * @param project        the context project
   * @param vcsRoot        the git root
   * @param thisRevision   the current revision
   * @param parentRevision the parent revision for this change list
   * @param s              the lines to parse
   * @param changes        a list of changes to update
   * @param ignoreNames    a set of names ignored during collection of the changes
   * @throws VcsException if the input format does not matches expected format
   */
  public static void parseChanges(Project project,
                                  VirtualFile vcsRoot,
                                  GitRevisionNumber thisRevision,
                                  GitRevisionNumber parentRevision,
                                  String s,
                                  List<Change> changes,
                                  final Set<String> ignoreNames) throws VcsException {
    StringScanner sc = new StringScanner(s);
    parseChanges(project, vcsRoot, thisRevision, parentRevision, sc, changes, ignoreNames);
    if (sc.hasMoreData()) {
      throw new IllegalStateException("Uknown file status: " + sc.line());
    }
  }

  public static Collection<String> parseDiffForPaths(final String rootPath, final StringScanner s) throws VcsException {
    final Collection<String> result = new LinkedList<String>();

    while (s.hasMoreData()) {
      if (s.isEol()) {
        s.nextLine();
        continue;
      }
      if ("CADUMR".indexOf(s.peek()) == -1) {
        // exit if there is no next character
        break;
      }
      String[] tokens = s.line().split("\t");
      String path = tokens[tokens.length - 1];
      path = rootPath + File.separator + GitUtil.unescapePath(path);
      path = FileUtil.toSystemDependentName(path);
      result.add(path);
    }
    return result;
  }

  /**
   * Parse changes from lines
   *
   * @param project        the context project
   * @param vcsRoot        the git root
   * @param thisRevision   the current revision
   * @param parentRevision the parent revision for this change list
   * @param s              the lines to parse
   * @param changes        a list of changes to update
   * @param ignoreNames    a set of names ignored during collection of the changes
   * @throws VcsException if the input format does not matches expected format
   */
  public static void parseChanges(Project project,
                                  VirtualFile vcsRoot,
                                  GitRevisionNumber thisRevision,
                                  GitRevisionNumber parentRevision,
                                  StringScanner s,
                                  List<Change> changes,
                                  final Set<String> ignoreNames) throws VcsException {
    while (s.hasMoreData()) {
      FileStatus status = null;
      if (s.isEol()) {
        s.nextLine();
        continue;
      }
      if ("CADUMR".indexOf(s.peek()) == -1) {
        // exit if there is no next character
        return;
      }
      String[] tokens = s.line().split("\t");
      final ContentRevision before;
      final ContentRevision after;
      final String path = tokens[tokens.length - 1];
      switch (tokens[0].charAt(0)) {
        case 'C':
        case 'A':
          before = null;
          status = FileStatus.ADDED;
          after = GitContentRevision.createRevision(vcsRoot, path, thisRevision, project, false);
          break;
        case 'U':
          status = FileStatus.MERGED_WITH_CONFLICTS;
        case 'M':
          if (status == null) {
            status = FileStatus.MODIFIED;
          }
          before = GitContentRevision.createRevision(vcsRoot, path, parentRevision, project, false);
          after = GitContentRevision.createRevision(vcsRoot, path, thisRevision, project, false);
          break;
        case 'D':
          status = FileStatus.DELETED;
          before = GitContentRevision.createRevision(vcsRoot, path, parentRevision, project, true);
          after = null;
          break;
        case 'R':
          status = FileStatus.MODIFIED;
          before = GitContentRevision.createRevision(vcsRoot, tokens[1], parentRevision, project, true);
          after = GitContentRevision.createRevision(vcsRoot, path, thisRevision, project, false);
          break;
        default:
          throw new VcsException("Unknown file status: " + Arrays.asList(tokens));
      }
      if (ignoreNames == null || !ignoreNames.contains(path)) {
        changes.add(new Change(before, after, status));
      }
    }
  }

  /**
   * Load actual revision number with timestamp basing on revision number expression
   *
   * @param project        a project
   * @param vcsRoot        a repository root
   * @param revisionNumber a revision number expression
   * @return a resolved revision
   * @throws VcsException if there is a problem with running git
   */
  @SuppressWarnings({"SameParameterValue"})
  public static GitRevisionNumber loadRevision(final Project project, final VirtualFile vcsRoot, @NonNls final String revisionNumber)
    throws VcsException {
    GitSimpleHandler handler = new GitSimpleHandler(project, vcsRoot, GitHandler.REV_LIST);
    handler.addParameters("--timestamp", "--max-count=1", revisionNumber);
    handler.endOptions();
    handler.setNoSSH(true);
    handler.setSilent(true);
    String output = handler.run();
    StringTokenizer stk = new StringTokenizer(output, "\n\r \t", false);
    if (!stk.hasMoreTokens()) {
      throw new VcsException("The string '" + revisionNumber + "' does not represents a revision number.");
    }
    Date timestamp = GitUtil.parseTimestamp(stk.nextToken());
    return new GitRevisionNumber(stk.nextToken(), timestamp);
  }

  /**
   * Check if the exception means that HEAD is missing for the current repository.
   *
   * @param e the exception to examine
   * @return true if the head is missing
   */
  public static boolean isHeadMissing(final VcsException e) {
    @NonNls final String errorText = "fatal: bad revision 'HEAD'\n";
    return e.getMessage().equals(errorText);
  }

  /**
   * Get list of changes. Because native Git non-linear revision tree structure is not
   * supported by the current IDEA interfaces some simplifications are made in the case
   * of the merge, so changes are reported as difference with the first revision
   * listed on the the merge that has at least some changes.
   *
   * @param project      the project file
   * @param root         the git root
   * @param revisionName the name of revision (might be tag)
   * @return change list for the respective revision
   * @throws VcsException in case of problem with running git
   */
  public static CommittedChangeList getRevisionChanges(Project project, VirtualFile root, String revisionName) throws VcsException {
    GitSimpleHandler h = new GitSimpleHandler(project, root, GitHandler.SHOW);
    h.setNoSSH(true);
    h.setSilent(true);
    h.addParameters("--name-status", "--no-abbrev", "-M", "--pretty=format:" + COMMITTED_CHANGELIST_FORMAT, "--encoding=UTF-8",
                    revisionName, "--");
    String output = h.run();
    StringScanner s = new StringScanner(output);
    try {
      return parseChangeList(project, root, s);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (VcsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VcsException(e);
    }
  }


  /**
   * Parse changelist
   *
   * @param project the project
   * @param root    the git root
   * @param s       the scanner for log or show command output
   * @return the parsed changelist
   * @throws VcsException if there is a problem with running git
   */
  public static CommittedChangeList parseChangeList(Project project, VirtualFile root, StringScanner s) throws VcsException {
    ArrayList<Change> changes = new ArrayList<Change>();
    // parse commit information
    final Date commitDate = GitUtil.parseTimestamp(s.line());
    final String revisionNumber = s.line();
    final String parentsLine = s.line();
    final String[] parents = parentsLine.length() == 0 ? new String[0] : parentsLine.split(" ");
    String authorName = s.line();
    String committerName = s.line();
    committerName = GitUtil.adjustAuthorName(authorName, committerName);
    String commentSubject = s.boundedToken('\u0000', true);
    s.nextLine();
    String commentBody = s.boundedToken('\u0000', true);
    // construct full comment
    String fullComment;
    if (commentSubject.length() == 0) {
      fullComment = commentBody;
    }
    else if (commentBody.length() == 0) {
      fullComment = commentSubject;
    }
    else {
      fullComment = commentBody + "\n\n" + commentSubject;
    }
    GitRevisionNumber thisRevision = new GitRevisionNumber(revisionNumber, commitDate);
    GitRevisionNumber parentRevision = parents.length > 0 ? loadRevision(project, root, parents[0]) : null;
    long number = Long.parseLong(revisionNumber.substring(0, 15), 16) << 4 + Integer.parseInt(revisionNumber.substring(15, 16), 16);
    if (parents.length <= 1) {
      // This is the first or normal commit with the single parent.
      // Just parse changes in this commit as returned by the show command.
      parseChanges(project, root, thisRevision, parentRevision, s, changes, null);
    }
    else {
      // This is the merge commit. It has multiple parent commits.
      // Find the first commit with changes and report it as a change list.
      // If no changes are found (why to merge than?). Empty changelist is reported.
      int i = 0;
      assert parentRevision != null;
      do {
        if (i != 0) {
          parentRevision = loadRevision(project, root, parents[i]);
          if (parentRevision == null) {
            // the repository was cloned with --depth parameter
            continue;
          }
        }
        GitSimpleHandler diffHandler = new GitSimpleHandler(project, root, GitHandler.DIFF);
        diffHandler.setNoSSH(true);
        diffHandler.setSilent(true);
        diffHandler.addParameters("--name-status", "-M", parentRevision.getRev(), thisRevision.getRev());
        String diff = diffHandler.run();
        parseChanges(project, root, thisRevision, parentRevision, diff, changes, null);
        if (changes.size() > 0) {
          break;
        }
        i++;
      }
      while (i < parents.length);
    }
    return new CommittedChangeListImpl(commentSubject + "(" + revisionNumber + ")", fullComment, committerName, number, commitDate,
                                       changes);
  }
}
