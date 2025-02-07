/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
package git4idea.rebase;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import java.io.Closeable;

/**
 * The handler for rebase editor request. The handler shows {@link git4idea.rebase.GitRebaseEditor}
 * dialog with the specified file. If user accepts the changes, it saves file and returns 0,
 * otherwise it just returns error code.
 */
public class GitRebaseEditorHandler implements Closeable {
  /**
   * The logger
   */
  private final static Logger LOG = Logger.getInstance(GitRebaseEditorHandler.class.getName());
  /**
   * The service object that has created this handler
   */
  private final GitRebaseEditorService myService;
  /**
   * The context project
   */
  private final Project myProject;
  /**
   * The git repository root
   */
  private final VirtualFile myRoot;
  /**
   * The handler number
   */
  private final int myHandlerNo;
  /**
   * If true, the handler has been closed
   */
  private boolean myIsClosed;
  /**
   * Set to true after rebase edior was shown
   */
  private boolean myRebaseEditorShown = false;

  /**
   * The constructor from fields that is expected to be
   * accessed only from {@link git4idea.rebase.GitRebaseEditorService}.
   *
   * @param service   the service object that has created this handler
   * @param project   the context project
   * @param root      the git repository root
   * @param handlerNo the handler no for this editor
   */
  GitRebaseEditorHandler(final GitRebaseEditorService service, final Project project, final VirtualFile root, final int handlerNo) {
    myService = service;
    myProject = project;
    myRoot = root;
    myHandlerNo = handlerNo;
  }

  /**
   * Edit commits request
   *
   * @param path the path to eding
   * @return the exit code to be returned from editor
   */
  public int editCommits(final String path) {
    ensureOpen();
    final Ref<Boolean> isSuccess = new Ref<Boolean>();
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      public void run() {
        try {
          if (myRebaseEditorShown) {
            GitRebaseUnstructuredEditor editor = new GitRebaseUnstructuredEditor(myProject, myRoot, path);
            editor.show();
            if (editor.isOK()) {
              editor.save();
              isSuccess.set(true);
              return;
            }
            else {
              isSuccess.set(false);
            }
          }
          else {
            setRebaseEditorShown();
            GitRebaseEditor editor = new GitRebaseEditor(myProject, myRoot, path);
            editor.show();
            if (editor.isOK()) {
              editor.save();
              isSuccess.set(true);
              return;
            }
            else {
              editor.cancel();
              isSuccess.set(true);
            }
          }
        }
        catch (Exception e) {
          LOG.error("Failed to edit the git rebase file: " + path, e);
        }
        isSuccess.set(false);
      }
    });
    return (isSuccess.isNull() || !isSuccess.get().booleanValue()) ? GitRebaseEditorMain.ERROR_EXIT_CODE : 0;
  }

  /**
   * This method is invoked to indicate that this editor will be invoked in the rebase continuation action.
   */
  public void setRebaseEditorShown() {
    myRebaseEditorShown = true;
  }

  /**
   * Check that handler has not yet been closed
   */
  private void ensureOpen() {
    if (myIsClosed) {
      throw new IllegalStateException("The handler was already closed");
    }
  }

  /**
   * Stop using the handler
   */
  public void close() {
    ensureOpen();
    myIsClosed = true;
    myService.unregisterHandler(myHandlerNo);
  }

  /**
   * @return the handler number
   */
  public int getHandlerNo() {
    return myHandlerNo;
  }
}
