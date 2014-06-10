package com.intellij.struts.facet.ui;

import com.intellij.facet.ui.MultipleFacetSettingsEditor;
import com.intellij.facet.ui.MultipleFacetEditorHelper;
import com.intellij.facet.ui.FacetEditor;
import com.intellij.facet.ui.FacetEditorsFactory;
import com.intellij.util.NotNullFunction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author nik
 */
public class MultipleStrutsFacetEditor extends MultipleFacetSettingsEditor {
  private final MultipleFacetEditorHelper myHelper;
  private final StrutsFacetCommonSettingsPanel mySettingsPanel;

  public MultipleStrutsFacetEditor(@NotNull FacetEditor[] editors) {
    mySettingsPanel = new StrutsFacetCommonSettingsPanel();

    myHelper = FacetEditorsFactory.getInstance().createMultipleFacetEditorHelper();
    myHelper.bind(mySettingsPanel.getDisablePropertyKeysValidationCheckBox(), editors, new NotNullFunction<FacetEditor, JCheckBox>() {
      @NotNull
      public JCheckBox fun(final FacetEditor facetEditor) {
        return facetEditor.getEditorTab(StrutsValidationEditor.class).getSettingsPanel().getDisablePropertyKeysValidationCheckBox();
      }
    });
  }

  public JComponent createComponent() {
    return mySettingsPanel.getMainPanel();
  }

  public void disposeUIResources() {
    myHelper.unbind();
  }
}
