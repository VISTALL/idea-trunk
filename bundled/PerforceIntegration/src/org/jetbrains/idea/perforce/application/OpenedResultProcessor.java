/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangelistBuilder;
import com.intellij.util.ThrowableConvertor;
import org.jetbrains.idea.perforce.perforce.PerforceAbstractChange;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.ResolvedFile;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.util.*;

public class OpenedResultProcessor {
  private final ChangelistBuilder myBuilder;
  private final P4Connection myConnection;
  private final ChangeCreator myChangeCreator;
  private final LocalPathsSet myResolvedWithConflicts;
  private final ResolvedFilesWrapper myResolvedFiles;
  private final ThrowableConvertor<PerforceChange, ChangeList, VcsException> myChangelistCalculator;

  public OpenedResultProcessor(final P4Connection connection, final ChangeCreator changeCreator, final ChangelistBuilder builder,
                               final LocalPathsSet resolvedWithConflicts, final ResolvedFilesWrapper resolvedFiles,
                               final ThrowableConvertor<PerforceChange, ChangeList, VcsException> changelistCalculator) {
    myConnection = connection;
    myChangeCreator = changeCreator;
    myBuilder = builder;
    myResolvedWithConflicts = resolvedWithConflicts;
    myResolvedFiles = resolvedFiles;
    myChangelistCalculator = changelistCalculator;
  }

  private abstract class MyAbstractProcessor {
    private final boolean myRequireChangeList;

    protected MyAbstractProcessor(boolean requireChangeList) {
      myRequireChangeList = requireChangeList;
    }

    protected abstract boolean matches(final PerforceChange perforceChange);
    protected abstract void process(final PerforceChange perforceChange, final ChangeList changeList);

    public void processList(final Collection<PerforceChange> perforceChanges) throws VcsException {
      for (Iterator<PerforceChange> iterator = perforceChanges.iterator(); iterator.hasNext();) {
        final PerforceChange change = iterator.next();
        if (change.getFile() == null) continue; //?
        if (matches(change)) {
          final ChangeList changeList = myRequireChangeList ? myChangelistCalculator.convert(change) : null;
          iterator.remove();
          process(change, changeList);
        }
      }
    }
  }

  private class MyLocallyDeletedProcessor extends MyAbstractProcessor {
    private MyLocallyDeletedProcessor() {
      super(false);
    }

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      final File file = perforceChange.getFile();
      final int type = perforceChange.getType();
      return (file != null) && (! file.exists()) && (type != PerforceAbstractChange.DELETE) && (type != PerforceAbstractChange.MOVE_DELETE);
    }

    @Override
    protected void process(PerforceChange perforceChange, final ChangeList changeList) {
      myBuilder.processLocallyDeletedFile(FilePathImpl.createForDeletedFile(perforceChange.getFile(), false));
    }
  }

  // post - applied
  private class MyDeletedProcessor extends MyAbstractProcessor {
    private final Map<String, PerforceChange> myDeleted;

    private MyDeletedProcessor() {
      super(true);
      myDeleted = new HashMap<String, PerforceChange>();
    }

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      final int type = perforceChange.getType();
      return (type == PerforceAbstractChange.DELETE) || (type == PerforceAbstractChange.MOVE_DELETE);
    }

    @Override
    protected void process(PerforceChange perforceChange, final ChangeList changeList) {
      if (myResolvedFiles.getDepotToFiles().containsKey(perforceChange.getDepotPath())) {
        myDeleted.put(perforceChange.getDepotPath(), perforceChange);
        return;
      }
      myBuilder.processChangeInList(myChangeCreator.createDeletedFileChange(perforceChange.getFile(), perforceChange.getRevision(), false),
                                    changeList, PerforceVcs.getKey());
    }

    public void postProcessAll() throws VcsException {
      for (PerforceChange perforceChange : myDeleted.values()) {
        final ChangeList changeList = myChangelistCalculator.convert(perforceChange);
        myBuilder.processChangeInList(myChangeCreator.createDeletedFileChange(perforceChange.getFile(), perforceChange.getRevision(), false),
                                      changeList, PerforceVcs.getKey());
      }
    }

    public PerforceChange removePeer(final String depotPath) {
      return myDeleted.remove(depotPath);
    }
  }

  private class MyAddedProcessor extends MyAbstractProcessor {
    private final MyDeletedProcessor myDeletedProcessor;

    private MyAddedProcessor(MyDeletedProcessor deletedProcessor) {
      super(true);
      myDeletedProcessor = deletedProcessor;
    }

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      final int type = perforceChange.getType();
      // todo????
      return (type == PerforceAbstractChange.ADD) || (type == PerforceAbstractChange.MOVE_ADD);
    }

    @Override
    protected void process(PerforceChange perforceChange, ChangeList changeList) {
      final File file = perforceChange.getFile();
      final ResolvedFile resolvedPeer = myResolvedFiles.getLocalToFiles().get(file);
      if (resolvedPeer != null) {
        final String operation = resolvedPeer.getOperation();
        if ((ResolvedFile.OPERATION_BRANCH.equals(operation)) || ResolvedFile.OPERATION_MOVE.equals(operation)) {
          final PerforceChange deletedChange = myDeletedProcessor.removePeer(resolvedPeer.getDepotPath());
          if (deletedChange != null) {
            // report move
            myBuilder.processChangeInList(myChangeCreator.createRenameChange(myConnection, resolvedPeer, FilePathImpl.create(file)),
                                          changeList, PerforceVcs.getKey());
            return;
          }
        }
      }
      myBuilder.processChangeInList(myChangeCreator.createAddedFileChange(FilePathImpl.create(file), myResolvedWithConflicts.contains(file)),
                                    changeList, PerforceVcs.getKey());
    }
  }

  // todo require additional work -> with integrated command
  private class MyBranchedProcessor extends MyAbstractProcessor {
    private MyBranchedProcessor() {
      super(true);
    }

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      return perforceChange.getType() == PerforceAbstractChange.BRANCH;
    }

    @Override
    protected void process(PerforceChange perforceChange, ChangeList changeList) {
      final File file = perforceChange.getFile();
      myBuilder.processChangeInList(myChangeCreator.createAddedFileChange(FilePathImpl.create(file), myResolvedWithConflicts.contains(file)),
                                    changeList, PerforceVcs.getKey());
    }
  }

  private class MyEditedProcessor extends MyAbstractProcessor {
    private MyEditedProcessor() {
      super(true);
    }

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      return (perforceChange.getRevision() > 0) &&
        ((perforceChange.getType() == PerforceAbstractChange.EDIT) || (perforceChange.getType() == PerforceAbstractChange.INTEGRATE));
    }

    @Override
    protected void process(PerforceChange perforceChange, ChangeList changeList) {
      final File file = perforceChange.getFile();
      myBuilder.processChangeInList(myChangeCreator.createEditedFileChange(FilePathImpl.create(file), perforceChange.getRevision(),
                                                               myResolvedWithConflicts.contains(file)), changeList, PerforceVcs.getKey());
    }
  }

  public void process(final Collection<PerforceChange> p4changes) throws VcsException {
    // todo somehow static???
    final List<MyAbstractProcessor> processors = new LinkedList<MyAbstractProcessor>();
    processors.add(new MyLocallyDeletedProcessor());
    final MyDeletedProcessor deletedProcessor = new MyDeletedProcessor();
    processors.add(deletedProcessor);
    final MyAddedProcessor addedProcessor = new MyAddedProcessor(deletedProcessor);
    processors.add(addedProcessor);
    processors.add(new MyBranchedProcessor());
    processors.add(new MyEditedProcessor());

    for (MyAbstractProcessor processor : processors) {
      processor.processList(p4changes);
    }

    deletedProcessor.postProcessAll();
  }
}
