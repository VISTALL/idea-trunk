/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.svn.history;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.issueLinks.TableLinkMouseListener;
import com.intellij.openapi.vcs.history.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Consumer;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnRevisionNumber;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.actions.ShowAllSubmittedFilesAction;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.util.SVNLogType;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class SvnHistoryProvider implements VcsHistoryProvider {

  private final SvnVcs myVcs;
  private SVNURL myURL;
  private SVNRevision myRevision;
  private boolean myDirectory;

  public SvnHistoryProvider(SvnVcs vcs) {
    this(vcs, null, null, false);
  }

  public SvnHistoryProvider(@NotNull SvnVcs vcs, SVNURL url, SVNRevision revision, boolean isDirectory) {
    myVcs = vcs;
    myURL = url;
    myRevision = revision;
    myDirectory = isDirectory;
  }

  public HistoryAsTreeProvider getTreeHistoryProvider() {
    return null;
  }

  public boolean supportsHistoryForDirectories() {
    return true;
  }

  public VcsDependentHistoryComponents getUICustomization(final VcsHistorySession session, JComponent forShortcutRegistration) {
    final ColumnInfo[] columns;
    final Consumer<VcsFileRevision> listener;
    final JComponent addComp;
    if (((MyHistorySession) session).isSupports15()) {
      final MergeSourceColumnInfo mergeSourceColumn = new MergeSourceColumnInfo((MyHistorySession)session);
      columns = new ColumnInfo[] {new CopyFromColumnInfo(), mergeSourceColumn};

      final JTextArea field = new JTextArea();
      field.setEditable(false);
      field.setBackground(UIUtil.getComboBoxDisabledBackground());
      field.setWrapStyleWord(true);
      listener = new Consumer<VcsFileRevision>() {
        public void consume(VcsFileRevision vcsFileRevision) {
          field.setText(mergeSourceColumn.getText(vcsFileRevision));
        }
      };
      final JPanel panel = new JPanel(new GridBagLayout());
      final GridBagConstraints gb =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);

      final JLabel mergeLabel = new JLabel("Merge Sources:");
      final MergeSourceDetailsAction sourceAction = new MergeSourceDetailsAction();
      sourceAction.registerSelf(forShortcutRegistration);
      final DefaultActionGroup group = new DefaultActionGroup();
      group.add(sourceAction);
      final JComponent toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true).getComponent();

      panel.add(mergeLabel, gb);
      ++ gb.gridx;
      gb.insets.left = 10;
      gb.anchor = GridBagConstraints.NORTHWEST;
      panel.add(toolbar, gb);

      ++ gb.gridy;
      gb.insets.left = 0;
      gb.gridx = 0;
      gb.gridwidth = 2;
      gb.weightx = gb.weighty = 1;
      gb.fill = GridBagConstraints.BOTH;
      panel.add(ScrollPaneFactory.createScrollPane(field), gb);
      addComp = panel;
    } else {
      columns = new ColumnInfo[] {new CopyFromColumnInfo()};
      addComp = null;
      listener = null;
    }
    return new VcsDependentHistoryComponents(columns, listener, addComp);
  }

  private class MyHistorySession extends VcsHistorySession {
    private final FilePath myCommittedPath;
    private final Map<Long, SvnChangeList> myListsMap;
    private final boolean mySupports15;

    private MyHistorySession(final List<VcsFileRevision> revisions, final FilePath committedPath, final boolean supports15) {
      super(revisions);
      myCommittedPath = committedPath;
      mySupports15 = supports15;
      myListsMap = new HashMap<Long, SvnChangeList>();
      refresh();
    }

    public Map<Long, SvnChangeList> getListsMap() {
      return myListsMap;
    }

    @Nullable
    public VcsRevisionNumber calcCurrentRevisionNumber() {
      if (myCommittedPath == null) {
        return null;
      }
      return getCurrentRevision(myCommittedPath);
    }

    public FilePath getCommittedPath() {
      return myCommittedPath;
    }

    @Override
    public boolean isContentAvailable(final VcsFileRevision revision) {
      return ! myDirectory;
    }

    public boolean isSupports15() {
      return mySupports15;
    }
  }

  @Nullable
  public VcsHistorySession createSessionFor(final FilePath filePath) throws VcsException {
    final FilePath committedPath = ChangesUtil.getCommittedPath(myVcs.getProject(), filePath);
    final Ref<Boolean> supports15Ref = new Ref<Boolean>();
    final List<VcsFileRevision> revisions = getRevisionsList(committedPath, supports15Ref);
    if (revisions == null) {
      return null;
    }
    return new MyHistorySession(revisions, committedPath, Boolean.TRUE.equals(supports15Ref.get()));
  }

  @Nullable
  private List<VcsFileRevision> getRevisionsList(final FilePath file, final Ref<Boolean> supports15Ref) throws VcsException {
    final SVNException[] exception = new SVNException[1];
    final ArrayList<VcsFileRevision> result = new ArrayList<VcsFileRevision>();

    Runnable command = new Runnable() {
      public void run() {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setText(SvnBundle.message("progress.text2.collecting.history", file.getName()));
        }
        try {
          if (myURL == null) {
            collectLogEntries(indicator, file, exception, result, supports15Ref);
          } else {
            collectLogEntriesForRepository(indicator, result, supports15Ref);
          }
        }
        catch(SVNCancelException ex) {
          throw new ProcessCanceledException(ex);
        }
        catch (SVNException e) {
          exception[0] = e;
        }
      }
    };

    command.run();

    if (exception[0] != null) {
      throw new VcsException(exception[0]);
    }
    return result;
  }

  private void collectLogEntries(final ProgressIndicator indicator, FilePath file, SVNException[] exception,
                                 final ArrayList<VcsFileRevision> result, final Ref<Boolean> supports15Ref) throws SVNException {
    SVNWCClient wcClient = myVcs.createWCClient();
    SVNInfo info = wcClient.doInfo(new File(file.getIOFile().getAbsolutePath()), SVNRevision.WORKING);
    wcClient.setEventHandler(new ISVNEventHandler() {
      public void handleEvent(SVNEvent event, double progress) throws SVNException {
      }

      public void checkCancelled() throws SVNCancelException {
        indicator.checkCanceled();
      }
    });
    if (info == null || info.getRepositoryRootURL() == null) {
        exception[0] = new SVNException(SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "File ''{0}'' is not under version control", file.getIOFile()));
        return;
    }
    final String url = info.getURL() == null ? null : info.getURL().toString();
    String relativeUrl = url;
    final SVNURL repoRootURL = info.getRepositoryRootURL();

    final String root = repoRootURL.toString();
    if (url != null && url.startsWith(root)) {
      relativeUrl = url.substring(root.length());
    }
    if (indicator != null) {
      indicator.setText2(SvnBundle.message("progress.text2.changes.establishing.connection", url));
    }
    final SVNRevision pegRevision = info.getRevision();
    SVNLogClient client = myVcs.createLogClient();

    final boolean supports15 = SvnUtil.checkRepositoryVersion15(myVcs, root);
    supports15Ref.set(supports15);
    client.doLog(new File[]{new File(file.getIOFile().getAbsolutePath())}, SVNRevision.HEAD, SVNRevision.create(1), SVNRevision.UNDEFINED,
                 false, true, supports15, 0, null,
                 new MyLogEntryHandler(url, pegRevision, relativeUrl, result));
  }

  private void collectLogEntriesForRepository(final ProgressIndicator indicator, final ArrayList<VcsFileRevision> result,
                                              final Ref<Boolean> supports15Ref) throws SVNException {
    if (indicator != null) {
      indicator.setText2(SvnBundle.message("progress.text2.changes.establishing.connection", myURL.toString()));
    }
    SVNWCClient wcClient = myVcs.createWCClient();
    SVNInfo info = wcClient.doInfo(myURL, SVNRevision.UNDEFINED, SVNRevision.HEAD);
    final String root = info.getRepositoryRootURL().toString();
    String url = myURL.toString();
    String relativeUrl = url;
    if (url.startsWith(root)) {
      relativeUrl = url.substring(root.length());
    }
    SVNLogClient client = myVcs.createLogClient();
    final boolean supports15 = SvnUtil.checkRepositoryVersion15(myVcs, root);
    supports15Ref.set(supports15);
    client.doLog(myURL, new String[] {}, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNRevision.create(1), false, true, supports15, 0, null,
                 new RepositoryLogEntryHandler(url, SVNRevision.UNDEFINED, relativeUrl, result));
  }

  public String getHelpId() {
    return null;
  }

  @Nullable
  public VcsRevisionNumber getCurrentRevision(FilePath file) {
    if (myRevision != null) {
      return new SvnRevisionNumber(myRevision);
    }
    try {
      SVNWCClient wcClient = myVcs.createWCClient();
      SVNInfo info = wcClient.doInfo(new File(file.getPath()).getAbsoluteFile(), SVNRevision.WORKING);
      if (info != null) {
        return new SvnRevisionNumber(info.getCommittedRevision());
      } else {
        return null;
      }
    }
    catch (SVNException e) {
      return null;
    }
  }

  public AnAction[] getAdditionalActions(final FileHistoryPanel panel) {
    return new AnAction[]{new ShowAllSubmittedFilesAction(), new MergeSourceDetailsAction()};
  }

  public boolean isDateOmittable() {
    return false;
  }

  private class MyLogEntryHandler implements ISVNLogEntryHandler {
    private final ProgressIndicator myIndicator;
    private String myLastPath;
    protected final ArrayList<VcsFileRevision> myResult;
    private final SVNRevision myPegRevision;
    private final String myUrl;
    private int myMergeLevel;

    public MyLogEntryHandler(final String url, final SVNRevision pegRevision, String lastPath, final ArrayList<VcsFileRevision> result) {
      myLastPath = lastPath;
      myIndicator = ProgressManager.getInstance().getProgressIndicator();
      myResult = result;
      myPegRevision = pegRevision;
      myUrl = url;
      myMergeLevel = -1;
    }

    public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
      if (myIndicator != null) {
        if (myIndicator.isCanceled()) {
          SVNErrorManager.cancel(SvnBundle.message("exception.text.update.operation.cancelled"), SVNLogType.DEFAULT);
        }
        myIndicator.setText2(SvnBundle.message("progress.text2.revision.processed", logEntry.getRevision()));
      }
      String copyPath = null;
      SVNLogEntryPath entryPath = (SVNLogEntryPath)logEntry.getChangedPaths().get(myLastPath);
      if (entryPath != null) {
        copyPath = entryPath.getCopyPath();
      }
      else {
        String path = SVNPathUtil.removeTail(myLastPath);
        while(path.length() > 0) {
          entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(path);
          if (entryPath != null) {
            String relativePath = myLastPath.substring(entryPath.getPath().length());
            copyPath = entryPath.getCopyPath() + relativePath;
            break;
          }
          path = SVNPathUtil.removeTail(path);
        }
      }
      if (copyPath != null) {
        myLastPath = copyPath;
      }
      addResultRevision(logEntry, copyPath);
    }

    protected void addResultRevision(final SVNLogEntry logEntry, final String copyPath) {
      if (logEntry.getRevision() < 0) {
        -- myMergeLevel;
        return;
      }
      final SvnFileRevision revision = createRevision(logEntry, copyPath);
      if (myMergeLevel >= 0) {
        addToListByLevel((SvnFileRevision) myResult.get(myResult.size() - 1), revision, myMergeLevel);
      } else {
        myResult.add(revision);
      }
      if (logEntry.hasChildren()) {
        ++ myMergeLevel;
      }
    }

    private void addToListByLevel(final SvnFileRevision revision, final SvnFileRevision revisionToAdd, final int level) {
      if (level < 0) {
        return;
      }
      if (level == 0) {
        revision.addMergeSource(revisionToAdd);
        return;
      }
      final List<SvnFileRevision> sources = revision.getMergeSources();
      if (! sources.isEmpty()) {
        addToListByLevel(sources.get(sources.size() - 1), revisionToAdd, level - 1);
      }
    }

    protected SvnFileRevision createRevision(final SVNLogEntry logEntry, final String copyPath) {
      Date date = logEntry.getDate();
      String author = logEntry.getAuthor();
      String message = logEntry.getMessage();
      SVNRevision rev = SVNRevision.create(logEntry.getRevision());
      return new SvnFileRevision(myVcs, myPegRevision, rev, myUrl, author, date, message, copyPath);
    }
  }

  private class RepositoryLogEntryHandler extends MyLogEntryHandler {
    public RepositoryLogEntryHandler(final String url, final SVNRevision pegRevision, String lastPath, final ArrayList<VcsFileRevision> result) {
      super(url, pegRevision, lastPath, result);
    }

    @Override
    protected SvnFileRevision createRevision(final SVNLogEntry logEntry, final String copyPath) {
      return new SvnFileRevision(myVcs, SVNRevision.UNDEFINED, logEntry, myURL.toString(), copyPath);
    }
  }

  private class MergeSourceColumnInfo extends ColumnInfo<VcsFileRevision, VcsFileRevision> {
    private final MergeSourceRenderer myRenderer;

    private MergeSourceColumnInfo(final MyHistorySession session) {
      super("Merge Sources");
      myRenderer = new MergeSourceRenderer(session);
    }

    @Override
    public TableCellRenderer getRenderer(final VcsFileRevision vcsFileRevision) {
      return myRenderer;
    }

    public VcsFileRevision valueOf(final VcsFileRevision vcsFileRevision) {
      return (VcsFileRevision) vcsFileRevision;
    }

    public String getText(final VcsFileRevision vcsFileRevision) {
      return myRenderer.getText(vcsFileRevision);
    }

    @Override
    public int getAdditionalWidth() {
      return 20;
    }

    @Override
    public String getPreferredStringValue() {
      return "1234567, 1234567, 1234567";
    }
  }

  private static final Object MERGE_SOURCE_DETAILS_TAG = new Object();

  private class MergeSourceDetailsLinkListener extends TableLinkMouseListener {
    private final VirtualFile myFile;
    private final Object myTag;

    private MergeSourceDetailsLinkListener(final Object tag, final VirtualFile file) {
      myTag = tag;
      myFile = file;
    }

    public void mouseClicked(final MouseEvent e) {
      if (e.getButton() == 1 && !e.isPopupTrigger()) {
        Object tag = getTagAt(e);
        if (tag == myTag) {
          final SvnFileRevision revision = getSelectedRevision(e);
          if (revision != null) {
            SvnMergeSourceDetails.showMe(myVcs.getProject(), revision, myFile);
          }
        }
      }
    }

    @Nullable
    private SvnFileRevision getSelectedRevision(final MouseEvent e) {
      JTable table = (JTable)e.getSource();
      int row = table.rowAtPoint(e.getPoint());
      int column = table.columnAtPoint(e.getPoint());

      final Object value = table.getModel().getValueAt(row, column);
      if (value instanceof SvnFileRevision) {
        return (SvnFileRevision) value;
      }
      return null;
    }

    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      Object tag = getTagAt(e);
      if (tag == myTag) {
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
      else {
        table.setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  private class MergeSourceRenderer extends ColoredTableCellRenderer {
    private MergeSourceDetailsLinkListener myListener;
    private final VirtualFile myFile;

    private MergeSourceRenderer(final MyHistorySession session) {
      myFile = session.getCommittedPath().getVirtualFile();
    }

    public String getText(final VcsFileRevision value) {
      if (! (value instanceof SvnFileRevision)) return "";
      final SvnFileRevision revision = (SvnFileRevision) value;
      final List<SvnFileRevision> mergeSources = revision.getMergeSources();
      if (mergeSources.isEmpty()) {
        return "";
      }
      final StringBuilder sb = new StringBuilder();
      for (SvnFileRevision source : mergeSources) {
        if (sb.length() != 0) {
          sb.append(", ");
        }
        sb.append(source.getRevisionNumber().asString());
        if (! source.getMergeSources().isEmpty()) {
          sb.append("*");
        }
      }
      return sb.toString();
    }

    protected void customizeCellRenderer(final JTable table,
                                         final Object value,
                                         final boolean selected,
                                         final boolean hasFocus,
                                         final int row,
                                         final int column) {
      if (myListener == null) {
        myListener = new MergeSourceDetailsLinkListener(MERGE_SOURCE_DETAILS_TAG, myFile);
        myListener.install(table);
      }
      if (! (value instanceof SvnFileRevision)) {
        append("", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        return;
      }
      final SvnFileRevision revision = (SvnFileRevision) value;
      final String text = getText(revision);
      if (text.length() == 0) {
        append("", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        return;
      }

      append(cutString(text, table.getCellRect(row, column, false).getWidth()), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    private String cutString(final String text, final double value) {
      final FontMetrics m = getFontMetrics(getFont());
      final Graphics g = getGraphics();

      if (m.getStringBounds(text, g).getWidth() < value) return text;

      final String dots = "...";
      final double dotsWidth = m.getStringBounds(dots, g).getWidth();
      if (dotsWidth >= value) {
        return dots;
      }

      for (int i = 1; i < text.length(); i++) {
        if ((m.getStringBounds(text, 0, i, g).getWidth() + dotsWidth) >= value) {
          if (i < 2) return dots;
          return text.substring(0, i - 1) + dots;
        }
      }
      return text;
    }
  }

  private static class CopyFromColumnInfo extends ColumnInfo<VcsFileRevision, String> {
    private final Icon myIcon = IconLoader.getIcon("/actions/menu-copy.png");
    private final ColoredTableCellRenderer myRenderer = new ColoredTableCellRenderer() {
      protected void customizeCellRenderer(final JTable table, final Object value, final boolean selected, final boolean hasFocus, final int row, final int column) {
        if (value instanceof String && ((String) value).length() > 0) {
          setIcon(myIcon);
          setToolTipText(SvnBundle.message("copy.column.tooltip", value));
        }
        else {
          setToolTipText("");
        }
      }
    };

    public CopyFromColumnInfo() {
      super(SvnBundle.message("copy.column.title"));
    }

    public String valueOf(final VcsFileRevision o) {
      return o instanceof SvnFileRevision ? ((SvnFileRevision) o).getCopyFromPath() : "";
    }

    @Override
    public TableCellRenderer getRenderer(final VcsFileRevision vcsFileRevision) {
      return myRenderer;
    }

    @Override
    public String getMaxStringValue() {
      return SvnBundle.message("copy.column.title");
    }

    @Override
    public int getAdditionalWidth() {
      return 6;
    }
  }
}
