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

package com.advancedtools.webservices.rest.client;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.RestUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.TypeSafeDataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * @by Konstantin Bulenkov
 */
public class RESTClient implements RestClientModel, Disposable {
  private JComboBox myHttpMethod;
  private JComboBox myURL;
  private JButton myGoButton;
  private JTabbedPane myTabs;
  private JTextPane myResponse;
  private JPanel myMainPanel;
  private JTextPane myHeader;
  private JTextField myURLBase;
  private JTable myHeaderParameters;
  private JTable myParameters;
  private JCheckBox myDoNotSendParams;
  private JButton myUpdateButton;
  private JButton myViewAsHtml;
  private JScrollPane myResponseScrollPane;
  private JButton myViewAsJson;
  private JButton myViewAsXml;
  private JButton myWebBrowserButton;
  private JToolBar myToolbar;
  private JLabel myStatus;
  private final NameValueTableModel myHeaderData = new NameValueTableModel();
  private final NameValueTableModel myRequestData = new NameValueTableModel();
  private final RestClientController myController;
  private final JPanel myWrappedMainPanel = new RestHelpWrapper();


  public static final @NonNls String ACCEPT = "Accept";
  public static final @NonNls String CONTENT_TYPE = "Content-Type";
  public static final @NonNls String CONTENT_LENGTH = "Content-Length";
  public static final @NonNls String CACHE_CONTROL = "Cache-Control";
  public static final @NonNls String[] IMMUTABLE_HEADER_PARAMS = {ACCEPT, CONTENT_TYPE, CONTENT_LENGTH, CACHE_CONTROL};

  public RESTClient(final Project project) {
    myController = new RestClientControllerImpl(this, project);
    createUIComponents(project);
    myWrappedMainPanel.add(myMainPanel);
  }

  public String getHeaderName(final int ind) {
    return myHeaderData.getName(ind);
  }

  public String getHeaderValue(final int ind) {
    return myHeaderData.getValue(ind);
  }

  public int getHeaderSize() {
    return myHeaderData.getElementsCount();
  }

  public void setHeader(final String name, final String value) {
    myHeaderData.addProperty(name, value);
  }

  public void setResponseHeader(final String header) {
    myHeader.setText( header == null ? "" : header);
  }

  public String getParameterName(final int ind) {
    return myRequestData.getName(ind);
  }

  public String getParameterValue(final int ind) {
    return myRequestData.getValue(ind);
  }

  public int getParametersSize() {
    return myRequestData.getElementsCount();
  }

  public void setParameter(final String name, final String value) {
    myRequestData.addProperty(name, value);
  }

  public boolean isParametersEnabled() {
    return ! myDoNotSendParams.isSelected();
  }

  public String getURL() {
    String base = myURLBase.getText();
    String path = (myURL.getSelectedItem() == null) ? "" : myURL.getSelectedItem().toString();
    if (!base.endsWith("/") && path.length() > 0) base += "/";
    base = (path.startsWith("/")) ? base + path.substring(1) : base + path;
    return base.replace(" ", "%20");
  }

  public String getURLBase() {
    return myURLBase.getText();
  }

  public void setURLTemplates(final Set<String> templates) {
    myURL.removeAllItems();
    for (String template : templates) {
      myURL.addItem(template);
    }
    if (!templates.isEmpty()) {
      myURL.addItem("/application.wadl");
    }
  }

  public String getResponse() {
    return myResponse.getText();
  }

  public void setResponse(final String response) {
    myResponse.setText(response == null ? "" : response);
  }

  public void appendToResponse(final String text) {
    final Document document = myResponse.getDocument();
    try {
      final MutableAttributeSet attributeSet = ((StyledEditorKit)myResponse.getEditorKit()).getInputAttributes();
      document.insertString(document.getLength(), text, attributeSet);
    } catch (BadLocationException e) {//
    }
  }

  public HTTPMethod getHttpMethod() {
    String method = (String)myHttpMethod.getSelectedItem();
    for (HTTPMethod type : HTTPMethod.values()) {
      if (type.toString().equals(method)) return type;
    }
    return HTTPMethod.GET;
  }

  private void createUIComponents(final Project project) {
    myViewAsHtml.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myController.openResponseInEditorAs("html", project);
      }
    });

    myViewAsXml.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myController.openResponseInEditorAs("xml", project);
      }
    });

    myViewAsJson.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myController.openResponseInEditorAs("json", project);
      }
    });

    myWebBrowserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myController.openResponseInBrowser();
      }
    });

    myURLBase.setText(WebServicesPluginSettings.getInstance().getLastRestClientHost());
    myGoButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent event) {
        onGoToUrlAction();
      }
    });

    myHeaderData.setImmutableFields(IMMUTABLE_HEADER_PARAMS);
    myHeaderData.setImmutableHeaderExceptions(ACCEPT, CONTENT_TYPE, CACHE_CONTROL);
    myHeaderData.addProperty(ACCEPT, "*/*");
    myHeaderData.addProperty(CONTENT_TYPE, "application/x-www-form-urlencoded");
    myHeaderData.addProperty(CACHE_CONTROL, "no-cache");
    myHeaderParameters.setModel(myHeaderData);
    myHeaderParameters.getColumnModel().getColumn(1).setCellEditor(
      new TypesCellEditor(new String[]{ACCEPT, CONTENT_TYPE}, RestUtils.getAllMimes(project, RestUtils.PREDEFINED_MIME_TYPES)));

    myURLBase.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        super.keyReleased(e);
        myURLBase.setBackground(myController.isValidURL(myURLBase.getText()) ? Color.WHITE : Color.PINK);
      }
    });

    final KeyAdapter enterListener = new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          onGoToUrlAction();
        }
      }
    };

    myURLBase.addKeyListener(enterListener);
    myURL.getEditor().getEditorComponent().addKeyListener(enterListener);

    myController.onUpdateURLs();
    myUpdateButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myController.onUpdateURLs();
      }
    });


    myURL.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        String s = (String)myURL.getSelectedItem();
        if (s == null) return;
        if (s.contains("{") && s.contains("}")) {
          int start = s.indexOf("{");
          int end = s.indexOf("}");
          if (start < end) {
            ((JTextField)myURL.getEditor().getEditorComponent()).setSelectionStart(start);
            ((JTextField)myURL.getEditor().getEditorComponent()).setSelectionEnd(end + 1);
          }
        }
      }
    });

    myParameters.setModel(myRequestData);

    myDoNotSendParams.addChangeListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        myParameters.setEnabled(!myDoNotSendParams.isSelected());
      }
    });
  }

  private void onGoToUrlAction() {
    if (myParameters.isEditing()) myParameters.getCellEditor().stopCellEditing();
    if (myHeaderParameters.isEditing()) myHeaderParameters.getCellEditor().stopCellEditing();
    myTabs.setSelectedIndex(1);
    myController.onGoButtonClick();
  }

  public JComponent getComponent() {
    return myWrappedMainPanel;
  }

  public void dispose() {
    if (myMainPanel != null) {
      Disposer.dispose(this);
    }
  }

  public void updateModel(Project project) {
    myHeaderParameters.getColumnModel().getColumn(1).setCellEditor(
      new TypesCellEditor(new String[]{ACCEPT, CONTENT_TYPE}, RestUtils.getAllMimes(project, RestUtils.PREDEFINED_MIME_TYPES)));
  }

  public void setStatus(String status) {
    myStatus.setText(status);
  }

  private static class RestHelpWrapper extends JPanel implements TypeSafeDataProvider {
    public RestHelpWrapper() {
      super(new BorderLayout());
    }

    public void calcData(final DataKey key, final DataSink sink) {
      if (PlatformDataKeys.HELP_ID.equals(key)) {
        sink.put(key, "reference.tool.windows.rest.client");
      }
    }
  }
}
