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

package org.jetbrains.tfsIntegration.ui;

import com.intellij.ui.DocumentAdapter;
import com.intellij.util.EventDispatcher;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.core.configuration.Credentials;
import org.jetbrains.tfsIntegration.core.tfs.TfsUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.URI;
import java.util.EventListener;

public class LoginForm {

  public interface Listener extends EventListener {
    void stateChanged();
  }

  private JTextField myUriField;
  private JTextField myUsernameField;
  private JTextField myDomainField;
  private JPasswordField myPasswordField;
  private JCheckBox myStorePasswordCheckbox;
  private JPanel myContentPane;
  private JLabel myMessageLabel;

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  public LoginForm(URI initialUri, Credentials initialCredentials, boolean allowUrlChange) {
    myUriField.setText(initialUri != null ? initialUri.toString() : null);
    myUriField.setEditable(allowUrlChange);
    myUsernameField.setText(initialCredentials != null ? initialCredentials.getUserName() : null);
    myDomainField.setText(initialCredentials != null ? initialCredentials.getDomain() : null);
    myPasswordField.setText(initialCredentials != null ? initialCredentials.getPassword() : null);

    final DocumentListener changeListener = new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent e) {
        myEventDispatcher.getMulticaster().stateChanged();
      }
    };

    myUriField.getDocument().addDocumentListener(changeListener);
    myUsernameField.getDocument().addDocumentListener(changeListener);
    myDomainField.getDocument().addDocumentListener(changeListener);
    myPasswordField.getDocument().addDocumentListener(changeListener);
  }

  public JComponent getPreferredFocusedComponent() {
    if (myUriField.isEditable()) {
      return myUriField;
    }
    else if (myUsernameField.getText() == null || myUsernameField.getText().length() == 0) {
      return myUsernameField;
    }
    else {
      return myPasswordField;
    }
  }

  public JPanel getContentPane() {
    return myContentPane;
  }

  @Nullable
  public URI getUri() {
    return TfsUtil.getHostUri(myUriField.getText(), true);
  }

  public String getUsername() {
    return myUsernameField.getText();
  }

  public String getDomain() {
    return myDomainField.getText();
  }

  public String getPassword() {
    return String.valueOf(myPasswordField.getPassword());
  }

  public Credentials getCredentials() {
    return new Credentials(getUsername(), getDomain(), getPassword(), myStorePasswordCheckbox.isSelected());
  }

  public void addListener(Listener listener) {
    myEventDispatcher.addListener(listener);
  }

  public void removeListener(Listener listener) {
    myEventDispatcher.removeListener(listener);
  }

  public void setErrorMessage(final @Nullable String errorMessage) {
    myMessageLabel.setText(errorMessage != null ? errorMessage : " "); // keep the space for label
  }
}
