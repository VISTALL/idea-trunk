package com.advancedtools.webservices.utils.facet;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.EnableWebServicesSupportUtils;
import com.advancedtools.webservices.actions.WebServicePlatformUtils;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.advancedtools.webservices.wsengine.WSEngineManager;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.application.ApplicationManager;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Maxim
 */
public abstract class BaseWebServicesFacetConfiguration implements FacetConfiguration {
  private WSEngine wsEngine;
  private static final @NonNls String WS_ENGINE_KEY = "ws.engine";

  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager facetValidatorsManager) {
    return new FacetEditorTab[] { new MyFacetEditorTab(editorContext, facetValidatorsManager) };
  }

  public void readExternal(Element element) throws InvalidDataException {
    final String wsEngineName = element.getAttributeValue(WS_ENGINE_KEY);
    wsEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(wsEngineName);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    if (wsEngine != null) element.setAttribute(WS_ENGINE_KEY, wsEngine.getName());
  }

  public void setWsEngine(WSEngine _wsEngine) {
    wsEngine = _wsEngine;
  }

  private class MyFacetEditorTab extends FacetEditorTab {
    private JPanel myPanel;
    private JComboBox wsEngineSelect;
    private final FacetEditorContext facetEditorContext;
    private final WSEngineManager wsEngineManager;

    MyFacetEditorTab(FacetEditorContext _facetEditorContext, FacetValidatorsManager facetValidatorsManager) {
      facetEditorContext = _facetEditorContext;

      wsEngineManager = WebServicesPluginSettings.getInstance().getEngineManager();
      final String[] strings = getEngines(wsEngineManager);
      wsEngineSelect.setModel(new DefaultComboBoxModel(strings));

      reset();

      facetValidatorsManager.registerValidator(new FacetEditorValidator() {
        public ValidationResult check() {
          final WSEngine engine = wsEngineManager.getWSEngineByName((String) wsEngineSelect.getSelectedItem());
          if (engine == null) return new ValidationResult(WSBundle.message("nonspecified.webservices.engine.name.validation.message"));

          if (engine.getBasePath() == null) {
            final String message = WebServicePlatformUtils.checkIfPlatformIsSetUpCorrectly(engine, facetEditorContext.getModule());

            if (message != null) return new ValidationResult(
              message,
              new FacetConfigurationQuickFix() {
                public void run(JComponent jComponent) {
                  final WebServicesPluginSettings settings = ApplicationManager.getApplication().getComponent(WebServicesPluginSettings.class);//ShowSettingsUtil.getInstance().findApplicationConfigurable(WebServicesPluginSettings.class);
                  ShowSettingsUtil.getInstance().editConfigurable(
                    facetEditorContext.getProject(),
                    settings
                  );
                }
              }
            );
          }
          return ValidationResult.OK;
        }
      }, wsEngineSelect);
    }

    @Nls public String getDisplayName() {
      return WSBundle.message("webservices.facet.config.name");
    }

    public JComponent createComponent() {
      return myPanel;
    }

    public boolean isModified() {
      return (wsEngine != null && !wsEngine.getName().equals(wsEngineSelect.getSelectedItem())) ||
        (wsEngine == null && wsEngineSelect.getSelectedItem() != null);
    }

    public void apply() throws ConfigurationException {
      final WSEngine newWsEngine = wsEngineManager.getWSEngineByName((String) wsEngineSelect.getSelectedItem());
      if (wsEngine == newWsEngine) return;
      wsEngine = newWsEngine;

      if (wsEngine.getBasePath() == null) {
        Messages.showWarningDialog(
          facetEditorContext.getProject(),
          WebServicePlatformUtils.checkIfPlatformIsSetUpCorrectly(newWsEngine, facetEditorContext.getModule()),
          "Web Services Engine Is Not Configured"
        );
        return;
      }

      final Module module = facetEditorContext.getModule();

      EnableWebServicesSupportUtils.setupWebServicesInfrastructureForModule(new EnableWebServicesSupportUtils.EnableWebServicesSupportModel() {
        @NotNull
        public Module getModule() {
          return module;
        }

        public WSEngine getWsEngine() {
          return wsEngine;
        }

        public boolean isServerSideSupport() {
          return BaseWebServicesFacetConfiguration.this.isServerSideSupport();
        }

        @Nullable
        public String getBindingType() {
          return null;
        }
      }, facetEditorContext.getProject(), false);
    }

    public void reset() {
      wsEngineSelect.setSelectedItem(wsEngine != null ? wsEngine.getName():JWSDPWSEngine.JWSDP_PLATFORM);
    }

    public void disposeUIResources() {

    }
  }

  protected abstract boolean isServerSideSupport();

  protected abstract String[] getEngines(WSEngineManager wsEngineManager);

  public WSEngine getWsEngine() {
    return wsEngine;
  }
}
