/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.perspectives.create;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassNode;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import static org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo.Relation;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.refactoring.GroovyNamesUtil;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.*;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CreateNewRelation extends DialogWrapper {
  private JPanel myContentPane;
  private JRadioButton myBelongsToRadioButton;
  private ComboBox myNameComboBox;
  private JRadioButton myHasManyRadioButton;
  private JLabel myRelationFieldNameLable;
  private JRadioButton myStableRadioButton;

  private final EventListenerList myListenerList = new EventListenerList();
  private final DomainClassNode mySource;
  private final Project myProject;
  private Relation edgeRelationType;

  private final String newNodeShortTypeText;
  private final GrTypeDefinition mySourceTypeDefinition;
  private final PsiClass myTargetTypeDefinition;

  public CreateNewRelation(DomainClassNode source, DomainClassNode target, Project project) {
    super(project, true);
    mySource = source;
    myProject = project;
    setModal(true);
    myTargetTypeDefinition = target.getTypeDefinition();
    newNodeShortTypeText = myTargetTypeDefinition.getName();
    mySourceTypeDefinition = (GrTypeDefinition) mySource.getTypeDefinition();
    assert newNodeShortTypeText != null;

    setUpLabel(myRelationFieldNameLable);
    setUpNameComboBox();
    setTitle(GrailsBundle.message("create.relation"));

    init();
    updateOkStatus();

    myContentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myNameComboBox.requestFocus();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


  }

  private void setUpLabel(JLabel relationFieldNameLabel) {
    relationFieldNameLabel.setText(GrailsBundle.message("add.field.to.class", mySource.getTypeDefinition().getName()));
  }

  protected void doOKAction() {
    assert mySource != null;
    RelationsCreationsProvider creationsProvider = new RelationsCreationsProvider(myTargetTypeDefinition.getQualifiedName(), mySourceTypeDefinition, myProject, getEnteredName());

    int exitCode = OK_EXIT_CODE;

    if (myBelongsToRadioButton.isSelected()) {
      if (!creationsProvider.canCreateBelongsToRelation()) {
        exitCode = Messages.showDialog(myProject, GrailsBundle.message("Relation.already.defined.Could.you.create.it.in.any.way"), GrailsBundle.message("such.relation.already.defined"), new String[]{"OK", "Cancel"}, 1, IconLoader.findIcon("/general/todoQuestion.png"));
      }

      switch (exitCode) {
        case OK_EXIT_CODE: {
          creationsProvider.createBelongsToRelation();
          break;
        }

        case CANCEL_EXIT_CODE: {
          break;
        }
      }


    } else if (myHasManyRadioButton.isSelected()) {
      if (!creationsProvider.canCreateHasManyRelation()) {
        exitCode = Messages.showDialog(getContentPane(), GrailsBundle.message("Relation.already.defined.Could.you.create.it.in.any.way"), GrailsBundle.message("such.relation.already.defined"), new String[]{"OK", "Cancel"}, 1, IconLoader.findIcon("/general/todoQuestion.png"));
      }

      switch (exitCode) {
        case OK_EXIT_CODE: {
          creationsProvider.createHasManyRelation();
          break;
        }

        case CANCEL_EXIT_CODE: {
          break;
        }
      }

    } else if (myStableRadioButton.isSelected()) {
      if (!creationsProvider.canCreateStrongRelation()) {
        exitCode = Messages.showDialog(getContentPane(), GrailsBundle.message("Relation.already.defined.Could.you.create.it.in.any.way"), GrailsBundle.message("such.relation.already.defined"), new String[]{"OK", "Cancel"}, 1, IconLoader.findIcon("/general/todoQuestion.png"));
      }

      switch (exitCode) {
        case OK_EXIT_CODE: {
          creationsProvider.createStrongRelation();
          break;
        }

        case CANCEL_EXIT_CODE: {
          break;
        }
      }
    }

    if (myBelongsToRadioButton.isSelected()) edgeRelationType = Relation.BELONGS_TO;
    else if (myHasManyRadioButton.isSelected()) edgeRelationType = Relation.HAS_MANY;
    else if (myStableRadioButton.isSelected()) edgeRelationType = Relation.STRONG;
    else edgeRelationType = Relation.UNKNOWN;


    super.doOKAction();
  }

  private void setUpNameComboBox() {
    final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, GroovyFileType.GROOVY_FILE_TYPE, myNameComboBox);

    myNameComboBox.setEditor(comboEditor);

    myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));

    myNameComboBox.setEditable(true);
    myNameComboBox.setMaximumRowCount(8);

    myListenerList.add(DataChangedListener.class, new DataChangedListener());

    myNameComboBox.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            fireNameDataChanged();
          }
        }
    );

    ((EditorTextField) myNameComboBox.getEditor().getEditorComponent()).addDocumentListener(new DocumentListener() {
      public void beforeDocumentChange(DocumentEvent event) {
      }

      public void documentChanged(DocumentEvent event) {
        fireNameDataChanged();
      }
    });

    myNameComboBox.addItem(findSuitableName(mySourceTypeDefinition, firstLetterToLowerCase(newNodeShortTypeText), 1));

    myHasManyRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        myNameComboBox.removeAllItems();
        myNameComboBox.addItem(findSuitableName(mySourceTypeDefinition, StringUtil.pluralize(newNodeShortTypeText.toLowerCase()), 1));
      }
    });

    myBelongsToRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        myNameComboBox.removeAllItems();
        myNameComboBox.addItem(findSuitableName(mySourceTypeDefinition, StringUtil.decapitalize(newNodeShortTypeText), 1));
      }
    });

    myStableRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        myNameComboBox.removeAllItems();
        myNameComboBox.addItem(findSuitableName(mySourceTypeDefinition, StringUtil.decapitalize(newNodeShortTypeText), 1));
      }
    });

  }

  private String findSuitableName(GrTypeDefinition typeDefinition, String baseName, int counter) {
    if (baseName == null) return "";

    final GrField[] fields = typeDefinition.getFields();

    for (GrField field : fields) {
      if ((baseName + counter).equals(field.getName())) {
        return findSuitableName(typeDefinition, baseName, ++counter);
      }

      Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges = new HashMap<DomainClassNode, List<DomainClassRelationsInfo>>();
      if (DomainClassUtils.isHasManyField(field)) {
        DomainClassUtils.buildHasManySourcesToOutEdgesMap(sourcesToOutEdges, field);

        final List<DomainClassRelationsInfo> thisOutEdges = sourcesToOutEdges.get(new DomainClassNode(typeDefinition));
        if (thisOutEdges == null) break;

        for (DomainClassRelationsInfo outEdge : thisOutEdges) {
          if ((baseName + counter).equals(outEdge.getVarName())) {
            return findSuitableName(typeDefinition, baseName, ++counter);
          }
        }
      }

      if (DomainClassUtils.isBelongsToField(field)) {
        sourcesToOutEdges.clear();
        DomainClassUtils.buildBelongsToSourcesToOutEdges(sourcesToOutEdges, field);

        final List<DomainClassRelationsInfo> thisOutEdges = sourcesToOutEdges.get(new DomainClassNode(typeDefinition));
        if (thisOutEdges == null) break;

        for (DomainClassRelationsInfo outEdge : thisOutEdges) {
          if ((baseName + counter).equals(outEdge.getVarName())) {
            return findSuitableName(typeDefinition, baseName, ++counter);
          }
        }
      }

    }

    return baseName + counter;
  }

  @Nullable
  private String firstLetterToLowerCase(String newNodeTypeText) {
    if (newNodeTypeText == null) return null;

    return String.valueOf(newNodeTypeText.charAt(0)).toLowerCase() + newNodeTypeText.substring(1);
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameComboBox;
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myContentPane;
  }

  class DataChangedListener implements EventListener {
    void dataChanged() {
      updateOkStatus();
    }
  }

  private void updateOkStatus() {
    String text = getEnteredName();
    setOKActionEnabled(GroovyNamesUtil.isIdentifier(text) && !(newNodeShortTypeText.equals(text)));
  }

  @Nullable
  public String getEnteredName() {
    if (myNameComboBox.getEditor().getItem() instanceof String &&
        ((String) myNameComboBox.getEditor().getItem()).length() > 0) {
      return (String) myNameComboBox.getEditor().getItem();
    } else {
      return null;
    }
  }

  private void fireNameDataChanged() {
    Object[] list = myListenerList.getListenerList();
    for (Object aList : list) {
      if (aList instanceof DataChangedListener) {
        ((DataChangedListener) aList).dataChanged();
      }
    }
  }

  public Relation getEdgeRelationType() {
    return edgeRelationType;
  }
}
