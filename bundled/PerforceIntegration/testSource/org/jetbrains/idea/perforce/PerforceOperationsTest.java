package org.jetbrains.idea.perforce;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.operations.P4AddOperation;
import org.jetbrains.idea.perforce.operations.P4DeleteOperation;
import org.jetbrains.idea.perforce.operations.P4EditOperation;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class PerforceOperationsTest extends PerforceTestCase {
  @Test
  public void testAddOperation() throws Exception {
    VirtualFile fileToAdd = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
      public VirtualFile compute() {
        try {
          return myWorkingCopyDir.createChildData(this, "a.txt");
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
    new P4AddOperation("Default", fileToAdd).execute(myProject);
    verifyOpened("a.txt", "add");
  }

 /* @Test
  public void testHashInHave() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    submitFile("//depot/a.txt");
    final File wrongFile = new File(myWorkingCopyDir.getPath(), ".net.2009-09-04T09-52-03.#6244-498293954811245.log.debug.txt");
    final boolean created = wrongFile.createNewFile();
    Assert.assertTrue(created);

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(fileToEdit);
    Assert.assertNotNull(connection);

    try {
      PerforceRunner.getInstance(myProject).have(P4File.create(wrongFile), connection, true, new Consumer<String>() {
        public void consume(final String s) {
          System.out.println("received: " + s);
        }
      });
    }
    catch (VcsException e) {
      Assert.assertTrue(false);
    }
  } */

  @Test
  public void testEditOperation() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    submitFile("//depot/a.txt");
    new P4EditOperation("Default", fileToEdit).execute(myProject);
    verifyOpened("a.txt", "edit");
  }

  @Test
  public void testDeleteOperation() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(fileToEdit);
    submitFile("//depot/a.txt");
    new P4DeleteOperation("Default", filePath).execute(myProject);
    verifyOpened("a.txt", "delete");
  }

  @Test
  public void testRenameAddedFile() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile fileToAdd = createFileInCommand("a.txt", null);
    verifyOpened("a.txt", "add");

    renameFileInCommand(fileToAdd, "b.txt");

    final List<String> files = getFilesInDefaultChangelist();
    Assert.assertEquals(1, files.size());
    String file = files.get(0);
    Assert.assertEquals("//depot/b.txt\t# add", file);

    final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
    changeListManager.ensureUpToDate(false);
    final List<Change> changes = new ArrayList<Change>(changeListManager.getDefaultChangeList().getChanges());
    Assert.assertEquals(1, changes.size());
    verifyChange(changes.get(0), null, "b.txt");
  }

  @Test
  public void testRenamePackage() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile dir = createDirInCommand(myWorkingCopyDir, "child");
    final VirtualFile child = createFileInCommand(dir, "a.txt", "test");
    submitFile("//depot/child/a.txt");

    new P4EditOperation("Default", child).execute(myProject);
    renameFileInCommand(dir, "newchild");

    Assert.assertFalse(new File(myWorkingCopyDir.getPath(), "child").exists());   // IDEADEV-18837
    final File newChildDir = new File(myWorkingCopyDir.getPath(), "newchild");
    Assert.assertTrue(newChildDir.exists());
    Assert.assertTrue(new File(newChildDir, "a.txt").canWrite());                 // IDEADEV-18783
    final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
    changeListManager.ensureUpToDate(false);
    final List<Change> changes = new ArrayList<Change>(changeListManager.getDefaultChangeList().getChanges());
    Assert.assertEquals(1, changes.size());
    verifyChange(changes.get(0), "child\\a.txt", "newchild\\a.txt");
  }

  @Test
  public void testDeleteAddedFile() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    Assert.assertEquals(1, getFilesInDefaultChangelist().size());
    deleteFileInCommand(fileToEdit);
    final List<String> files = getFilesInDefaultChangelist();
    Assert.assertEquals(0, files.size());
  }

  @Test
  public void testDeleteDirWithAddedFiles() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile dir = createDirInCommand(myWorkingCopyDir, "test");
    createFileInCommand(dir, "a.txt", null);
    Assert.assertEquals(1, getFilesInDefaultChangelist().size());
    deleteFileInCommand(dir);
    Assert.assertEquals(0, getFilesInDefaultChangelist().size());
  }
}