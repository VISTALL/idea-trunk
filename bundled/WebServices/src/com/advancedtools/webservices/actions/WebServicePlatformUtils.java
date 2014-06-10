package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.advancedtools.webservices.wsengine.DeploymentDialog;
import com.advancedtools.webservices.wsengine.DialogWithWebServicePlatform;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.wsengine.WSEngineManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author maxim
 * Date: 27.07.2006
 */
public class WebServicePlatformUtils {

  public static void initWSPlatforms(final DialogWithWebServicePlatform dialog) {
    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    final WSEngineManager engineManager = instance.getEngineManager();
    final String[] strings = dialog instanceof DeploymentDialog ? engineManager.getAvailableWSEngineNamesWithSupportedDeployment() : engineManager.getAvailableWSEngineNames();
    final List<String> platforms = Arrays.asList(strings);
    dialog.configureComboBox(dialog.getWebServicePlatformCombo(),platforms);

    setupWebServiceEngineField(dialog, instance, platforms);

    dialog.doInitFor(dialog.getWebServicePlaformText(), dialog.getWebServicePlatformCombo(), 'l');

    dialog.getWebServicePlatformCombo().addItemListener(
      new ItemListener() {
        public void itemStateChanged(ItemEvent itemEvent) {
          dialog.setupWSPlatformSpecificFields();
          dialog.initiateValidation(500);
        }
      }
    );

    JComboBox moduleChooser = dialog.getModuleChooser();
    if (moduleChooser != null) {
      moduleChooser.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          setupWebServiceEngineField(dialog, instance, platforms);
        }
      });
    }
  }

  private static void setupWebServiceEngineField(DialogWithWebServicePlatform dialog, WebServicesPluginSettings instance, List<String> platforms) {
    final Module selectedModule = dialog.getSelectedModule();
    final WSEngine engine = selectedModule != null ? EnvironmentFacade.getInstance().getEngineFromModule(selectedModule):null;
    String platformToUse;

    if (engine == null) {
      platformToUse = instance.getLastPlatform();
      dialog.getWebServicePlatformCombo().setVisible(true);
      dialog.getWebServicePlaformText().setVisible(true);
      dialog.pack();
    } else {
      platformToUse = engine.getName();

      dialog.getWebServicePlatformCombo().setVisible(false);
      dialog.getWebServicePlaformText().setVisible(false);
      dialog.pack();
    }

    int selectedPlatformIndex = platformToUse != null ? platforms.indexOf(platformToUse): -1;
    if (selectedPlatformIndex == -1) selectedPlatformIndex = 0;

    dialog.getWebServicePlatformCombo().setSelectedIndex(selectedPlatformIndex);
  }

  public static boolean isJdk1_6SetUpForModule(Module module) {
    final Sdk projectJdk = InvokeExternalCodeUtil.JavaExternalProcessHandler.evaluateJdkForModule(module);

    return projectJdk != null && projectJdk.getVersionString().indexOf("1.6") != -1;
  }

  public static MyDialogWrapper.ValidationResult checkIfPlatformIsSetUpCorrectly(final DialogWithWebServicePlatform dialog, WSEngine engine) {
    final String message = checkIfPlatformIsSetUpCorrectly(engine, dialog.getSelectedModule());
    if (message != null) return dialog.createValidationResult(message, null, 2000);
    return null;
  }

  public static String checkIfPlatformIsSetUpCorrectly(WSEngine engine, Module module) {
    if (engine.getBasePath() == null || !new File(engine.getBasePath()).exists()) {
      String message = "Set valid " + engine.getName() + " path in WebServices plugin settings";

      if (engine instanceof JWSDPWSEngine) {

        if (!isJdk1_6SetUpForModule(module) && !isOldWsGenAccessibleForModule(module)) {
          message += " or use JDK 6";
        } else {
          return null;
        }
      } else if (engine instanceof RestWSEngine) {
        return null;
      } else if (engine instanceof AxisWSEngine) {
        return null;
      }
      return message;
    }
    return null;
  }

  public static boolean isOldWsGenAccessibleForModule(Module module) {    
    return JavaPsiFacade.getInstance(module.getProject())
      .findClass("com.sun.tools.ws.WsGen", GlobalSearchScope.moduleWithLibrariesScope(module)) != null;
  }
}
