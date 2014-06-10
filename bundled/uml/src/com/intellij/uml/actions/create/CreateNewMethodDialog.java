/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.actions.create;

import com.intellij.codeInsight.completion.JavaCompletionUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.changeSignature.ChangeSignatureHandler;
import com.intellij.refactoring.changeSignature.ChangeSignatureProcessor;
import com.intellij.refactoring.changeSignature.ParameterInfoImpl;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import com.intellij.refactoring.ui.CodeFragmentTableCellEditor;
import com.intellij.refactoring.ui.CodeFragmentTableCellRenderer;
import com.intellij.refactoring.ui.StringTableCellEditor;
import com.intellij.refactoring.ui.VisibilityPanel;
import com.intellij.refactoring.util.CanonicalTypes;
import com.intellij.refactoring.util.RefactoringMessageUtil;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.ui.*;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlPsiUtil;
import com.intellij.util.Alarm;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.VisibilityUtil;
import com.intellij.util.ui.Table;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class CreateNewMethodDialog extends DialogWrapper {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uml.actions.create.CreateNewMethodDialog");
  private final PsiMethod myMethod;
  private EditorTextField myNameField;
  private EditorTextField myReturnTypeField;
  private JTable myParametersTable;
  private final ParameterTableModel myParametersTableModel;
  private JTextArea mySignatureArea;
  private final Alarm myUpdateSignatureAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

  private VisibilityPanel myVisibilityPanel;
  private PsiTypeCodeFragment myReturnTypeCodeFragment;
  private JTable myExceptionsTable;
  private final ExceptionsTableModel myExceptionsTableModel;
  private Set<PsiMethod> myMethodsToPropagateParameters = null;
  private Set<PsiMethod> myMethodsToPropagateExceptions = null;
  private Action myCreateAction;
  private boolean myCbPreviewResults;
  protected final Project myProject;
  private final PsiClass myClass;
  private final @NonNls JCheckBox myAbstract = new JCheckBox("abstract");

  public CreateNewMethodDialog(Project project, final PsiMethod method, PsiClass owner) {
    super (project, true);
    myClass = owner;
    myCbPreviewResults = true;
    myProject = project;
    myMethod = method;
    myParametersTableModel = new ParameterTableModel(myMethod.getParameterList(), null, this);
    myExceptionsTableModel = new ExceptionsTableModel(myMethod.getThrowsList());

    setParameterInfos(getParameterInfos(method));
    myExceptionsTableModel.setTypeInfos(method);

    setTitle(UmlBundle.message(method.isConstructor() ? "create.new.constructor.dialog.title" : "create.new.method.dialog.title"));
    init();
    doUpdateSignature();
  }

  public void setParameterInfos(List<ParameterInfoImpl> parameterInfos) {
    myParametersTableModel.setParameterInfos(parameterInfos, myMethod.getParameterList());
    updateSignature();
  }

  private static List<ParameterInfoImpl> getParameterInfos(PsiMethod method) {
    final ArrayList<ParameterInfoImpl> result = new ArrayList<ParameterInfoImpl>();
    final PsiParameter[] parameters = method.getParameterList().getParameters();
    for (int i = 0; i < parameters.length; i++) {
      PsiParameter parameter = parameters[i];
      ParameterInfoImpl info = new ParameterInfoImpl(i, parameter.getName(), parameter.getType());
      info.setDefaultValue("null");
      result.add(info);
    }
    return result;
  }

  @NonNls
  private String getMethodName() {
    if (myMethod.isConstructor()) {
      final String name = myClass.getName();
      return name == null ? "_Dummy_" : name;
    }
    return (myNameField != null) ? myNameField.getText().trim() : myMethod.getName();
  }

  @Nullable
  private CanonicalTypes.Type getReturnType() {
    if (myReturnTypeField != null) {
      try {
        final PsiType type = myReturnTypeCodeFragment.getType();
        return CanonicalTypes.createTypeWrapper(type);
      } catch (Exception e) {
        //LOG.error(e);
        return null;
      }
    }

    return null;
  }

  private String getVisibility() {
    return (myVisibilityPanel != null) ?
           myVisibilityPanel.getVisibility() : VisibilityUtil.getVisibilityModifier(myMethod.getModifierList());
  }

  public ParameterInfoImpl[] getParameters() {
    return myParametersTableModel.getParameters();
  }

  private ThrownExceptionInfo[] getExceptions() {
    return myExceptionsTableModel.getThrownExceptions();
  }

  public boolean isGenerateDelegate() {
    return false;
  }

  public JComponent getPreferredFocusedComponent() {
    return myParametersTableModel.getRowCount() > 0 ? myParametersTable : myNameField;
  }


  protected JComponent createNorthPanel() {
    JPanel panel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP));
    if (!myMethod.isConstructor()) {
      JLabel namePrompt = new JLabel();
      myNameField = new EditorTextField();
      namePrompt.setText(RefactoringBundle.message("name.prompt"));
      panel.add(namePrompt);
      panel.add(myNameField);

      JLabel typePrompt = new JLabel();
      panel.add(typePrompt);
      final PsiElementFactory factory = JavaPsiFacade.getInstance(myMethod.getProject()).getElementFactory();
      myReturnTypeCodeFragment = factory.createTypeCodeFragment(myMethod.getReturnTypeElement().getText(), myMethod.getParameterList(), true, true);
      final Document document = PsiDocumentManager.getInstance(myProject).getDocument(myReturnTypeCodeFragment);
      myReturnTypeField = new EditorTextField(document, myProject, StdFileTypes.JAVA);
      myReturnTypeField.setText("");
      typePrompt.setText(RefactoringBundle.message("changeSignature.return.type.prompt"));
      panel.add(myReturnTypeField);

      final DocumentListener documentListener = new DocumentListener() {
        public void beforeDocumentChange(DocumentEvent event) {
        }

        public void documentChanged(DocumentEvent event) {
          updateSignature();
        }
      };

      myNameField.addDocumentListener(documentListener);
      myReturnTypeField.addDocumentListener(documentListener);
    }
    return panel;
  }

  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel subPanel = new JPanel(new BorderLayout());
    subPanel.add(createParametersPanel(), BorderLayout.CENTER);

    if (myClass != null && !myClass.isInterface()) {
      myVisibilityPanel = new VisibilityPanel(false, false);
      myVisibilityPanel.setVisibility(VisibilityUtil.getVisibilityModifier(myMethod.getModifierList()));
      myVisibilityPanel.addStateChangedListener(new VisibilityPanel.StateChanged() {
        public void visibilityChanged() {
          updateSignature();
        }
      });
      myAbstract.addChangeListener(new ChangeListener() {
        public void stateChanged(final ChangeEvent e) {
          updateSignature();
        }
      });
      JPanel modifiersPanel = new JPanel(new BorderLayout());
      modifiersPanel.add(myVisibilityPanel, BorderLayout.CENTER);
      modifiersPanel.add(myAbstract, BorderLayout.SOUTH);
      subPanel.add(modifiersPanel, BorderLayout.EAST);
    }

    panel.add(subPanel, BorderLayout.CENTER);

    JPanel subPanel1 = new JPanel(new GridBagLayout());
    subPanel1.add(createExceptionsPanel(), new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(4,4,4,0), 0, 0));
    subPanel1.add(createSignaturePanel(), new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,0,4,4), 0, 0));
    panel.add(subPanel1, BorderLayout.SOUTH);

    return panel;
  }

  protected String getDimensionServiceKey() {
    return "refactoring.ChangeSignatureDialog";
  }

  private JPanel createParametersPanel() {
    myParametersTable = new Table(myParametersTableModel);
    myParametersTable.setFocusCycleRoot(true);
    configureParameterTableEditors();
    return createTablePanelImpl(myParametersTable, myParametersTableModel, RefactoringBundle.message("parameters.border.title"), true);
  }

  private JPanel createExceptionsPanel() {
    myExceptionsTable = new Table(myExceptionsTableModel);
    configureExceptionTableEditors();
    return createTablePanelImpl(myExceptionsTable, myExceptionsTableModel, RefactoringBundle.message("changeSignature.exceptions.panel.border.title"), false);
  }

  private JPanel createTablePanelImpl (JTable table, RowEditableTableModel tableModel, String borderTitle, boolean addMnemonics) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(IdeBorderFactory.createTitledBorder(borderTitle));

    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(table);

    JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.add(scrollPane, BorderLayout.CENTER);

    tablePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    panel.add(tablePanel, BorderLayout.CENTER);

    table.setPreferredScrollableViewportSize(new Dimension(450, table.getRowHeight() * 8));
    table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().setSelectionInterval(0, 0);
    table.setSurrendersFocusOnKeystroke(true);

    JPanel buttonsPanel = EditableRowTable.createButtonsTable(table, tableModel, addMnemonics);

    panel.add(buttonsPanel, BorderLayout.EAST);

    tableModel.addTableModelListener(
      new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          updateSignature();
        }
      }
    );

    return panel;
  }

  private void configureParameterTableEditors() {
    myParametersTable.getColumnModel().getColumn(0).setCellRenderer(new CodeFragmentTableCellRenderer(myProject));
    myParametersTable.getColumnModel().getColumn(1).setCellRenderer(new MyCellRenderer());
    myParametersTable.getColumnModel().getColumn(0).setCellEditor(new CodeFragmentTableCellEditor(myProject));
    myParametersTable.getColumnModel().getColumn(1).setCellEditor(new MyNameTableCellEditor(myProject));
  }

  private void configureExceptionTableEditors () {
    myExceptionsTable.getColumnModel().getColumn(0).setCellRenderer(new CodeFragmentTableCellRenderer(myProject));
    myExceptionsTable.getColumnModel().getColumn(0).setCellEditor(new CodeFragmentTableCellEditor(myProject));
  }

  private void completeVariable(EditorTextField editorTextField, PsiType type) {
    Editor editor = editorTextField.getEditor();
    String prefix = editorTextField.getText();
    if (prefix == null) prefix = "";
    Set<LookupElement> set = new LinkedHashSet<LookupElement>();
    JavaCompletionUtil.completeVariableNameForRefactoring(myProject, set, prefix, type, VariableKind.PARAMETER);

    LookupElement[] lookupItems = set.toArray(new LookupElement[set.size()]);
    editor.getCaretModel().moveToOffset(prefix.length());
    editor.getSelectionModel().removeSelection();
    LookupManager.getInstance(myProject).showLookup(editor, lookupItems, prefix);
  }

  private JComponent createSignaturePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createCompoundBorder(IdeBorderFactory.createTitledBorder(RefactoringBundle.message("signature.preview.border.title")), IdeBorderFactory.createEmptyBorder(new Insets(4, 4, 4, 4))));

    String s = calculateSignature();
    s = StringUtil.convertLineSeparators(s);
    int height = new StringTokenizer(s, "\n\r").countTokens() + 2;
    if (height > 10) height = 10;
    mySignatureArea = new JTextArea(height, 50);
    mySignatureArea.setEditable(false);
    mySignatureArea.setBackground(getContentPane().getBackground());
    //mySignatureArea.setFont(myTableFont);
    JScrollPane scrollPane = new JScrollPane(mySignatureArea);
    scrollPane.setBorder(IdeBorderFactory.createEmptyBorder(new Insets(0,0,0,0)));
    panel.add(scrollPane, BorderLayout.CENTER);

    updateSignature();
    return panel;
  }

  private void updateSignature() {
    if (mySignatureArea == null) return;

    myUpdateSignatureAlarm.cancelAllRequests();
    myUpdateSignatureAlarm.addRequest(new Runnable() {
      public void run() {
        doUpdateSignature();
      }
    }, 100, ModalityState.stateForComponent(mySignatureArea));
  }

  private void doUpdateSignature() {
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    String signature = calculateSignature();
    mySignatureArea.setText(signature);
  }

  private boolean mayPropagateExceptions() {
    final ThrownExceptionInfo[] thrownExceptions = myExceptionsTableModel.getThrownExceptions();
    final PsiClassType[] types = myMethod.getThrowsList().getReferencedTypes();
    if (thrownExceptions.length < types.length) return false;
    for (int i = 0; i < types.length; i++) {
      if (thrownExceptions[i].getOldIndex() != i) return false;
    }
    return true;
  }

  private boolean mayPropagateParameters() {
    final ParameterInfoImpl[] infos = myParametersTableModel.getParameters();
    final PsiParameter[] parameters = myMethod.getParameterList().getParameters();
    if (infos.length < parameters.length) return false;
    for (int i = 0; i < parameters.length; i++) {
      if (infos[i].oldParameterIndex != i) return false;
    }
    return true;
  }

  private String calculateSignature() {
    @NonNls StringBuilder buffer = new StringBuilder();

    PsiModifierList modifierList = myMethod.getModifierList();
    String modifiers = modifierList.getText();
    String oldModifier = VisibilityUtil.getVisibilityModifier(modifierList);
    String newModifier = getVisibility();
    String newModifierStr = VisibilityUtil.getVisibilityString(newModifier);
    if (!newModifier.equals(oldModifier)) {
      int index = modifiers.indexOf(oldModifier);
      if (index >= 0) {
        StringBuilder buf = new StringBuilder(modifiers);
        buf.replace(index,
                    index + oldModifier.length() + ("".equals(newModifierStr) ? 1 : 0),
                    newModifierStr);
        modifiers = buf.toString();
      }
      else {
        if (!"".equals(newModifierStr)) newModifierStr += " ";
        modifiers = newModifierStr + modifiers;
      }
    }

    buffer.append(modifiers);
    if (modifiers.length() > 0 &&
        !StringUtil.endsWithChar(modifiers, '\n') && !StringUtil.endsWithChar(modifiers, '\r') && !StringUtil.endsWithChar(modifiers, ' ')) {
      buffer.append(" ");
    }
    if (myAbstract.isSelected()) {
      buffer.append("abstract ");
    }
    if (!myMethod.isConstructor()) {
      final CanonicalTypes.Type returnType = getReturnType();
      if (returnType != null) {
        buffer.append(returnType.getTypeText());
      }
      buffer.append(" ");
    }
    buffer.append(getMethodName());
    buffer.append("(");

    final List<PsiTypeCodeFragment> codeFraments = myParametersTableModel.getCodeFraments();

    final ParameterInfoImpl[] parameterInfos = myParametersTableModel.getParameters();
    //LOG.assertTrue(codeFraments.size() == parameterInfos.length);
    final String indent = "    ";
    for (int i = 0; i < parameterInfos.length; i++) {
      ParameterInfoImpl info = parameterInfos[i];
      if (i > 0) {
        buffer.append(",");
      }
      buffer.append("\n");
      buffer.append(indent);
      buffer.append(codeFraments.get(i).getText());
      buffer.append(" ");
      buffer.append(info.getName());
    }
    if (parameterInfos.length > 0) {
      buffer.append("\n");
    }
    buffer.append(")");
    PsiTypeCodeFragment[] thrownExceptionsFragments = myExceptionsTableModel.getTypeCodeFragments();
    if (thrownExceptionsFragments.length > 0) {
      buffer.append("\n");
      buffer.append("throws\n");
      for (PsiTypeCodeFragment thrownExceptionsFragment : thrownExceptionsFragments) {
        String text = thrownExceptionsFragment.getText();
        buffer.append(indent);
        buffer.append(text);
        buffer.append("\n");
      }
    }
    return buffer.toString();
  }

  protected void doAction() {
    stopEditing();
    String message = validateAndCommitData();
    if (message != null) {
      Messages.showErrorDialog(myProject, message, RefactoringBundle.message("error.incorrect.data"));
      return;
    }
    if (!checkMethodConflicts()) {
      return;
    }

    if (myMethodsToPropagateParameters != null && !mayPropagateParameters()) {
      Messages.showWarningDialog(myProject, RefactoringBundle.message("changeSignature.parameters.wont.propagate"), ChangeSignatureHandler.REFACTORING_NAME);
      myMethodsToPropagateParameters = null;
    }
    if (myMethodsToPropagateExceptions != null && !mayPropagateExceptions()) {
      Messages.showWarningDialog(myProject, RefactoringBundle.message("changeSignature.exceptions.wont.propagate"), ChangeSignatureHandler.REFACTORING_NAME);
      myMethodsToPropagateExceptions = null;
    }

    final ParameterInfoImpl[] parameterInfos = getParameters();
    for (ParameterInfoImpl parameterInfo : parameterInfos) {
      parameterInfo.setDefaultValue("null");
    }
    invokeRefactoring(new ChangeSignatureProcessor(getProject(), myMethod, false,
                                                   getVisibility(), getMethodName(), getReturnType(), parameterInfos, getExceptions(),
                                                   myMethodsToPropagateParameters,
                                                   myMethodsToPropagateExceptions));
    if (myAbstract.isSelected()) {
      UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable(){
        public void run() {
          try {
            PsiUtil.setModifierProperty(myMethod, PsiModifier.ABSTRACT, true);
          } catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }
      });
    }
  }

  private String validateAndCommitData() {
    PsiManager manager = PsiManager.getInstance(myProject);
    String name = getMethodName();
    if (!JavaPsiFacade.getInstance(manager.getProject()).getNameHelper().isIdentifier(name)) {
      return RefactoringMessageUtil.getIncorrectIdentifierMessage(name);
    }

    if (!myMethod.isConstructor()) {
      try {
        myReturnTypeCodeFragment.getType();
      }
      catch (PsiTypeCodeFragment.TypeSyntaxException e) {
        myReturnTypeField.requestFocus();
        return RefactoringBundle.message("changeSignature.wrong.return.type", myReturnTypeCodeFragment.getText());
      }
      catch (PsiTypeCodeFragment.NoTypeException e) {
        myReturnTypeField.requestFocus();
        return RefactoringBundle.message("changeSignature.no.return.type");
      }
    }

    final List<PsiTypeCodeFragment> codeFraments = myParametersTableModel.getCodeFraments();
    final List<JavaCodeFragment> defaultValueFraments = myParametersTableModel.getDefaultValueFraments();
    ParameterInfoImpl[] parameterInfos = myParametersTableModel.getParameters();
    final int newParametersNumber = parameterInfos.length;
    //LOG.assertTrue(codeFraments.size() == newParametersNumber);

    for (int i = 0; i < newParametersNumber; i++) {
      ParameterInfoImpl info = parameterInfos[i];
      PsiTypeCodeFragment psiCodeFragment = codeFraments.get(i);

      if (!JavaPsiFacade.getInstance(manager.getProject()).getNameHelper().isIdentifier(info.getName())) {
        return RefactoringMessageUtil.getIncorrectIdentifierMessage(info.getName());
      }

      final PsiType type;
      try {
        type = psiCodeFragment.getType();
      }
      catch (PsiTypeCodeFragment.TypeSyntaxException e) {
        return RefactoringBundle.message("changeSignature.wrong.type.for.parameter", psiCodeFragment.getText(), info.getName());
      }
      catch (PsiTypeCodeFragment.NoTypeException e) {
        return RefactoringBundle.message("changeSignature.no.type.for.parameter", info.getName());
      }

      info.setType(type);

      if (type instanceof PsiEllipsisType && i != newParametersNumber - 1) {
        return RefactoringBundle.message("changeSignature.vararg.not.last");
      }
    }

    ThrownExceptionInfo[] exceptionInfos = myExceptionsTableModel.getThrownExceptions();
    PsiTypeCodeFragment[] typeCodeFragments = myExceptionsTableModel.getTypeCodeFragments();
    for (int i = 0; i < exceptionInfos.length; i++) {
      ThrownExceptionInfo exceptionInfo = exceptionInfos[i];
      PsiTypeCodeFragment typeCodeFragment = typeCodeFragments[i];
      try {
        PsiType type = typeCodeFragment.getType();
        if (!(type instanceof PsiClassType)) {
          return RefactoringBundle.message("changeSignature.wrong.type.for.exception", typeCodeFragment.getText());
        }

        PsiClassType throwable = JavaPsiFacade.getInstance(myMethod.getProject()).getElementFactory()
          .createTypeByFQClassName("java.lang.Throwable", type.getResolveScope());
        if (!throwable.isAssignableFrom(type)) {
          return RefactoringBundle.message("changeSignature.not.throwable.type", typeCodeFragment.getText());
        }
        exceptionInfo.setType((PsiClassType)type);
      }
      catch (PsiTypeCodeFragment.TypeSyntaxException e) {
        return RefactoringBundle.message("changeSignature.wrong.type.for.exception", typeCodeFragment.getText());
      }
      catch (PsiTypeCodeFragment.NoTypeException e) {
        return RefactoringBundle.message("changeSignature.no.type.for.exception");
      }
    }

    return null;
  }

  private boolean checkMethodConflicts() {
    try {
      PsiManager manager = PsiManager.getInstance(myProject);
      ParameterInfoImpl[] parameters = getParameters();

      for (ParameterInfoImpl info : parameters) {
        final PsiType parameterType = info.createType(myMethod, manager);

        if (!RefactoringUtil.isResolvableType(parameterType)) {
          final int ret = Messages.showOkCancelDialog(myProject,
                                                      RefactoringBundle.message("changeSignature.cannot.resolve.type", info.getTypeText()),
                                                      ChangeSignatureHandler.REFACTORING_NAME, Messages.getErrorIcon()
          );
          if (ret != 0) return false;
        }

      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
    return true;
  }

  private void stopEditing() {
    TableUtil.stopEditing(myParametersTable);
  }

  protected void doHelpAction() {
    HelpManager.getInstance().invokeHelp(HelpID.CHANGE_SIGNATURE);
  }

  public final boolean isPreviewUsages() {
    return myCbPreviewResults;
  }

  protected void createDefaultActions() {
    super.createDefaultActions ();
    myCreateAction = new CreateAction();
  }

  /**
   * @return default implementation of "Refactor" action.
   */
  protected final Action getCreateAction() {
    return myCreateAction;
  }


  private void doRefactorAction () {
    myCbPreviewResults = false;
    doAction();
  }

  protected final void closeOKAction() {
    super.doOKAction();
  }

  protected final void doOKAction() {
    doAction();
  }

  protected void validateButtons() {
    getCreateAction().setEnabled(true);
  }

  protected Action[] createActions() {
    return new Action[]{getCreateAction(), getCancelAction()};
  }

  protected Project getProject() {
    return myProject;
  }

  private class CreateAction extends AbstractAction {
    public CreateAction() {
      putValue(NAME, UmlBundle.message("create.method"));
      putValue(DEFAULT_ACTION, Boolean.TRUE);
    }

    public void actionPerformed(ActionEvent e) {
      doRefactorAction ();
    }
  }

  protected void invokeRefactoring(BaseRefactoringProcessor processor) {
    final Runnable prepareSuccessfulCallback = new Runnable() {
      public void run() {
        close(OK_EXIT_CODE);
      }
    };
    processor.setPrepareSuccessfulSwingThreadCallback(prepareSuccessfulCallback);
    processor.setPreviewUsages(isPreviewUsages());
    processor.run();
  }


  private class MyCellRenderer extends ColoredTableCellRenderer {

    public void customizeCellRenderer(JTable table, Object value,
                                      boolean isSelected, boolean hasFocus, int row, int column) {
      if (value == null) return;
      if (!myParametersTableModel.isCellEditable(row, myParametersTable.convertColumnIndexToModel(column))) {
        setBackground(getBackground().darker());
      }
      append((String)value, new SimpleTextAttributes(Font.PLAIN, null));
    }
  }

  private class MyNameTableCellEditor extends StringTableCellEditor {
    public MyNameTableCellEditor(Project project) {
      super(project);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      final EditorTextField textField = (EditorTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
      textField.registerKeyboardAction(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int column = myParametersTable.convertColumnIndexToModel(myParametersTable.getEditingColumn());
          if (column == 1) {
            int row = myParametersTable.getEditingRow();
            PsiType type = myParametersTableModel.getTypeByRow(row);
            if (type != null) {
              completeVariable(textField, type);
            }
          }
        }
      }, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
      return textField;
    }
  }
}
