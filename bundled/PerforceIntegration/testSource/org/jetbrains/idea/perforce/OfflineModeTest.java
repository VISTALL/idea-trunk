package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FileContentUtil;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.operations.P4EditOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class OfflineModeTest extends PerforceTestCase {
  @Test
  public void testOfflineAdd() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    createFileInCommand("a.txt", null);
    goOnline();
    verifyOpened("a.txt", "add");
  }

  @Test
  public void testOfflineDelete() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile file = createAndSubmit("a.txt", "original content");

    PerforceSettings settings = PerforceSettings.getSettings(myProject);
    settings.disable();
    deleteFileInCommand(file);
    settings.enable();
    verifyOpened("a.txt", "delete");
  }

  @Test
  public void testOfflineChanges() throws Throwable {
    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        myWorkingCopyDir.createChildData(this, "a.txt");
      }
    }.execute().throwException();
    
    verify(runP4WithClient("add", new File(myClientRoot, "a.txt").toString()));
    ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
    changeListManager.ensureUpToDate(false);
    LocalChangeList list = changeListManager.getDefaultChangeList();
    Assert.assertEquals(1, list.getChanges().size());

    goOffline();
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    changeListManager.ensureUpToDate(false);
    list = changeListManager.getDefaultChangeList();
    Collection<Change> changes = list.getChanges();
    Assert.assertEquals(1, changes.size());
    Change c = changes.iterator().next();
    Assert.assertNull(c.getBeforeRevision());
    ContentRevision afterRevision = c.getAfterRevision();
    Assert.assertTrue(afterRevision instanceof CurrentContentRevision);
  }

  @Test
  public void testChangesForAddDoneOffline() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    createFileInCommand("a.txt", null);
    refreshChanges();
    Collection<Change> changes = ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges();
    Assert.assertEquals(1, changes.size());
  }

  private void refreshChanges() {
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    ChangeListManager.getInstance(myProject).ensureUpToDate(false);
  }

  @Test
  public void testChangesForEditDoneOffline() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    openForEdit(fileToEdit);
    setFileText(fileToEdit, "new content");
    refreshChanges();

    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision() instanceof PerforceCachingContentRevision);
    Assert.assertTrue(c.getAfterRevision() instanceof CurrentContentRevision);
    Assert.assertEquals("original content", c.getBeforeRevision().getContent());
    Assert.assertEquals("new content", c.getAfterRevision().getContent());
  }

  private void openForEdit(final VirtualFile fileToEdit) throws VcsException {
    PerforceVcs.getInstance(myProject).getEditFileProvider().editFiles(new VirtualFile[] { fileToEdit });
  }

  @Test
  public void testChangesForDeleteDoneOffline() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    deleteFileInCommand(fileToEdit);
    refreshChanges();

    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision() instanceof PerforceCachingContentRevision);
    Assert.assertNull(c.getAfterRevision());
    // TODO[yole]: implement when LocalVCS allows to retrieve content for deleted files
    //Assert.assertEquals("original content", c.getBeforeRevision().getContent());
  }

  @Test
  public void testChangesForRenameDoneOffline() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    renameFileInCommand(fileToEdit, "b.txt");
    refreshChanges();

    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision().getFile().getPath().endsWith("a.txt"));
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("b.txt"));
  }

  private Change getSingleChange() {
    Collection<Change> changes = ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges();
    Assert.assertEquals(1, changes.size());
    return changes.iterator().next();
  }

  @Test
  public void testOfflineRevertForOnlineEdit() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    openForEdit(fileToEdit);
    setFileText(fileToEdit, "new content");
    refreshChanges();
    Change c = getSingleChange();
    ensureContentCached(c);

    goOffline();
    refreshChanges();
    c = getSingleChange();
    rollbackChange(c);

    Assert.assertEquals("original content", getFileText(fileToEdit));
    Assert.assertFalse(fileToEdit.isWritable());
    refreshChanges();
    Assert.assertEquals(0, ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges().size());

    goOnline();
    Assert.assertEquals(0, getFilesInDefaultChangelist().size());
  }

  @Test
  public void testOfflineRevertForOfflineEdit() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");
    goOffline();
    openForEdit(fileToEdit);
    setFileText(fileToEdit, "new content");
    refreshChanges();

    Change c = getSingleChange();
    rollbackChange(c);
    Assert.assertEquals("original content", getFileText(fileToEdit));

    refreshChanges();
    Assert.assertEquals(0, ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges().size());
  }

  @Test
  public void testOfflineRevertForOnlineAdd() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    createFileInCommand("a.txt", "content");
    refreshChanges();

    goOffline();
    rollbackChange(getSingleChange());
    refreshChanges();
    Assert.assertEquals(0, ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges().size());

    goOnline();
    Assert.assertEquals(0, getFilesInDefaultChangelist().size());
  }

  @Test
  public void testOfflineRevertForOfflineAdd() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    createFileInCommand("a.txt", "content");
    refreshChanges();
    rollbackChange(getSingleChange());
    refreshChanges();
    Assert.assertEquals(0, ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges().size());
    Assert.assertEquals(0, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testOfflineRevertForOfflineRename() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    openForEdit(file);
    renameFileInCommand(file, "b.txt");

    refreshChanges();
    Change c = getSingleChange();
    rollbackChange(c);
    refreshChanges();

    Assert.assertEquals("a.txt", file.getName());
    Assert.assertEquals(0, ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges().size());
    Assert.assertEquals(0, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testOfflineDeleteAfterOfflineAdd() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);

    goOffline();
    final VirtualFile file = createFileInCommand("a.txt", "content");
    deleteFileInCommand(file);

    refreshChanges();
    Assert.assertEquals(0, ChangeListManager.getInstance(myProject).getDefaultChangeList().getChanges().size());
    Assert.assertEquals(0, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testMergeAddAndRename() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    final VirtualFile file = createFileInCommand("a.txt", "content");
    renameFileInCommand(file, "b.txt");

    refreshChanges();
    Change c = getSingleChange();
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("b.txt"));
    Assert.assertEquals(1, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testMergeTwoRenames() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    renameFileInCommand(file, "b.txt");
    renameFileInCommand(file, "c.txt");

    refreshChanges();
    Change c = getSingleChange();
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("c.txt"));
    Assert.assertEquals(1, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testCyclicRename() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    renameFileInCommand(file, "b.txt");
    renameFileInCommand(file, "a.txt");

    refreshChanges();
    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision().getFile().getPath().endsWith("a.txt"));
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("a.txt"));
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());
    Assert.assertTrue(pendingOps.get(0) instanceof P4EditOperation);
  }

  @Test
  public void testRevertCyclicRename() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    openForEdit(file);
    renameFileInCommand(file, "b.txt");
    setFileText(file, "new content");
    renameFileInCommand(file, "a.txt");

    refreshChanges();
    Change c = getSingleChange();
    rollbackChange(c);
    Assert.assertEquals("content", getFileText(file));
  }

  @Test
  public void testAddInChangelist() throws Exception {
    goOffline();
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    createFileInCommand("a.txt", "new content");
    switchToChangeList(oldDefaultList);

    refreshChanges();
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    final LocalChangeList list = clManager.findChangeList("Second");
    Assert.assertEquals(1, list.getChanges().size());

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testEditInChangelist() throws Exception {
    VirtualFile f = createAndSubmit("a.txt", "old content");

    goOffline();
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    openForEdit(f);
    switchToChangeList(oldDefaultList);

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testDeleteInChangelist() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile f = createAndSubmit("a.txt", "old content");

    goOffline();
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    deleteFileInCommand(f);
    switchToChangeList(oldDefaultList);

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testRenameInChangelist() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    renameFileInCommand(fileToEdit, "b.txt");
    switchToChangeList(oldDefaultList);

    goOnline();
    verifyOpenedInList("Second", "a.txt");
    verifyOpenedInList("Second", "b.txt");
  }

  @Test
  public void testReopen() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "original content");
    openForEdit(file);
    refreshChanges();
    getSingleChange();
    goOffline();
    refreshChanges();
    Change c = getSingleChange();

    ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    final LocalChangeList list = clManager.addChangeList("Second", "");
    clManager.moveChangesTo(list, new Change[] { c });

    refreshChanges();
    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    Assert.assertEquals(1, clManager.findChangeList("Second").getChanges().size());
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testMergeReopen() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "original content");
    refreshChanges();
    goOffline();
    openForEdit(file);

    refreshChanges();
    Change c = getSingleChange();
    ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    final LocalChangeList list = clManager.addChangeList("Second", "");
    clManager.moveChangesTo(list, new Change[] { c });

    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    Assert.assertEquals(1, clManager.findChangeList("Second").getChanges().size());
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());
  }

  @Test
  public void testMergeReopenRename() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "original content");
    refreshChanges();
    goOffline();
    openForEdit(file);
    renameFileInCommand(file, "b.txt");

    refreshChanges();
    Change c = getSingleChange();
    ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    final LocalChangeList list = clManager.addChangeList("Second", "");
    clManager.moveChangesTo(list, new Change[] { c });

    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    Assert.assertEquals(1, clManager.findChangeList("Second").getChanges().size());
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());
  }

  @Test
  public void testOfflineCopy() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createAndSubmit("a.txt", "content");
    goOffline();

    copyFileInCommand(file, "b.txt");
    goOnline();
    verifyOpened("b.txt", "add");
    ProcessOutput result = runP4WithClient("resolved");
    String stdout = result.getStdout().trim();
    Assert.assertTrue(stdout.endsWith("b.txt - branch from //depot/a.txt#1"));
  }

  private void goOffline() {
    PerforceSettings.getSettings(myProject).disable();
  }

  private void goOnline() {
    PerforceSettings.getSettings(myProject).enable();
  }

  private VirtualFile createAndSubmit(final String fileName, final String content) throws IOException {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile fileToEdit = createFileInCommand(fileName, content);
    submitFile("//depot/" + fileName);
    fileToEdit.refresh(false, false);
    return fileToEdit;
  }

  private void setFileText(final VirtualFile fileToEdit, final String newContent) {
    CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              FileContentUtil.setFileText(myProject, fileToEdit, newContent);
            }
            catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
    }, "", null);
  }

  private String getFileText(final VirtualFile fileToEdit) throws IOException {
    return CharsetToolkit.UTF8_CHARSET.decode(ByteBuffer.wrap(fileToEdit.contentsToByteArray())).toString();
  }

  private void ensureContentCached(final Change c) throws VcsException {
    final ContentRevision beforeRevision = c.getBeforeRevision();
    assert beforeRevision != null;
    beforeRevision.getContent();
  }

  private void rollbackChange(final Change c) {
    final List<VcsException> exceptions = new ArrayList<VcsException>();
    PerforceVcs.getInstance(myProject).getRollbackEnvironment().rollbackChanges(Collections.singletonList(c), exceptions,
                                                                                                      RollbackProgressListener.EMPTY);
    if (exceptions.size() > 0) {
      for(VcsException ex: exceptions) {
        ex.printStackTrace();
      }
      Assert.assertTrue("Unexpected exception: " + exceptions.get(0).toString(), false);
    }
  }

  private LocalChangeList switchToChangeList(final String name) {
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    final LocalChangeList list = clManager.addChangeList(name, "");
    final LocalChangeList oldDefaultList = clManager.getDefaultChangeList();
    clManager.setDefaultChangeList(list);
    return oldDefaultList;
  }

  private void switchToChangeList(final LocalChangeList oldDefaultList) {
    ChangeListManager.getInstance(myProject).setDefaultChangeList(oldDefaultList);
  }

  private void verifyOpenedInList(final String changeListName, final String path) throws IOException {
    ProcessOutput execResult = runP4WithClient("changes", "-i", "-t", "-s", "pending");
    final String stdout = execResult.getStdout().trim();

    Pattern pattern = Pattern.compile("Change (\\d+).+'(.+) '");
    Matcher m = pattern.matcher(stdout);
    Assert.assertTrue("Unexpected pending changes: " + stdout, m.matches());
    Assert.assertEquals(changeListName, m.group(2));

    execResult = runP4WithClient("describe", "-s", m.group(1));
    Assert.assertTrue(execResult.getStdout().contains("... //depot/" + path));
  }
}