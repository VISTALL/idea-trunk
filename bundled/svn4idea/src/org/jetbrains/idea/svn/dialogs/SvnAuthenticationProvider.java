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
package org.jetbrains.idea.svn.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.GuiUtils;
import com.intellij.util.SystemProperties;
import org.jetbrains.idea.svn.SvnBundle;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 25.06.2005
 * Time: 17:00:17
 * To change this template use File | Settings | File Templates.
 */
public class SvnAuthenticationProvider implements ISVNAuthenticationProvider {
  private final Project myProject;

  public SvnAuthenticationProvider(Project project) {
    myProject = project;
  }

  public SVNAuthentication requestClientAuthentication(final String kind,
                                                       final SVNURL url,
                                                       final String realm,
                                                       SVNErrorMessage errorMessage,
                                                       final SVNAuthentication previousAuth,
                                                       final boolean authMayBeStored) {
    final SVNAuthentication[] result = new SVNAuthentication[1];
    Runnable command = null;

    final String userName =
      previousAuth != null && previousAuth.getUserName() != null ? previousAuth.getUserName() : SystemProperties.getUserName();
    if (ISVNAuthenticationManager.PASSWORD.equals(kind)) {// || ISVNAuthenticationManager.USERNAME.equals(kind)) {
      command = new Runnable() {
        public void run() {
          SimpleCredentialsDialog dialog = new SimpleCredentialsDialog(myProject);
          dialog.setup(realm, userName, authMayBeStored);
          if (previousAuth == null) {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required"));
          }
          else {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required.was.failed"));
          }
          dialog.show();
          if (dialog.isOK()) {
            result[0] = new SVNPasswordAuthentication(dialog.getUserName(), dialog.getPassword(), dialog.isSaveAllowed());
          }
        }
      };
    }
    else if (ISVNAuthenticationManager.USERNAME.equals(kind)) {
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        return new SVNUserNameAuthentication(userName, false);
      }
      command = new Runnable() {
        public void run() {
          UserNameCredentialsDialog dialog = new UserNameCredentialsDialog(myProject);
          dialog.setup(realm, userName, authMayBeStored);
          if (previousAuth == null) {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required"));
          }
          else {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required.was.failed"));
          }
          dialog.show();
          if (dialog.isOK()) {
            result[0] = new SVNUserNameAuthentication(dialog.getUserName(), dialog.isSaveAllowed());
          }
        }
      };
    }
    else if (ISVNAuthenticationManager.SSH.equals(kind)) {
      command = new Runnable() {
        public void run() {
          SSHCredentialsDialog dialog = new SSHCredentialsDialog(myProject);
          dialog.setup(realm, userName, authMayBeStored, url.getPort());
          if (previousAuth == null) {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required"));
          }
          else {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required.was.failed"));
          }
          dialog.show();
          if (dialog.isOK()) {
            int port = dialog.getPortNumber();
            if (dialog.getKeyFile() != null && dialog.getKeyFile().trim().length() > 0) {
              String passphrase = dialog.getPassphrase();
              if (passphrase != null && passphrase.length() == 0) {
                passphrase = null;
              }
              result[0] =
                new SVNSSHAuthentication(dialog.getUserName(), new File(dialog.getKeyFile()), passphrase, port, dialog.isSaveAllowed());
            }
            else {
              result[0] = new SVNSSHAuthentication(dialog.getUserName(), dialog.getPassword(), port, dialog.isSaveAllowed());
            }
          }
        }
      };
    } else if (ISVNAuthenticationManager.SSL.equals(kind)) {
      command = new Runnable() {
        public void run() {
          final SSLCredentialsDialog dialog = new SSLCredentialsDialog(myProject, realm, authMayBeStored);
          if (previousAuth == null) {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required"));
          }
          else {
            dialog.setTitle(SvnBundle.message("dialog.title.authentication.required.was.failed"));
          }
          dialog.show();
          if (dialog.isOK()) {
            result[0] = new SVNSSLAuthentication(new File(dialog.getCertificatePath()), String.valueOf(dialog.getCertificatePassword()),
                                                 dialog.getSaveAuth());
          }
        }
      };
    }

    if (command != null) {
      try {
        GuiUtils.runOrInvokeAndWait(command);
      }
      catch (InterruptedException e) {
        //
      }
      catch (InvocationTargetException e) {
        //
      }
    }
    return result[0];
  }

  public int acceptServerAuthentication(SVNURL url, String realm, final Object certificate, final boolean resultMayBeStored) {
    if (!(certificate instanceof X509Certificate)) {
      return ACCEPTED;
    }
    final int[] result = new int[1];
    Runnable command = new Runnable() {
      public void run() {
        ServerSSLDialog dialog = new ServerSSLDialog(myProject, (X509Certificate)certificate, resultMayBeStored);
        dialog.show();
        result[0] = dialog.getResult();

      }
    };
    try {
      GuiUtils.runOrInvokeAndWait(command);
    }
    catch (InterruptedException e) {
      //
    }
    catch (InvocationTargetException e) {
      //
    }
    return result[0];
  }
}
