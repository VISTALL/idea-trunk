package com.advancedtools.webservices.actions;

import javax.swing.*;

/**
 * @by Konstantin Bulenkov
 */
public class GenerateWadlFromJavaDialog {
  private JTextField myBaseURI;
  private JTextField myWadlFile;
  private JPanel myCentralPanel;

  public JPanel getCentralPanel() {
    return myCentralPanel;
  }

  public String getBaseURI() {
    return myBaseURI.getText().trim();
  }

  public String getWadlFilePath() {
    return myWadlFile.getText().trim();
  }

  public void setBaseURI(final String baseURI) {
    myBaseURI.setText(baseURI);
  }

  public void setWadlFilePath(final String wadlFilePath) {
    myWadlFile.setText(wadlFilePath);
  }
}
