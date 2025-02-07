/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
package org.jetbrains.git4idea.ssh;

import com.intellij.ide.XmlRpcServer;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.trilead.ssh2.KnownHosts;
import git4idea.commands.ScriptGenerator;
import git4idea.i18n.GitBundle;
import gnu.trove.THashMap;
import org.apache.commons.codec.DecoderException;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * The provider of SSH scripts for the Git
 */
public class GitSSHService implements ApplicationComponent {
  /**
   * The string used to indicate missing value
   */
  private static final String XML_RPC_NULL_STRING = "\u0000";
  /**
   * the logger
   */
  private static final Logger log = Logger.getInstance(GitSSHService.class.getName());

  /**
   * random number generator to use
   */
  private static final Random RANDOM = new Random();
  /**
   * Name of the handler
   */
  @NonNls private static final String HANDLER_NAME = "Git4ideaSSHHandler";
  /**
   * The prefix of the ssh script name
   */
  @NonNls private static final String GIT_SSH_PREFIX = "git-ssh-";
  /**
   * If true, the component has been initialized
   */
  private boolean myInitialized = false;
  /**
   * Path to the generated script
   */
  private File myScriptPath;
  /**
   * XML RPC server
   */
  private final XmlRpcServer myXmlRpcServer;
  /**
   * Registered handlers
   */
  private final THashMap<Integer, Handler> handlers = new THashMap<Integer, Handler>();

  /**
   * Name of environment variable for SSH handler
   */
  @NonNls public static final String SSH_HANDLER_ENV = "GIT4IDEA_SSH_HANDLER";
  /**
   * Name of environment variable for SSH executable
   */
  @NonNls public static final String GIT_SSH_ENV = "GIT_SSH";


  /**
   * A constructor from parameter
   *
   * @param xmlRpcServer the injected XmlRpc server reference
   */
  public GitSSHService(final @NotNull XmlRpcServer xmlRpcServer) {
    myXmlRpcServer = xmlRpcServer;
  }

  /**
   * @return an instance of the server
   */
  @NotNull
  public static GitSSHService getInstance() {
    final GitSSHService service = ServiceManager.getService(GitSSHService.class);
    if (service == null) {
      throw new IllegalStateException("The service " + GitSSHService.class.getName() + " cannot be located");
    }
    return service;
  }

  /**
   * Get file to the script service
   *
   * @return path to the script
   * @throws IOException if script cannot be generated
   */
  @NotNull
  public synchronized File getScriptPath() throws IOException {
    if (myScriptPath == null) {
      ScriptGenerator generator = new ScriptGenerator(GIT_SSH_PREFIX, SSHMain.class);
      generator.addInternal(Integer.toString(myXmlRpcServer.getPortNumber()));
      generator.addClasses(XmlRpcClientLite.class, DecoderException.class);
      generator.addClasses(KnownHosts.class, FileUtil.class);
      generator.addResource(GitBundle.class, "/git4idea/i18n/GitBundle.properties");
      myScriptPath = generator.generate();
    }
    return myScriptPath;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public String getComponentName() {
    return GitSSHService.class.getSimpleName();
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent() {
    if (!myInitialized) {
      myXmlRpcServer.addHandler(HANDLER_NAME, new InternalRequestHandler());
      myInitialized = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void disposeComponent() {
    myXmlRpcServer.removeHandler(HANDLER_NAME);
    if (myScriptPath != null) {
      if (!myScriptPath.delete()) {
        log.warn("The temporary file " + myScriptPath + " generated by git4idea plugin failed to be removed during disposing.");
      }
      myScriptPath = null;
    }
  }

  /**
   * Register handler. Note that handlers must be unregistered using {@link #unregisterHandler(int)}.
   *
   * @param handler a handler to register
   * @return an identifier to pass to the environment variable
   */
  public synchronized int registerHandler(@NotNull Handler handler) {
    initComponent();
    while (true) {
      int candidate = RANDOM.nextInt();
      if (candidate == Integer.MIN_VALUE) {
        continue;
      }
      candidate = Math.abs(candidate);
      if (handlers.containsKey(candidate)) {
        continue;
      }
      handlers.put(candidate, handler);
      return candidate;
    }
  }

  /**
   * Get handler for the key
   *
   * @param key the key to use
   * @return the registered handler
   */
  @NotNull
  private synchronized Handler getHandler(int key) {
    Handler rc = handlers.get(key);
    if (rc == null) {
      throw new IllegalStateException("No handler for the key " + key);
    }
    return rc;
  }

  /**
   * Unregister handler by the key
   *
   * @param key the key to unregister
   */
  public synchronized void unregisterHandler(int key) {
    if (handlers.remove(key) == null) {
      throw new IllegalArgumentException("The handler " + key + " is not registered");
    }
  }


  /**
   * Handler interface to use by the client code
   */
  public interface Handler {
    /**
     * Verify key
     *
     * @param hostname               a host name
     * @param port                   a port number
     * @param serverHostKeyAlgorithm an algorithm
     * @param serverHostKey          a key
     * @param isNew                  a isNew key
     * @return true if the key is valid
     */
    boolean verifyServerHostKey(final String hostname,
                                final int port,
                                final String serverHostKeyAlgorithm,
                                final String serverHostKey,
                                final boolean isNew);

    /**
     * Ask passphrase
     *
     * @param username  a user name
     * @param keyPath   a key path
     * @param lastError the last error for the handler
     * @return a passphrase or null if dialog was cancelled.
     */
    String askPassphrase(final String username, final String keyPath, final String lastError);

    /**
     * Reply to challenge in keyboard-interactive scenario
     *
     * @param username    a user name
     * @param name        a name of challenge
     * @param instruction a instructions
     * @param numPrompts  number of prompts
     * @param prompt      prompts
     * @param echo        true if the reply for corresponding prompt should be echoed
     * @param lastError   the last error
     * @return replies to the challenges
     */
    @SuppressWarnings({"UseOfObsoleteCollectionType"})
    Vector<String> replyToChallenge(final String username,
                                    final String name,
                                    final String instruction,
                                    final int numPrompts,
                                    final Vector<String> prompt,
                                    final Vector<Boolean> echo,
                                    final String lastError);

    /**
     * Ask password
     *
     * @param username  a user name
     * @param lastError the previous error
     * @return a password or null if dialog was cancelled.
     */
    String askPassword(final String username, final String lastError);

  }

  /**
   * Internal handler implementation class, do not use it.
   */
  public class InternalRequestHandler implements GitSSHHandler {
    /**
     * {@inheritDoc}
     */
    public boolean verifyServerHostKey(final int handler,
                                       final String hostname,
                                       final int port,
                                       final String serverHostKeyAlgorithm,
                                       final String serverHostKey,
                                       final boolean isNew) {
      return getHandler(handler).verifyServerHostKey(hostname, port, serverHostKeyAlgorithm, serverHostKey, isNew);
    }

    /**
     * {@inheritDoc}
     */
    public String askPassphrase(final int handler, final String username, final String keyPath, final String lastError) {
      return adjustNull(getHandler(handler).askPassphrase(username, keyPath, lastError));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"UseOfObsoleteCollectionType"})
    public Vector<String> replyToChallenge(final int handlerNo,
                                           final String username,
                                           final String name,
                                           final String instruction,
                                           final int numPrompts,
                                           final Vector<String> prompt,
                                           final Vector<Boolean> echo,
                                           final String lastError) {
      return adjustNull(getHandler(handlerNo).replyToChallenge(username, name, instruction, numPrompts, prompt, echo, lastError));
    }

    /**
     * {@inheritDoc}
     */
    public String askPassword(final int handlerNo, final String username, final String lastError) {
      return adjustNull(getHandler(handlerNo).askPassword(username, lastError));
    }

    /**
     * Adjust null value (by converting to {@link GitSSHService#XML_RPC_NULL_STRING})
     *
     * @param s a value to adjust
     * @return a string if non-null or {@link GitSSHService#XML_RPC_NULL_STRING} if s == null
     */
    private String adjustNull(final String s) {
      return s == null ? XML_RPC_NULL_STRING : s;
    }

    /**
     * Adjust null value (returns empty array)
     *
     * @param s if null return empty array
     * @return s if not null, empty array otherwise
     */
    @SuppressWarnings({"UseOfObsoleteCollectionType"})
    private <T> Vector<T> adjustNull(final Vector<T> s) {
      return s == null ? new Vector<T>() : s;
    }
  }
}
