package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.pending.DuringChangeListManagerUpdateTestScheme;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.perforce.PerforceChangeListHelper;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.jobs.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author yole
 */
public class PerforceChangeProviderTest extends PerforceTestCase {
  private DuringChangeListManagerUpdateTestScheme myScheme;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    // similiar to svn tests to be added
    myScheme = new DuringChangeListManagerUpdateTestScheme(myProject, myTempDirFixture.getTempDirPath());

    clearAll();
  }

  private void doTestVariants(final Runnable runnable, final boolean insideUpdate) throws Throwable {
    if (insideUpdate) {
      myScheme.doTest(runnable);
    } else {
      runnable.run();
    }
  }

  private void clearAll() throws IOException, VcsException {
    final List<String> files = getOpenedFiles();

    for (String file : files) {
      ProcessOutput result = runP4WithClient("revert", file);
      verify(result);
    }

    final Set<String> jobs = new HashSet<String>();
    final List<Long> lists = getLists();
    for (Long list : lists) {
      final Set<String> jobNames = new HashSet<String>(getAssociatedJobs(list));
      jobs.addAll(jobNames);
      for (String name : jobNames) {
        if (name.length() > 0) {
          unlinkJob(list, name);
        }
      }

      final ProcessOutput result = runP4(new String[]{"-c", "test", "change", "-d", "" + list}, null);
      verify(result);
    }

    for (String job : jobs) {
      if (job.length() > 0) {
        killJob(job);
      }
    }
  }

  private List<String> getOpenedFiles() throws IOException {
    final ProcessOutput opened = runP4(new String[]{"-c", "test", "opened"}, null);
    verify(opened);
    final String[] lines = opened.getStdout().split("\n");
    final List<String> files = new ArrayList<String>();
    for (String line : lines) {
      final int idx = line.indexOf('#');
      if (idx != -1) {
        files.add(line.substring(0, idx));
      }
    }
    return files;
  }

  @Override
  public void tearDown() throws Exception {
    clearAll();

    super.tearDown();
  }

  @Test
  public void testAddedFile() throws Throwable {
    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        myWorkingCopyDir.createChildData(this, "a.txt");
      }
    }.execute().throwException();

    verify(runP4WithClient("add", new File(myClientRoot, "a.txt").toString()));
    ChangeProvider changeProvider = PerforceVcs.getInstance(myProject).getChangeProvider();
    assert changeProvider != null;

    sleep3();

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    clManager.ensureUpToDate(false);

    final List<LocalChangeList> lists = clManager.getChangeLists();
    assert lists.size() == 1;
    final LocalChangeList list = lists.get(0);
    final Collection<Change> changes = list.getChanges();
    assert changes.size() == 1;
    Change change = changes.iterator().next();
    Assert.assertEquals(Change.Type.NEW, change.getType());
  }

  @Test
  public void testListAddedExternally() throws Throwable {
    testListAddedExternallyImpl(false);
  }
  
  @Test
  public void testListAddedExternallyDuringUpdate() throws Throwable {
    testListAddedExternallyImpl(true);
  }

  private void testListAddedExternallyImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);

    final String listName = "test new list";
    final Ref<Long> numberRef = new Ref<Long>();
    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          numberRef.set(createChangeList(listName, Arrays.asList("//depot/a.txt")));
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }
    assert ! numberRef.isNull();
    long listNumber = numberRef.get();
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get()}, listName, clManager);
  }

  private void addFile(final String subPath) throws IOException {
    verify(runP4WithClient("add", new File(myClientRoot, subPath).toString()));
  }

  @Test
  public void testPerforceCreatesList() throws Throwable {
    testPerforceCreatesListImpl(false);
  }

  @Test
  public void testPerforceCreatesListInUpdate() throws Throwable {
    testPerforceCreatesListImpl(true);
  }

  private void testPerforceCreatesListImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    final List<Long> before = getLists();

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);

    final String name = "added in IDEA";
    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          LocalChangeList list = clManager.addChangeList(name, null);
          clManager.moveChangesTo(list, new Change[] {clManager.getChange(refA.get())});
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }

    sleep3();

    final List<Long> after = getLists();

    assert before.size() == 0;
    assert after.size() == 1;

    final List<String> files = getFilesInList(after.get(0));
    assert files.size() == 1;
    assert files.get(0).startsWith("//depot/a.txt") : files.get(0);
  }

  @Test
  public void testCommentFromIdeaGoesIntoNative() throws Throwable {
    testCommentFromIdeaGoesInfoNativeImpl(false);
  }

  @Test
  public void testCommentFromIdeaGoesIntoNativeInUpdate() throws Throwable {
    testCommentFromIdeaGoesInfoNativeImpl(true);
  }

  private void testCommentFromIdeaGoesInfoNativeImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(nativeComment);

    long listNumber = createChangeList(convertor.getNativeDescription(), Arrays.asList("//depot/a.txt"));

    checkNative(listNumber, nativeComment);

    // synch
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get()}, convertor.getIdeaName(), clManager);
    final LocalChangeList list = clManager.findChangeList(convertor.getIdeaName());
    checkListComment(list, convertor.getIdeaComment());

    final String newName = "new name";
    final String newComment = "new name\n<--->\n>=***=<";

    // edit in idea
    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          clManager.editName(convertor.getIdeaName(), newName);
          clManager.editComment(newName, newComment);
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }

    sleep3();

    // check native description
    checkNative(listNumber, newComment);
  }

  @Test
  public void testNativeCommentGoesIntoIdea() throws Throwable {
    testNativeCommentGoesInfoIdeaImpl(false);
  }

  @Test
  public void testNativeCommentGoesIntoIdeaInUpdate() throws Throwable {
    testNativeCommentGoesInfoIdeaImpl(true);
  }

  private void testNativeCommentGoesInfoIdeaImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(nativeComment);

    final long listNumber = createChangeList(convertor.getNativeDescription(), Arrays.asList("//depot/a.txt"));

    checkNative(listNumber, nativeComment);

    // synch
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get()}, convertor.getIdeaName(), clManager);

    final String newComment = "new first line\nnew second line";
    final String newName = "new first line...";
    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          editListDescription(listNumber, newComment);
          checkNative(listNumber, newComment);
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }
    checkNative(listNumber, newComment);
    
    sleep3();
    ChangeListSynchronizer.getInstance(myProject).queueUpdateOpenedFiles();
    sleep3();

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get()}, newName, clManager);
    final LocalChangeList changeList = clManager.findChangeList(newName);
    checkListComment(changeList, newComment);
  }

  @Test
  public void testNativeMoveGoesIntoIdea() throws Throwable {
    testNativeMoveGoesIntoIdeaImpl(false);
  }

  @Test
  public void testNativeMoveGoesIntoIdeaInUpdate() throws Throwable {
    testNativeMoveGoesIntoIdeaImpl(true);
  }

  private void testNativeMoveGoesIntoIdeaImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(nativeComment);

    long listNumber = createChangeList(convertor.getNativeDescription(), Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNative(listNumber, nativeComment);

    // synch
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refB.get()}, convertor.getIdeaName(), clManager);
    final LocalChangeList list = clManager.findChangeList(convertor.getIdeaName());
    checkListComment(list, nativeComment);

    final String anotherName = "anotherName...";
    final String anotherComment = "anotherName\nanotherComment";

    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          final long newListNumber = createChangeList(anotherComment, Arrays.asList("//depot/a.txt"));
          checkNative(newListNumber, anotherComment);
          moveFile(newListNumber, "//depot/a.txt");
          final List<String> filesInList = getFilesInList(newListNumber);
          assert filesInList.size() == 1;
          assert filesInList.get(0).startsWith("//depot/a.txt");
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }
    /*final long newListNumber = createChangeList(anotherComment, Arrays.asList("//depot/a.txt"));
    checkNative(newListNumber, anotherName, anotherComment);
    moveFile(newListNumber, "//depot/a.txt");
    final List<String> filesInList = getFilesInList(newListNumber);
    assert filesInList.size() == 1;
    assert filesInList.get(0).startsWith("//depot/a.txt");*/

    sleep3();

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get()}, anotherName, clManager);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refB.get()}, convertor.getIdeaName(), clManager);
    final LocalChangeList listOld = clManager.findChangeList(convertor.getIdeaName());
    checkListComment(listOld, nativeComment);
    final LocalChangeList listNew = clManager.findChangeList(anotherName);
    checkListComment(listNew, anotherComment);
  }

  @Test
  public void testIdeaMoveGoesIntoNative() throws Throwable {
    testIdeaMoveGoesIntoNativeImpl(false);
  }

  @Test
  public void testIdeaMoveGoesIntoNativeInUpdate() throws Throwable {
    testIdeaMoveGoesIntoNativeImpl(true);
  }

  private void testIdeaMoveGoesIntoNativeImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(nativeComment);

    long listNumber = createChangeList(convertor.getNativeDescription(), Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNative(listNumber, nativeComment);

    // synch
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refB.get()}, convertor.getIdeaName(), clManager);
    final LocalChangeList list = clManager.findChangeList(convertor.getIdeaName());
    checkListComment(list, nativeComment);

    final String newName = "newName";
    final String newComment = "newName\nnewComment";
    // idea move
    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          final LocalChangeList newChangeList = clManager.addChangeList(newName, newComment);
          clManager.moveChangesTo(newChangeList, new Change[] {clManager.getChange(refA.get())});
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }
    clManager.ensureUpToDate(false);
    final LocalChangeList newChangeList = clManager.findChangeList(newName);
    assert newChangeList != null;
    assert newChangeList.getChanges().size() == 1;

    sleep3();

    final List<Long> listNumbers = getLists();
    listNumbers.remove(listNumber);
    assert ! listNumbers.isEmpty();
    final Long newNumber = listNumbers.get(0);

    checkNative(newNumber, newComment);

    final List<String> files = getFilesInList(newNumber);
    assert files.size() == 1;
    assert files.get(0).startsWith("//depot/a.txt") : files.get(0);
  }

  @Test
  public void testIdeaDeleteGoesIntoNative() throws Throwable {
    testIdeaDeleteGoesIntoNativeImpl(false);
  }

  @Test
  public void testIdeaDeleteGoesIntoNativeInUpdate() throws Throwable {
    testIdeaDeleteGoesIntoNativeImpl(true);
  }

  private void testIdeaDeleteGoesIntoNativeImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(nativeComment);

    long listNumber = createChangeList(convertor.getNativeDescription(), Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNative(listNumber, nativeComment);

    // synch
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refB.get()}, convertor.getIdeaName(), clManager);
    final LocalChangeList list = clManager.findChangeList(convertor.getIdeaName());
    checkListComment(list, nativeComment);

    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          clManager.removeChangeList(list.getName());
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }

    sleep3();

    final List<Long> listNumbers = getLists();
    assert listNumbers.isEmpty();

    final List<String> inDefault = getFilesInDefaultChangelist();
    assert inDefault.size() == 2;
    assert inDefault.get(0).startsWith("//depot/a.txt") || inDefault.get(1).startsWith("//depot/a.txt");
    assert inDefault.get(0).startsWith("//depot/b.txt") || inDefault.get(1).startsWith("//depot/b.txt");
  }

  @Test
  public void testNativeDeleteGoesIntoIdea() throws Throwable {
    testNativeDeleteGoesIntoIdeaImpl(false);
  }

  @Test
  public void testNativeDeleteGoesIntoIdeaInUpdate() throws Throwable {
    testNativeDeleteGoesIntoIdeaImpl(true);
  }

  private void testNativeDeleteGoesIntoIdeaImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(nativeComment);

    final long listNumber = createChangeList(convertor.getNativeDescription(), Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNative(listNumber, nativeComment);

    // synch
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refB.get()}, convertor.getIdeaName(), clManager);
    final LocalChangeList list = clManager.findChangeList(convertor.getIdeaName());
    checkListComment(list, nativeComment);

    final Ref<Throwable> refThrowable = new Ref<Throwable>();
    doTestVariants(new Runnable() {
      public void run() {
        try {
          moveToDefault("//depot/a.txt");
          moveToDefault("//depot/b.txt");
          deleteList(listNumber);
        }
        catch (Throwable e) {
          refThrowable.set(e);
        }
      }
    }, inUpdate);
    if (! refThrowable.isNull()) {
      throw refThrowable.get();
    }

    sleep3();

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refB.get()}, clManager.getDefaultListName(), clManager);
  }

  @Test
  public void testCreateListWithJobInNative() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");

    final String description = "123";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(description);
    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String jobName = "justdoit";
    final long listNumber = createChangeList(description, Arrays.asList("//depot/a.txt"));
    checkNative(listNumber, description);

    createJob(jobName, "user", "open", "a task");
    linkJob(listNumber, jobName);

    sleep3();

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    clManager.ensureUpToDate(false);

    final LocalChangeList changeList = clManager.findChangeList(convertor.getIdeaName());
    Assert.assertNotNull(changeList);
    Assert.assertEquals(changeList.getComment(), changeList.getComment(), convertor.getIdeaComment());
  }

  @Test
  public void testJobsLoading() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");

    final String description = "123";
    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(description);
    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final String jobName = "justdoit";
    final String secondJob = "another";
    final long listNumber = createChangeList(description, Arrays.asList("//depot/a.txt"));
    checkNative(listNumber, description);

    createJob(jobName, "user", "open", "a task");
    linkJob(listNumber, jobName);

    createJob(secondJob, "user", "open", "a task");
    linkJob(listNumber, secondJob);

    sleep3();

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    clManager.ensureUpToDate(false);

    final LocalChangeList changeList = clManager.findChangeList(convertor.getIdeaName());
    Assert.assertNotNull(changeList);

    final JobDetailsLoader loader = new JobDetailsLoader(myProject);
    final Map<ConnectionKey, P4JobsLogicConn> connMap = new HashMap<ConnectionKey, P4JobsLogicConn>();
    final Map<ConnectionKey, java.util.List<PerforceJob>> perforceJobs = new HashMap<ConnectionKey, java.util.List<PerforceJob>>();
    loader.loadJobsForList(changeList, connMap, perforceJobs);

    final List<String> jobs = new ArrayList<String>();
    final Collection<List<PerforceJob>> listCollection = perforceJobs.values();
    for (List<PerforceJob> jobList : listCollection) {
      for (PerforceJob job : jobList) {
        jobs.add(job.getName());
      }
    }

    Assert.assertEquals(jobs.size(), 2);
    Assert.assertTrue(jobs.contains(jobName));
    Assert.assertTrue(jobs.contains(secondJob));
  }

  @Test
  public void testJobSearchAll() throws Throwable {
    final String[][] data = {{"job001", "anna", "open", "once upon a time"},
                             {"job002", "ivan", "open", "not a job actually"},
                             {"yt-abs-3284", "john.smith", "closed", "time to say"}};

    for (String[] strings : data) {
      createJob(strings[0], strings[1], strings[2], strings[3]);
    }

    final JobsWorker worker = new JobsWorker(myProject);

    final FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.jobname, "*");

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(myProject.getBaseDir());
    Assert.assertTrue(connection != null);

    final PerforceJobSpecification jobsSpec = worker.getSpec(connection);
    final List<PerforceJob> perforceJobs = worker.getJobs(jobsSpec, spec, connection, new ConnectionKey("localhost", "test", "test"));

    Assert.assertEquals(perforceJobs.size(), 3);
    // in alphabet order
    Assert.assertEquals(perforceJobs.get(0).getName(), "job001");
  }

  @Test
  public void testJobSearchByName() throws Throwable {
    final String[][] data = {{"job001", "anna", "open", "once upon a time"},
                             {"job002", "ivan", "open", "not a job actually"},
                             {"yt-abs-3284", "john.smith", "closed", "time to say"}};

    for (String[] strings : data) {
      createJob(strings[0], strings[1], strings[2], strings[3]);
    }

    final JobsWorker worker = new JobsWorker(myProject);

    final FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.jobname, "job*");

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(myProject.getBaseDir());
    Assert.assertTrue(connection != null);

    final PerforceJobSpecification jobsSpec = worker.getSpec(connection);
    final List<PerforceJob> perforceJobs = worker.getJobs(jobsSpec, spec, connection, new ConnectionKey("localhost", "test", "test"));

    Assert.assertEquals(perforceJobs.size(), 2);
    // in alphabet order
    Assert.assertEquals(perforceJobs.get(0).getName(), "job001");
    Assert.assertEquals(perforceJobs.get(1).getName(), "job002");
  }
  
  @Test
  public void testJobSearchBySeveralFields() throws Throwable {
    final String[][] data = {{"job001", "anna", "open", "once upon a time"},
                             {"job002", "ivan", "open", "not a job actually"},
                             {"yt-abs-3284", "john.smith", "closed", "time to say"}};

    for (String[] strings : data) {
      createJob(strings[0], strings[1], strings[2], strings[3]);
    }

    final JobsWorker worker = new JobsWorker(myProject);

    final FullSearchSpecificator spec = new FullSearchSpecificator();
    spec.addStandardConstraint(FullSearchSpecificator.Parts.description, "once upon*");
    spec.addStandardConstraint(FullSearchSpecificator.Parts.status, "closed");

    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(myProject.getBaseDir());
    Assert.assertTrue(connection != null);

    final PerforceJobSpecification jobsSpec = worker.getSpec(connection);
    final List<PerforceJob> perforceJobs = worker.getJobs(jobsSpec, spec, connection, new ConnectionKey("localhost", "test", "test"));

    Assert.assertEquals(perforceJobs.size(), 0);

    final FullSearchSpecificator spec2 = new FullSearchSpecificator();
    spec2.addStandardConstraint(FullSearchSpecificator.Parts.description, "once upon*");
    spec2.addStandardConstraint(FullSearchSpecificator.Parts.user, "a*a");

    final List<PerforceJob> perforceJobs2 = worker.getJobs(jobsSpec, spec2, connection, new ConnectionKey("localhost", "test", "test"));
    Assert.assertEquals(perforceJobs2.size(), 1);

    // in alphabet order
    Assert.assertEquals(perforceJobs2.get(0).getName(), "job001");
  }

  private static class SubTree {
    private VirtualFile myOuterDir;
    private VirtualFile myOuterFile;
    private VirtualFile myRootDir;
    private VirtualFile myInnerFile;
    private VirtualFile myNonVersionedUpper;

    private SubTree(final VirtualFile base) throws Throwable {
      new WriteAction() {
        protected void run(final Result result) throws Throwable {
          myOuterDir = base.createChildDirectory(this, "outer");
          myOuterFile = myOuterDir.createChildData(this, "outer.txt");
          myRootDir = myOuterDir.createChildDirectory(this, "root");
          myInnerFile = myRootDir.createChildData(this, "inner.txt");
          myNonVersionedUpper = base.createChildData(this, "nonVersioned.txt");
        }
      }.execute().throwException();
    }

    public List<String> getForAdds() {
      return Arrays.asList("outer/outer.txt", "outer/root/inner.txt");
    }
  }

  /*public void testPerforceVcsRootAbove() throws Throwable {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);

    final SubTree subTree = new SubTree(myWorkingCopyDir);

    final List<String> paths = subTree.getForAdds();
    final String[] pathsForSubmit = new String[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      final String path = paths.get(i);
      addFile(path);
      pathsForSubmit[i] = "//depot/" + path;
    }

    submitFile(pathsForSubmit);

    runP4WithClient("edit", new File(subTree.myOuterFile.getPath()).getAbsolutePath());
    runP4WithClient("edit", new File(subTree.myInnerFile.getPath()).getAbsolutePath());

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {subTree.myOuterFile, subTree.myInnerFile},
      clManager.getDefaultListName(), clManager);
  }*/

  /*public void testFakeScopeDontBreakTheView() throws Throwable {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);

    final SubTree subTree = new SubTree(myWorkingCopyDir);

    final List<String> paths = subTree.getForAdds();
    final String[] pathsForSubmit = new String[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      final String path = paths.get(i);
      addFile(path);
      pathsForSubmit[i] = "//depot/" + path;
    }

    submitFile(pathsForSubmit);

    runP4WithClient("edit", new File(subTree.myOuterFile.getPath()).getAbsolutePath());
    runP4WithClient("edit", new File(subTree.myInnerFile.getPath()).getAbsolutePath());

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {subTree.myOuterFile, subTree.myInnerFile},
      clManager.getDefaultListName(), clManager);

    VcsDirtyScopeManagerImpl.getInstance(myProject).fileDirty(subTree.myNonVersionedUpper);
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {subTree.myOuterFile, subTree.myInnerFile},
      clManager.getDefaultListName(), clManager);
  }*/

  @Test
  public void testNotSynchronizedInitially() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<VirtualFile>();
    final Ref<VirtualFile> refB = new Ref<VirtualFile>();
    final Ref<VirtualFile> refC = new Ref<VirtualFile>();
    final Ref<VirtualFile> refD = new Ref<VirtualFile>();

    new WriteAction() {
      protected void run(final Result result) throws Throwable {
        refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
        refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
        refC.set(myWorkingCopyDir.createChildData(this, "c.txt"));
        refD.set(myWorkingCopyDir.createChildData(this, "d.txt"));
      }
    }.execute().throwException();

    addFile("a.txt");
    addFile("b.txt");
    addFile("c.txt");
    addFile("d.txt");

    PerforceManager.getInstance(myProject).doAsynchronousUpdates();

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refB.get(), refC.get(), refD.get()}, clManager.getDefaultListName(), clManager);

    final String name1 = "idea1";
    final String name2 = "idea2";
    // do synchronized:
    final LocalChangeList idea1list = clManager.addChangeList(name1, null);
    final LocalChangeList idea2list = clManager.addChangeList(name2, null);

    clManager.moveChangesTo(idea1list, new Change[]{clManager.getChange(refA.get()), clManager.getChange(refB.get())});
    clManager.moveChangesTo(idea2list, new Change[]{clManager.getChange(refC.get()), clManager.getChange(refD.get())});

    sleep3();

    final List<Long> lists = getLists();
    assert lists.size() == 2;
    final List<String> files1 = getFilesInList(lists.get(0));
    final List<String> files2 = getFilesInList(lists.get(1));

    assert files1.size() == 2;
    assert files2.size() == 2;

    if (files1.get(0).startsWith("//depot/a.txt") || files1.get(1).startsWith("//depot/a.txt")) {
      assertAandB(files1, "//depot/a.txt", "//depot/b.txt");
      assertAandB(files2, "//depot/c.txt", "//depot/d.txt");
    } else {
      assertAandB(files1, "//depot/c.txt", "//depot/d.txt");
      assertAandB(files2, "//depot/a.txt", "//depot/b.txt");
    }

    // do miss - synchronized

    final String p41 = "p41\ncomment";
    final String p42 = "p42\ncomment2";

    final long p41Number = createChangeList(p41, null);
    moveFile(p41Number, "//depot/a.txt");
    moveFile(p41Number, "//depot/c.txt");

    final long p42Number = createChangeList(p42, null);
    moveFile(p42Number, "//depot/b.txt");
    moveFile(p42Number, "//depot/d.txt");

    sleep3();

    // now synchronize
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate(false);

    final LocalChangeList p41Idea = clManager.findChangeList("p41...");
    final LocalChangeList p42Idea = clManager.findChangeList("p42...");

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refA.get(), refC.get()}, "p41...", clManager);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {refB.get(), refD.get()}, "p42...", clManager);
    checkListComment(p41Idea, p41);
    checkListComment(p42Idea, p42);
  }

  private void assertAandB(final List<String> files, final String stringA, final String stringB) {
    if (files.get(0).startsWith(stringA)) {
      assert files.get(1).startsWith(stringB);
    } else {
      assert files.get(0).startsWith(stringB);
      assert files.get(1).startsWith(stringA);
    }
  }

  private void sleep3() {
    try {
      Thread.sleep(3000);
    }
    catch (InterruptedException e) {
      //
    }
  }

  private void deleteList(final long list) throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c", "test", "change", "-d", "" + list}, null);
    verify(result);
  }

  private void moveToDefault(final String filePath) throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c", "test", "reopen", "-c", "default", filePath}, null);
    verify(result);
  }

  private void moveFile(final long newListNumber, final String filePath) throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c", "test", "reopen", "-c", "" + newListNumber, filePath}, null);
    verify(result);
  }

  private void checkListComment(final LocalChangeList list, final String etalon) {
    assert list != null && etalon.trim().equals(list.getComment().trim()) : (list == null ? null : list.getComment().trim());
  }

  private void checkNative(final long number, final String comment) throws IOException {
    final String nativeDescription = getListDescription(number);
    assert nativeDescription != null;

    assert nativeDescription.trim().equals(comment.trim());
  }

  private List<Long> getLists() throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c", "test", "changes", "-s", "pending"}, null);
    verify(result);
    final String[] strings = result.getStdout().split("\n");
    final List<Long> numbers = new ArrayList<Long>();
    for (String string : strings) {
      if (string.length() > 0) {
        long number = PerforceChangeListHelper.parseCreatedListNumber(string);
        assert number != -1;
        if (! numbers.contains(number)) {
          numbers.add(number);
        }
      }
    }
    return numbers;
  }

  private long createChangeList(final String description, final List<String> files) throws VcsException, IOException {
    final ProcessOutput result = runP4(new String[]{"-c", "test", "change", "-i"}, PerforceChangeListHelper.createSpecification(description, -1, files, null, null,
                                                                                                                            false));
    verify(result);
    return PerforceChangeListHelper.parseCreatedListNumber(result.getStdout());
  }

  private void createJob(final String name, final String user, final String status, final String description) throws IOException {
    final Map<String, List<String>> mapRepresentation = new HashMap<String, List<String>>();
    mapRepresentation.put(PerforceRunner.JOB, Collections.singletonList(name));
    mapRepresentation.put(PerforceRunner.USER, Collections.singletonList(user));
    mapRepresentation.put(PerforceRunner.STATUS, Collections.singletonList(status));
    mapRepresentation.put(PerforceRunner.DESCRIPTION, PerforceRunner.processDescription(description));

    final String spec = PerforceRunner.createStringFormRepresentation(mapRepresentation).toString();

    final ProcessOutput result = runP4(new String[]{"-c","test","job","-i"}, spec);
    verify(result);
  }

  private void linkJob(final long number, final String name) throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c","test","fix","-c", "" + number, name}, null);
    verify(result);
  }

  private void unlinkJob(final long number, final String name) throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c","test","fix","-d","-c", "" + number, name}, null);
    verify(result);
  }

  private void killJob(final String name) throws IOException {
    final ProcessOutput result = runP4(new String[]{"-c","test","job","-d", name}, null);
    verify(result);
  }

  private List<String> getAssociatedJobs(final long number) throws VcsException, IOException {
    final ProcessOutput result = runP4(new String[]{"-c","test","fixes","-c", "" + number}, null);
    verify(result);
    final FixesOutputParser parser = new FixesOutputParser(Arrays.asList(result.getStdout().split("\n")));
    return parser.parseJobNames();
  }
}
