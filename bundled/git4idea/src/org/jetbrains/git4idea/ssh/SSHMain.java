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
package org.jetbrains.git4idea.ssh;

import com.intellij.openapi.util.io.FileUtil;
import com.trilead.ssh2.*;
import com.trilead.ssh2.crypto.PEMDecoder;
import git4idea.i18n.GitBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * The main class for SSH client. It can only handle the following command line (which is use by GIT):
 * git-ssh xmlRpcPort [-p port] host command. The program is wrapped in the script, so XML RPC port
 * not settable using script interface.
 * <p/>
 * The code here is based on SwingShell example.
 */
@SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class SSHMain {
  /**
   * the semaphore
   */
  private final Semaphore myForwardCompleted = new Semaphore(0);
  /**
   * the host
   */
  private final SSHConfig.Host myHost;
  /**
   * Handler number
   */
  private final int myHandlerNo;
  /**
   * the xml RPC port
   */
  private final GitSSHIdeaClient myXmlRpcClient;
  /**
   * the command to run
   */
  private final String myCommand;
  /**
   * the exit code
   */
  private int myExitCode = 0;
  /**
   * The last error
   */
  private String myLastError = "";
  /**
   * user directory
   */
  @NonNls private static final String userDir = System.getProperty("user.home");
  /**
   * Path to known hosts file
   */
  @NonNls private static final String knownHostPath = userDir + "/.ssh/known_hosts";
  /**
   * Path to DSA key
   */
  @NonNls private static final String idDSAPath = userDir + "/.ssh/id_dsa";
  /**
   * Path to RSA key
   */
  @NonNls private static final String idRSAPath = userDir + "/.ssh/id_rsa";

  /**
   * database of known hosts
   */
  private final KnownHosts database = new KnownHosts();
  /**
   * size of the buffers for stream forwarding
   */
  private static final int BUFFER_SIZE = 16 * 1024;
  /**
   * public key authentication method
   */
  @NonNls public static final String PUBLIC_KEY_METHOD = "publickey";
  /**
   * keyboard interactive method
   */
  @NonNls public static final String KEYBOARD_INTERACTIVE_METHOD = "keyboard-interactive";
  /**
   * password method
   */
  @NonNls public static final String PASSWORD_METHOD = "password";
  /**
   * RSA algorithm
   */
  @NonNls public static final String SSH_RSA_ALGORITHM = "ssh-rsa";
  /**
   * DSS algorithm
   */
  @NonNls public static final String SSH_DSS_ALGORITHM = "ssh-dss";


  /**
   * A constructor
   *
   * @param xmlRpcPort a xml RPC port
   * @param host       a host
   * @param username   a name of user (from URL)
   * @param port       a port
   * @param command    a command
   * @throws IOException if config file could not be loaded
   */
  private SSHMain(final int xmlRpcPort, String host, String username, Integer port, String command) throws IOException {
    SSHConfig config = SSHConfig.load();
    myHost = config.lookup(username, host, port);
    myXmlRpcClient = new GitSSHIdeaClient(xmlRpcPort, myHost.isBatchMode());
    myHandlerNo = Integer.parseInt(System.getenv(GitSSHService.SSH_HANDLER_ENV));
    myCommand = command;
  }

  /**
   * The application entry point
   *
   * @param args program arguments
   */
  public static void main(String[] args) {
    try {
      SSHMain app = parseArguments(args);
      app.start();
      System.exit(app.myExitCode);
    }
    catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Start the application
   *
   * @throws IOException          if there is a problem with connection
   * @throws InterruptedException if thread was interrupted
   */
  private void start() throws IOException, InterruptedException {
    Connection c = new Connection(myHost.getHostName(), myHost.getPort());
    try {
      configureKnownHosts(c);
      c.connect(new HostKeyVerifier());
      authenticate(c);
      final Session s = c.openSession();
      try {
        s.execCommand(myCommand);
        // Note that stdin is not being waited using semaphore.
        // Instead, the SSH process waits for remote process exit
        // if remote process exited, none is interested in stdin
        // anyway.
        forward("stdin", s.getStdin(), System.in, false);
        forward("stdout", System.out, s.getStdout(), true);
        forward("stderr", System.err, s.getStderr(), true);
        myForwardCompleted.acquire(2); // wait only for stderr and stdout
        s.waitForCondition(ChannelCondition.EXIT_STATUS, Long.MAX_VALUE);
        Integer exitStatus = s.getExitStatus();
        if (exitStatus == null) {
          // broken exit status
          exitStatus = 1;
        }
        System.exit(exitStatus.intValue() == 0 ? myExitCode : exitStatus.intValue());
      }
      finally {
        s.close();
      }
    }
    finally {
      c.close();
    }
  }

  /**
   * Authenticate using some supported methods. If authentication fails,
   * the method throws {@link IOException}.
   *
   * @param c the connection to use for authentication
   * @throws IOException in case of IO error or authentication failure
   */
  private void authenticate(final Connection c) throws IOException {
    for (String method : myHost.getPreferredMethods()) {
      if (c.isAuthenticationComplete()) {
        return;
      }
      if (PUBLIC_KEY_METHOD.equals(method)) {
        if (!myHost.supportsPubkeyAuthentication()) {
          continue;
        }
        if (!c.isAuthMethodAvailable(myHost.getUser(), PUBLIC_KEY_METHOD)) {
          continue;
        }
        File key = myHost.getIdentityFile();
        if (key == null) {
          for (String a : myHost.getHostKeyAlgorithms()) {
            if (SSH_RSA_ALGORITHM.equals(a)) {
              if (tryPublicKey(c, idRSAPath)) {
                return;
              }
            }
            else if (SSH_DSS_ALGORITHM.equals(a)) {
              if (tryPublicKey(c, idDSAPath)) {
                return;
              }
            }
          }
        }
        else {
          if (tryPublicKey(c, key.getPath())) {
            return;
          }
        }
      }
      else if (KEYBOARD_INTERACTIVE_METHOD.equals(method)) {
        if (!c.isAuthMethodAvailable(myHost.getUser(), KEYBOARD_INTERACTIVE_METHOD)) {
          continue;
        }
        InteractiveSupport interactiveSupport = new InteractiveSupport();
        for (int i = myHost.getNumberOfPasswordPrompts(); i > 0; i--) {
          if (c.isAuthenticationComplete()) {
            return;
          }
          if (c.authenticateWithKeyboardInteractive(myHost.getUser(), interactiveSupport)) {
            myLastError = "";
            return;
          }
          else {
            myLastError = GitBundle.getString("sshmain.keyboard.interactive.failed");
          }
          if (interactiveSupport.myPromptCount == 0 || interactiveSupport.myCancelled) {
            // the interactive callback has never been asked or it was cancelled, exit the loop
            myLastError = "";
            break;
          }
        }
      }
      else if (PASSWORD_METHOD.equals(method)) {
        if (!myHost.supportsPasswordAuthentication()) {
          continue;
        }
        if (!c.isAuthMethodAvailable(myHost.getUser(), PASSWORD_METHOD)) {
          continue;
        }
        for (int i = myHost.getNumberOfPasswordPrompts(); i > 0; i--) {
          String password = myXmlRpcClient.askPassword(myHandlerNo, getUserHostString(), myLastError);
          if (password == null) {
            break;
          }
          else {
            if (c.authenticateWithPassword(myHost.getUser(), password)) {
              myLastError = "";
              return;
            }
            else {
              myLastError = GitBundle.getString("sshmain.password.failed");
            }
          }
        }
      }
    }
    throw new IOException("Authentication failed");
  }

  /**
   * @return user and host string
   */
  private String getUserHostString() {
    int port = myHost.getPort();
    return myHost.getUser() + "@" + myHost.getHostName() + (port == 22 ? "" : ":" + port);
  }

  /**
   * Try public key
   *
   * @param c       a ssh connection
   * @param keyPath a path to key
   * @return true if authentication is successful
   */
  private boolean tryPublicKey(final Connection c, final String keyPath) {
    try {
      final File file = new File(keyPath);
      if (file.exists()) {
        // if encrypted ask user for passphrase
        String passphrase = null;
        char[] text = FileUtil.loadFileText(file);
        if (isEncryptedKey(text)) {
          // need to ask passphrase from user
          int i;
          for (i = myHost.getNumberOfPasswordPrompts(); i > 0; i--) {
            passphrase = myXmlRpcClient.askPassphrase(myHandlerNo, getUserHostString(), keyPath, myLastError);
            if (passphrase == null) {
              // if no passphrase was entered, just return false and try something other
              return false;
            }
            else {
              try {
                PEMDecoder.decode(text, passphrase);
                myLastError = "";
              }
              catch (IOException e) {
                // decoding failed
                myLastError = GitBundle.message("sshmain.invalidpassphrase", keyPath);
                continue;
              }
              break;
            }
          }
          if (i == 0) {
            myLastError = GitBundle.message("sshmain.too.mush.passphrase.guesses", keyPath, myHost.getNumberOfPasswordPrompts());
            return false;
          }
        }
        // try authentication
        if (c.authenticateWithPublicKey(myHost.getUser(), text, passphrase)) {
          myLastError = "";
          return true;
        }
        else {
          if (passphrase != null) {
            // mark as failed authentication only if passphrase were asked
            myLastError = GitBundle.message("sshmain.pk.authenitication.failed", keyPath);
          }
          else {
            myLastError = "";
          }
        }
      }
      return false;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Check if the key is encrypted. The key is considered encrypted
   *
   * @param text the text of the key
   * @return true if the key is encrypted
   * @throws IOException if there is a problem with reading key
   */
  private static boolean isEncryptedKey(char[] text) throws IOException {
    BufferedReader in = new BufferedReader(new CharArrayReader(text));
    try {
      String line;
      while ((line = in.readLine()) != null) {
        //noinspection HardCodedStringLiteral
        if (line.startsWith("Proc-Type: ") && line.indexOf("ENCRYPTED") != -1) {
          return true;
        }
        if (line.length() == 0) {
          // empty line means end of the mime headers
          break;
        }
      }
      return false;
    }
    finally {
      in.close();
    }
  }

  /**
   * Forward stream in separate thread.
   *
   * @param name             the name of the stream
   * @param out              the output stream
   * @param in               the input stream
   * @param releaseSemaphore if true the semaphore will be released
   */
  private void forward(@NonNls final String name, final OutputStream out, final InputStream in, final boolean releaseSemaphore) {
    final Runnable action = new Runnable() {
      public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        int rc;
        try {
          try {
            try {
              while ((rc = in.read(buffer)) != -1) {
                out.write(buffer, 0, rc);
              }
            }
            finally {
              out.close();
            }
          }
          finally {
            in.close();
          }
        }
        catch (IOException e) {
          System.err.println(GitBundle.message("sshmain.forwarding.failed", name, e.getMessage()));
          e.printStackTrace();
          myExitCode = 1;
          if (releaseSemaphore) {
            // in the case of error, release semaphore, so that application could exit
            myForwardCompleted.release(1);
          }
        }
        finally {
          if (releaseSemaphore) {
            myForwardCompleted.release(1);
          }
        }
      }
    };
    @SuppressWarnings({"HardCodedStringLiteral"}) final Thread t = new Thread(action, "Forwarding " + name);
    t.setDaemon(true);
    t.start();
  }


  /**
   * Configure known host database for connection
   *
   * @param c a connection
   * @throws IOException if there is a IO problem
   */
  private void configureKnownHosts(Connection c) throws IOException {
    File knownHostFile = new File(knownHostPath);
    if (knownHostFile.exists()) {
      database.addHostkeys(knownHostFile);
    }
    final List<String> algorithms = myHost.getHostKeyAlgorithms();
    c.setServerHostKeyAlgorithms(algorithms.toArray(new String[algorithms.size()]));
  }

  /**
   * Parse command line arguments and create application instance.
   *
   * @param args command line arguments
   * @return application instance
   * @throws IOException if loading configuration file failed
   */
  private static SSHMain parseArguments(String[] args) throws IOException {
    if (args.length != 3 && args.length != 5) {
      System.err.println(GitBundle.message("sshmain.invalid.amount.of.arguments", Arrays.asList(args)));
      System.exit(1);
    }
    int i = 0;
    int xmlRpcPort = Integer.parseInt(args[i++]);
    Integer port = null;
    //noinspection HardCodedStringLiteral
    if ("-p".equals(args[i])) {
      i++;
      port = Integer.parseInt(args[i++]);
    }
    String host = args[i++];
    String user;
    int atIndex = host.indexOf('@');
    if (atIndex == -1) {
      user = null;
    }
    else {
      user = host.substring(0, atIndex);
      host = host.substring(atIndex + 1);
    }
    String command = args[i];
    return new SSHMain(xmlRpcPort, host, user, port, command);
  }


  /**
   * Interactive callback support. The callback invokes Idea XML RPC server.
   */
  class InteractiveSupport implements InteractiveCallback {
    /**
     * Prompt count
     */
    int myPromptCount = 0;
    /**
     * true if keyboard interactive method was cancelled.
     */
    boolean myCancelled;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"UseOfObsoleteCollectionType"})
    @Nullable
    public String[] replyToChallenge(final String name,
                                     final String instruction,
                                     final int numPrompts,
                                     final String[] prompt,
                                     final boolean[] echo) throws Exception {
      if (numPrompts == 0) {
        return new String[0];
      }
      myPromptCount++;
      Vector<String> vPrompts = new Vector<String>(prompt.length);
      Collections.addAll(vPrompts, prompt);
      Vector<Boolean> vEcho = new Vector<Boolean>(prompt.length);
      for (boolean e : echo) {
        vEcho.add(e);
      }
      final Vector<String> result =
        myXmlRpcClient.replyToChallenge(myHandlerNo, getUserHostString(), name, instruction, numPrompts, vPrompts, vEcho, myLastError);
      if (result == null) {
        myCancelled = true;
        String[] rc = new String[numPrompts];
        Arrays.fill(rc, "");
        return rc;
      }
      else {
        return result.toArray(new String[result.size()]);
      }
    }
  }

  /**
   * Server host key verifier that invokes Idea XML RPC server.
   */
  private class HostKeyVerifier implements ServerHostKeyVerifier {
    /**
     * {@inheritDoc}
     */
    public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
      try {
        final int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);
        final boolean isNew;
        switch (result) {
          case KnownHosts.HOSTKEY_IS_OK:
            return true;
          case KnownHosts.HOSTKEY_IS_NEW:
            isNew = true;
            break;
          case KnownHosts.HOSTKEY_HAS_CHANGED:
            isNew = false;
            break;
          default:
            throw new IllegalStateException("Unknown verification result: " + result);
        }
        String fingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
        boolean keyCheck = myXmlRpcClient.verifyServerHostKey(myHandlerNo, hostname, port, serverHostKeyAlgorithm, fingerprint, isNew);
        if (keyCheck) {
          String hashedHostname = KnownHosts.createHashedHostname(hostname);
          // Add the host key to the in-memory database
          database.addHostkey(new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
          // Also try to add the key to a known_host file
          try {
            KnownHosts.addHostkeyToFile(new File(knownHostPath), new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
          }
          catch (IOException ignore) {
            // TODO log text
          }
          return true;
        }
        else {
          System.err.println(GitBundle.message("sshmain.invald.host.key", serverHostKeyAlgorithm, fingerprint));
          return false;
        }
      }
      catch (Throwable t) {
        System.err.println(GitBundle.message("sshmain.failed.to.verify.key", t.getMessage()));
        t.printStackTrace();
        return false;
      }
    }
  }
}
