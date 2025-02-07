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

import com.intellij.ide.XmlRpcServer;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.ScriptGenerator;
import gnu.trove.THashMap;
import org.apache.commons.codec.DecoderException;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

/**
 * The service that generates editor script for
 */
public class GitRebaseEditorService implements ApplicationComponent {
  /**
   * the logger
   */
  private static final Logger LOG = Logger.getInstance(GitRebaseEditorService.class.getName());

  /**
   * The editor command that is set to env variable
   */
  private String myEditorCommand;
  /**
   * The lock object
   */
  private final Object myScriptLock = new Object();
  /**
   * The handlers to use
   */
  private final Map<Integer, GitRebaseEditorHandler> myHandlers = new THashMap<Integer, GitRebaseEditorHandler>();
  /**
   * The lock for the handlers
   */
  private final Object myHandlersLock = new Object();
  /**
   * XML rcp server
   */
  private final XmlRpcServer myXmlRpcServer;
  /**
   * Random number generator
   */
  private final static Random oursRandom = new Random();
  /**
   * If true, the component has been intialized
   */
  private boolean myInitialized = false;
  /**
   * The prefix for rebase editors
   */
  @NonNls private static final String GIT_REBASE_EDITOR_PREFIX = "git-rebase-editor-";

  /**
   * The constructor
   *
   * @param xmlRpcServer the XML RCP server instance
   */
  public GitRebaseEditorService(@NotNull final XmlRpcServer xmlRpcServer) {
    myXmlRpcServer = xmlRpcServer;
  }

  /**
   * @return an instance of the server
   */
  @NotNull
  public static GitRebaseEditorService getInstance() {
    final GitRebaseEditorService service = ServiceManager.getService(GitRebaseEditorService.class);
    if (service == null) {
      throw new IllegalStateException("The service " + GitRebaseEditorService.class.getName() + " cannot be located");
    }
    return service;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public String getComponentName() {
    return getClass().getSimpleName();
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent() {
    if (!myInitialized) {
      myXmlRpcServer.addHandler(GitRebaseEditorMain.HANDLER_NAME, new InternalHandler());
      myInitialized = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void disposeComponent() {
    myXmlRpcServer.removeHandler(GitRebaseEditorMain.HANDLER_NAME);
  }

  /**
   * Get editor command
   *
   * @return the editor command
   */
  @NotNull
  public synchronized String getEditorCommand() {
    synchronized (myScriptLock) {
      if (myEditorCommand == null) {
        ScriptGenerator generator = new ScriptGenerator(GIT_REBASE_EDITOR_PREFIX, GitRebaseEditorMain.class);
        generator.addInternal(Integer.toString(myXmlRpcServer.getPortNumber()));
        generator.addClasses(XmlRpcClientLite.class, DecoderException.class);
        myEditorCommand = generator.commandLine();
      }
      return myEditorCommand;
    }
  }

  /**
   * @return the handler instance
   */
  public GitRebaseEditorHandler getHandler(Project project, VirtualFile root) {
    initComponent();
    GitRebaseEditorHandler rc = null;
    synchronized (myHandlersLock) {
      for (int i = Integer.MAX_VALUE; i > 0; i--) {
        int code = Math.abs(oursRandom.nextInt());
        // note that code might still be negative at this point if it is Integer.MIN_VALUE.
        if (code > 0 && !myHandlers.containsKey(code)) {
          rc = new GitRebaseEditorHandler(this, project, root, code);
          break;
        }
      }
      if (rc == null) {
        throw new IllegalStateException("There is a problem with random number allocation");
      }
      myHandlers.put(rc.getHandlerNo(), rc);
    }
    return rc;
  }


  /**
   * Unregister handler
   *
   * @param handlerNo the handler number.
   */
  void unregisterHandler(final int handlerNo) {
    synchronized (myHandlersLock) {
      if (myHandlers.remove(handlerNo) == null) {
        throw new IllegalStateException("The handler " + handlerNo + " has been already remoted");
      }
    }
  }

  /**
   * Unregister handler
   *
   * @param handlerNo the handler number.
   */
  @NotNull
  GitRebaseEditorHandler getHandler(final int handlerNo) {
    synchronized (myHandlersLock) {
      GitRebaseEditorHandler h = myHandlers.get(handlerNo);
      if (h == null) {
        throw new IllegalStateException("The handler " + handlerNo + " has been already remoted");
      }
      return h;
    }
  }


  /**
   * The internal xml rcp handler
   */
  public class InternalHandler {
    /**
     * Edit commits for the rebase operation
     *
     * @param handlerNo the handler no
     * @param path      the path to edit
     * @return exit code
     */
    public int editCommits(int handlerNo, String path) {
      return getHandler(handlerNo).editCommits(path);
    }
  }
}
