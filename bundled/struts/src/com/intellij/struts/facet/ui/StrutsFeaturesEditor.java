/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.ide.BrowserUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Ref;
import com.intellij.struts.facet.AddStrutsSupportUtil;
import com.intellij.struts.facet.StrutsFacet;
import com.intellij.struts.facet.StrutsSupportModel;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class StrutsFeaturesEditor extends FacetEditorTab {
  private static final Logger LOG = Logger.getInstance("#com.intellij.struts.facet.ui.StrutsFeaturesEditor");

  private JPanel myMainPanel;
  private JPanel myDescriptionPanel;
  private JCheckBox myTilesCheckBox;
  private JCheckBox myValidatorCheckBox;
  private JCheckBox myStrutsELCheckBox;
  private JComboBox myVersionComboBox;
  private JCheckBox myStrutsTaglibCheckBox;
  private JCheckBox myScriptingCheckBox;
  private JCheckBox myExtrasCheckBox;
  private JCheckBox myStrutsFacesCheckBox;

  private final Ref<Boolean> myTilesSupport = Ref.create(false);
  private final Ref<Boolean> myValidatorSupport = Ref.create(false);
  private final Ref<Boolean> myStrutsTaglibSupport = Ref.create(false);
  private final Ref<Boolean> myStrutsELSupport = Ref.create(false);
  private final Ref<Boolean> myScriptingSupport = Ref.create(false);
  private final Ref<Boolean> myExtrasSupport = Ref.create(false);
  private final Ref<Boolean> myStrutsFacesSupport = Ref.create(false);
  private StrutsVersion myVersion;
  private final FacetEditorContext myEditorContext;
  private final FacetLibrariesValidator myLibrariesValidator;
  private final List<BooleanConfigurableElement> myConfigurables = new ArrayList<BooleanConfigurableElement>();
  private LibraryInfo[] myLastLibraryInfos = LibraryInfo.EMPTY_ARRAY;

  public StrutsFeaturesEditor(FacetEditorContext editorContext, final FacetLibrariesValidator librariesValidator) {
    myEditorContext = editorContext;
    myLibrariesValidator = librariesValidator;
    initCheckboxes();
    final Facet parentFacet = myEditorContext.getParentFacet();
    if (parentFacet != null) {
      init((WebFacet)parentFacet);
    }

    myVersionComboBox.setModel(new EnumComboBoxModel<StrutsVersion>(StrutsVersion.class));
    myVersionComboBox.setEnabled(myVersion == null);
    if (myVersion == null) {
      myVersion = StrutsVersion.Struts1_3_8;
    }
    myVersionComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectVersion(getSelectedVersion());
      }
    });

    myStrutsTaglibCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateStrutsELCheckbox();
      }
    });


    myDescriptionPanel.setLayout(new BorderLayout());
    myDescriptionPanel.add(BorderLayout.CENTER, createDescriptionPanel());

    addCheckboxesListeners();
  }

  private void updateStrutsELCheckbox() {
    if (!myStrutsTaglibCheckBox.isSelected()) {
      myStrutsELCheckBox.setSelected(false);
    }
    myStrutsELCheckBox.setEnabled(myStrutsTaglibCheckBox.isSelected());
  }

  private StrutsVersion getSelectedVersion() {
    return (StrutsVersion)myVersionComboBox.getModel().getSelectedItem();
  }

  private void initCheckboxes() {
    myConfigurables.add(new BooleanConfigurableElement(myTilesCheckBox, myTilesSupport));
    myConfigurables.add(new BooleanConfigurableElement(myExtrasCheckBox, myExtrasSupport));
    myConfigurables.add(new BooleanConfigurableElement(myScriptingCheckBox, myScriptingSupport));
    myConfigurables.add(new BooleanConfigurableElement(myStrutsELCheckBox, myStrutsELSupport));
    myConfigurables.add(new BooleanConfigurableElement(myStrutsFacesCheckBox, myStrutsFacesSupport));
    myConfigurables.add(new BooleanConfigurableElement(myStrutsTaglibCheckBox, myStrutsTaglibSupport));
    myConfigurables.add(new BooleanConfigurableElement(myValidatorCheckBox, myValidatorSupport));
  }

  private void addCheckboxesListeners() {
    for (final BooleanConfigurableElement configurable : myConfigurables) {
      configurable.getCheckBox().addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          updateRequiredLibraries();
        }
      });
    }
  }

  private void updateRequiredLibraries() {
    final LibraryInfo[] libraries = getRequiredLibraries();
    if (!Arrays.equals(libraries, myLastLibraryInfos)) {
      myLibrariesValidator.setRequiredLibraries(libraries);
      myLastLibraryInfos = libraries;
    }
  }

  private void selectVersion(final StrutsVersion strutsVersion) {
    switch (strutsVersion) {
      case Struts1_2_9:
        myStrutsTaglibCheckBox.setEnabled(false);
        myStrutsTaglibCheckBox.setSelected(true);

        myStrutsFacesCheckBox.setEnabled(false);
        myStrutsFacesCheckBox.setSelected(false);

        myScriptingCheckBox.setEnabled(false);
        myScriptingCheckBox.setSelected(false);

        myExtrasCheckBox.setEnabled(false);
        myExtrasCheckBox.setSelected(false);
        break;
      case Struts1_3_8:
        myExtrasCheckBox.setEnabled(!myExtrasSupport.get());
        myExtrasCheckBox.setSelected(myExtrasSupport.get());

        myScriptingCheckBox.setEnabled(!myScriptingSupport.get());
        myScriptingCheckBox.setSelected(myScriptingSupport.get());

        myStrutsFacesCheckBox.setEnabled(!myStrutsFacesSupport.get());
        myStrutsFacesCheckBox.setSelected(myStrutsFacesSupport.get());

        myStrutsTaglibCheckBox.setEnabled(!myStrutsTaglibSupport.get() || myEditorContext.isNewFacet());
        myStrutsTaglibCheckBox.setSelected(myStrutsTaglibSupport.get() || myEditorContext.isNewFacet());

        break;
    }
  }

  public void reset() {
    for (BooleanConfigurableElement configurable : myConfigurables) {
      configurable.reset();
    }
    myStrutsTaglibCheckBox.setSelected(myStrutsTaglibSupport.get() || myEditorContext.isNewFacet());
    updateStrutsELCheckbox();
    myVersionComboBox.setSelectedItem(myVersion);
    updateRequiredLibraries();
  }

  public boolean isModified() {
    for (BooleanConfigurableElement configurable : myConfigurables) {
      if (configurable.isModified()) {
        return true;
      }
    }
    return myVersionComboBox.getSelectedItem() != myVersion ||
           myLibrariesValidator.isLibrariesAdded();
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public void disposeUIResources() {
  }

  @Nls
  public String getDisplayName() {
    return "Struts Features";
  }

  public void apply() {
    for (BooleanConfigurableElement configurable : myConfigurables) {
      configurable.apply();
    }
    myVersion = getSelectedVersion();

  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    myLibrariesValidator.onFacetInitialized(facet);
    final StrutsFacet strutsFacet = (StrutsFacet)facet;
    final WebFacet webFacet = strutsFacet.getWebFacet();
    assert webFacet != null;
    AddStrutsSupportUtil.addSupportInWriteCommandAction(webFacet, myValidatorSupport.get(), myTilesSupport.get(), myVersion);
  }

  @NotNull
  private LibraryInfo[] getRequiredLibraries() {
    LibraryInfo[] libs = myVersion.getJars();
    if (myStrutsTaglibCheckBox.isSelected() && myVersion.getStrutsTaglib() != null) {
      libs = ArrayUtil.append(libs, myVersion.getStrutsTaglib());
    }
    if ((myTilesCheckBox.isSelected() || myStrutsFacesCheckBox.isSelected()) && myVersion.getTiles() != null) {
      libs = ArrayUtil.append(libs, myVersion.getTiles());
    }
    if (myStrutsFacesCheckBox.isSelected()) {
      libs = ArrayUtil.append(libs, myVersion.getStrutsFaces());
    }
    if (myScriptingCheckBox.isSelected()) {
      libs = ArrayUtil.mergeArrays(libs, myVersion.getScripting(), LibraryInfo.class);
    }
    if (myExtrasCheckBox.isSelected()) {
      libs = ArrayUtil.append(libs, myVersion.getExtras());
    }
    if (myStrutsELCheckBox.isSelected()) {
      libs = ArrayUtil.mergeArrays(libs, myVersion.getStrutsEl(), LibraryInfo.class);
    }
    return libs;
  }

  private void init(@NotNull WebFacet webFacet) {
    StrutsSupportModel model = StrutsSupportModel.checkStrutsSupport(webFacet);
    if (model.isStrutsLib()) {
      myVersion = model.isStruts13() ? StrutsVersion.Struts1_3_8 : StrutsVersion.Struts1_2_9;
    }
    myTilesSupport.set(model.isTiles());
    myValidatorSupport.set(model.isValidation());
    myStrutsELSupport.set(model.isStrutsEl());
    myStrutsTaglibSupport.set(model.isStrutsTaglib());
    myStrutsFacesSupport.set(model.isStrutsFaces());
    myScriptingSupport.set(model.isScripting());
    myExtrasSupport.set(model.isExtras());
  }

  public static JPanel createDescriptionPanel() {
    JPanel panel = new JPanel(new VerticalFlowLayout());
    panel.add(new JLabel("Struts is a Java web-application development framework."));
    final HyperlinkLabel hyperlinkLabel = new HyperlinkLabel("More about Struts");
    hyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        BrowserUtil.launchBrowser("http://struts.apache.org/");
      }
    });
    panel.add(hyperlinkLabel);
    return panel;
  }

  public String getHelpTopic() {
    return "reference.settings.project.modules.struts.facet.tab.features";
  }

}
