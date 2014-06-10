/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.ide.util.TreeClassChooserDialog;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.Nls;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The facet editor tab which is used to set up Osmorc facet settings concerning the generation of the manifest file by
 * Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetManifestGenerationEditorTab extends FacetEditorTab
{
  public OsmorcFacetManifestGenerationEditorTab(FacetEditorContext editorContext)
  {
    _editorContext = editorContext;

    ChangeListener listener = new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        updateGui();
      }
    };

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener()
    {
      public void stateChanged()
      {
        _modified = true;
      }
    });

    watcher.register(_root);
    _bundleActivator.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onBundleActivatorSelect();
      }
    });

  }

  private void updateGui()
  {
    Boolean data = _editorContext.getUserData(OsmorcFacetGeneralEditorTab.MANUAL_MANIFEST_EDITING_KEY);
    boolean isManuallyEdited = data != null ? data : true;
    data = _editorContext.getUserData(OsmorcFacetGeneralEditorTab.BND_CREATION_KEY);
    boolean isBnd = data != null ? data : true;

    _bundleActivatorLabel.setEnabled(!isManuallyEdited && !isBnd);
    _bundleActivator.setEnabled(!isManuallyEdited && !isBnd);
    _bundleSymbolicName.setEnabled(!isManuallyEdited && !isBnd);
    _bundleSymbolicNameLabel.setEnabled(!isManuallyEdited && !isBnd);
    _bundleVersionLabel.setEnabled(!isManuallyEdited && !isBnd);
    _bundleVersion.setEnabled(!isManuallyEdited && !isBnd);
    _additionalProperties.setEnabled(!isManuallyEdited && !isBnd);
    _additionalPropertiesLabel.setEnabled(!isManuallyEdited && !isBnd);


  }

  private void onBundleActivatorSelect()
  {
    Project project = _editorContext.getProject();
    GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesScope(_editorContext.getModule());
    // show a class selector for descendants of BundleActivator
    PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass("org.osgi.framework.BundleActivator", GlobalSearchScope.allScope(project));
    TreeClassChooserDialog dialog =
        new TreeClassChooserDialog(OsmorcBundle.getTranslation("faceteditor.select.bundleactivator"),
            project, searchScope, new TreeClassChooserDialog.InheritanceClassFilterImpl(
                psiClass, false, true,
                null), null);
    dialog.showDialog();
    PsiClass clazz = dialog.getSelectedClass();
    if (clazz != null)
    {
      _bundleActivator.setText(clazz.getQualifiedName());
    }
  }


  @Nls
  public String getDisplayName()
  {
    return "Manifest Generation";
  }

  public JComponent createComponent()
  {
    return _root;
  }

  public boolean isModified()
  {
    return _modified;
  }

  public void apply() throws ConfigurationException
  {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
    configuration.setBundleActivator(_bundleActivator.getText());
    configuration.setBundleSymbolicName(_bundleSymbolicName.getText());
    configuration.setBundleVersion(_bundleVersion.getText());
    configuration.setAdditionalProperties(_additionalProperties.getText());
  }

  public void reset()
  {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
    _bundleActivator.setText(configuration.getBundleActivator());
    _bundleSymbolicName.setText(configuration.getBundleSymbolicName());
    _bundleVersion.setText(configuration.getBundleVersion());
    _additionalProperties.setText(configuration.getAdditionalProperties());
    updateGui();
  }

  @Override
  public void onTabEntering()
  {
    super.onTabEntering();
    updateGui();
  }

  public void disposeUIResources()
  {

  }

  private JPanel _root;
  private JTextField _bundleSymbolicName;
  private TextFieldWithBrowseButton _bundleActivator;
  private JLabel _bundleSymbolicNameLabel;
  private JLabel _bundleActivatorLabel;
  private JTextField _bundleVersion;
  private JLabel _bundleVersionLabel;
  private JTextArea _additionalProperties;
  private JLabel _additionalPropertiesLabel;
  private boolean _modified;
  private final FacetEditorContext _editorContext;
}