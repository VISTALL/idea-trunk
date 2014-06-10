package com.advancedtools.webservices.wsengine;

import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author maxim
 * Date: 27.07.2006
 */
public interface DialogWithWebServicePlatform {
  JComboBox getWebServicePlatformCombo();
  JLabel getWebServicePlaformText();
  void setupWSPlatformSpecificFields();
  @Nullable
  Module getSelectedModule();
  @Nullable
  JComboBox getModuleChooser();

  void configureComboBox(JComboBox combo, List<String> lastValues);
  void doInitFor(JLabel textComponent, JComponent component, char mnemonic);
  void initiateValidation(int delay);
  MyDialogWrapper.ValidationResult createValidationResult(String message, JComponent comp, int delta);

  void pack();
}
