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
package org.jetbrains.idea.svn.update;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnConfiguration;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.SvnWCRootCrawler;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractUpdateIntegrateCrawler implements SvnWCRootCrawler {
  protected final SvnVcs myVcs;
  protected final ISVNEventHandler myHandler;
  protected final Collection<VcsException> myExceptions;
  protected final UpdatedFiles myPostUpdateFiles;
  protected final boolean myIsTotalUpdate;

  protected AbstractUpdateIntegrateCrawler(
    final boolean isTotalUpdate,
    final UpdatedFiles postUpdateFiles,
    final Collection<VcsException> exceptions,
    final ISVNEventHandler handler,
    final SvnVcs vcs) {
    myIsTotalUpdate = isTotalUpdate;
    myPostUpdateFiles = postUpdateFiles;
    myExceptions = exceptions;
    myHandler = handler;
    myVcs = vcs;
  }

  public Collection<File> handleWorkingCopyRoot(File root, ProgressIndicator progress) {
    final Collection<File> result = new HashSet<File>();

    if (progress != null) {
      showProgressMessage(progress, root);
    }
    try {
      SVNUpdateClient client = myVcs.createUpdateClient();
      client.setEventHandler(myHandler);

      long rev = doUpdate(root, client);

      if (rev < 0 && !isMerge()) {
        throw new SVNException(SVNErrorMessage.create(SVNErrorCode.UNKNOWN, SvnBundle.message("exception.text.root.was.not.properly.updated", root)));
      }
    }
    catch (SVNException e) {
      myExceptions.add(new VcsException(e));
    }
    if (!SvnConfiguration.getInstanceChecked(myVcs.getProject()).UPDATE_RUN_STATUS) {
      return result;
    }

    final SvnStatusWorker statusWorker = new SvnStatusWorker(myVcs, result, root, myPostUpdateFiles, myIsTotalUpdate, myExceptions);
    statusWorker.doStatus();
    
    return result;
  }

  protected abstract void showProgressMessage(ProgressIndicator progress, File root);

  protected abstract long doUpdate(
    File root,
    SVNUpdateClient client) throws SVNException;

  protected abstract boolean isMerge();
}
