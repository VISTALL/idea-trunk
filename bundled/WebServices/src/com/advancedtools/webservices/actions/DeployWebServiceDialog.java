package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.xfire.XFireWSEngine;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.axis2.Axis2WSEngine;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.utils.DeployUtils;
import com.advancedtools.webservices.utils.ui.GenerateFromJavaCodeDialogBase;
import com.advancedtools.webservices.wsengine.DeploymentDialog;
import com.advancedtools.webservices.wsengine.DialogWithWebServicePlatform;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * @by Maxim.Mossienko
 */
public class DeployWebServiceDialog extends GenerateFromJavaCodeDialogBase implements DialogWithWebServicePlatform, DeploymentDialog {
  private ComboBox webServicePlatform;
  private JLabel webServicePlatformText;
  private JLabel status;
  private JLabel statusText;
  private JPanel myPanel;

  private WSEngine currentEngine;
  private boolean myDoUpdateFromSetClass;
  private boolean myDoUpdateFromClassTextChange;
  private boolean myDoUpdateFromConfiguringWebServices;

  private TextFieldWithBrowseButton myClassName;
  private JLabel myClassNameText;
  private JLabel myWSNameText;
  private ComboBox myWSName;

  private JTextField myWSNamespace;
  private JLabel myWsNamespaceText;
  private ComboBox myTargetModule;
  private JLabel myTargetModuleText;

  private boolean myWSNamespaceChanged;
  private boolean myWSNameChanged;
  private JLabel myWsServiceStyleText;
  private ComboBox myWsServiceStyle;

  private JLabel myWsUseOfItemsText;
  private ComboBox myWsUseOfItems;
  private JTable myMethodsTable;
  private JScrollPane myMethodsPane;
  private JCheckBox myAddRequiredLibraries;

  public DeployWebServiceDialog(final Project project, @Nullable PsiClass clazz, @Nullable DeployWebServiceDialog previousDialog) {
    super(project, previousDialog != null ? previousDialog.getCurrentClass() : clazz);

    setTitle(WSBundle.message("expose.class.as.web.service.dialog.title"));

    final JTextField classNameTextField = myClassName.getTextField();
    doInitFor(myClassNameText, classNameTextField, 'C');

    classNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        if (!myDoUpdateFromSetClass) {
          myDoUpdateFromClassTextChange = true;
          setCurrentClass(EnvironmentFacade.getInstance().findClass(classNameTextField.getText(), myProject, GlobalSearchScope.moduleScope(getSelectedModule())));
          myDoUpdateFromClassTextChange = false;
        }
      }
    });

    myClassName.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project).createProjectScopeChooser(
          "Choose Class",
          getCurrentClass()
        );
        chooser.showDialog();
        PsiClass selectedClass = chooser.getSelectedClass();
        if (selectedClass != null) {
          setCurrentClass(selectedClass);
        }
      }
    });

    doInitFor(myWSNameText, myWSName, 'W');

    myWSName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String webServiceName = (String) myWSName.getSelectedItem();
        final Module selectedModule = getSelectedModule();
        final String[] servicesOperations = selectedModule != null ? currentEngine.getWebServicesOperations(webServiceName, selectedModule):ArrayUtil.EMPTY_STRING_ARRAY;

        if (servicesOperations.length != 0) {
          myMethodsTable.setModel(new DefaultTableModel(new Object[][] {servicesOperations},new Object[] { "name" }));
        }
      }
    });

    ((JTextField)myWSName.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        if (!myDoUpdateFromSetClass && !myDoUpdateFromConfiguringWebServices) myWSNameChanged = true;
      }

      public void removeUpdate(DocumentEvent e) {
        if (!myDoUpdateFromSetClass && !myDoUpdateFromConfiguringWebServices) myWSNameChanged = true;
      }

      public void changedUpdate(DocumentEvent e) {
        if (!myDoUpdateFromSetClass && !myDoUpdateFromConfiguringWebServices) myWSNameChanged = true;
      }
    });

    ModuleManager moduleManager = ModuleManager.getInstance(project);
    Module[] modules = moduleManager.getModules();

    myTargetModule.setModel(new DefaultComboBoxModel(modules));

    if (clazz != null) {
      final Module moduleForFile = ProjectRootManager.getInstance(myProject).getFileIndex().getModuleForFile(clazz.getContainingFile().getVirtualFile());
      if (moduleForFile != null) myTargetModule.setSelectedItem(moduleForFile);
    } else {
      if (modules.length != 0) myTargetModule.setSelectedIndex(0);
    }

    doInitFor(myWsNamespaceText, myWSNamespace, 'N');
    doInitFor(myTargetModuleText, myTargetModule, 'M');
    configureComboBox(
      myWsUseOfItems,
      Arrays.asList(WSEngine.WS_USE_LITERAL, WSEngine.WS_USE_ENCODED)
    );
    myWsUseOfItemsText.setLabelFor(myWsUseOfItems);
    myWsUseOfItemsText.setDisplayedMnemonic('e');

    configureComboBox(myWsServiceStyle, Arrays.asList(WSEngine.WS_DOCUMENT_STYLE, WSEngine.WS_WRAPPED_STYLE, WSEngine.WS_RPC_STYLE, "MESSAGE"));
    myWsServiceStyleText.setLabelFor(myWsServiceStyle);
    myWsServiceStyleText.setDisplayedMnemonic('y');

    myWSNamespace.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        if (!myDoUpdateFromSetClass) myWSNamespaceChanged = true;
      }

      public void removeUpdate(DocumentEvent e) {
        if (!myDoUpdateFromSetClass) myWSNamespaceChanged = true;
      }

      public void changedUpdate(DocumentEvent e) {
        if (!myDoUpdateFromSetClass) myWSNamespaceChanged = true;
      }
    });

    myTargetModule.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        setupAvailableServices();
      }
    });

    myAddRequiredLibraries.setMnemonic('r');
    myAddRequiredLibraries.setSelected(WebServicesPlugin.getInstance(project).isToAddRequiredLibraries());
    if (EnvironmentFacade.isSelenaOrBetter()) myAddRequiredLibraries.setVisible(false);

    init();

    WebServicePlatformUtils.initWSPlatforms( this );
    setupWSPlatformSpecificFields();
    initiateValidation(1000);

    if (clazz == null && previousDialog != null) {
      myTargetModule.setSelectedItem(previousDialog.myTargetModule.getSelectedItem());
      myWSName.setSelectedItem(previousDialog.myWSName.getSelectedItem());
      myWSNamespace.setText(previousDialog.myWSNamespace.getText());
      myWsServiceStyle.setSelectedItem(previousDialog.myWsServiceStyle.getSelectedItem());
      myWsUseOfItems.setSelectedItem(previousDialog.myWsUseOfItems.getSelectedItem());
    }
  }

  private void setupAvailableServices() {
    myDoUpdateFromConfiguringWebServices = true;
    try {
      final Module selectedModule = (Module) myTargetModule.getSelectedItem();

      myWSName.setModel(
        new DefaultComboBoxModel(
          selectedModule != null ? currentEngine.getAvailableWebServices(selectedModule):ArrayUtil.EMPTY_OBJECT_ARRAY
        )
      );
      
      setGuessedClassNameAndNamespace(getCurrentClass());
    } finally {
      myDoUpdateFromConfiguringWebServices = false;
    }
  }

  protected void doSetClassNameText(JComponent className, String text) {
    if (!myDoUpdateFromClassTextChange) super.doSetClassNameText(className, text);
  }

  protected ValidationResult doValidate(ValidationData _data) {
    final PsiClass clazz = getCurrentClass();
    final ValidationResult validationResult;

    if (clazz != null && clazz.isInterface()) {
      String uptoDateCheck = DeployUtils.checkIfClassIsUpToDate(myProject, clazz);

      if (uptoDateCheck != null) {
        validationResult = new ValidationResult(uptoDateCheck, null, 1000);
      } else {
        validationResult = null;
      }
    } else {
      validationResult = super.doValidate(_data);
    }

    if (validationResult != null) return validationResult;
    ValidationResult result = WebServicePlatformUtils.checkIfPlatformIsSetUpCorrectly(this, currentEngine);

    if (result != null) {
      return result;
    }

    String notAcceptableClazzMessage = currentEngine.checkNotAcceptableClassForGenerateWsdl(clazz);

    if (notAcceptableClazzMessage != null) {
      return createValidationResult(notAcceptableClazzMessage, null, 1000);
    }
    
    MyValidationData data = (MyValidationData) _data;

    return doValidationWithData(data);
  }

  private ValidationResult doValidationWithData(final MyValidationData data) {
    if (data.module == null) {
      return new ValidationResult(WSBundle.message("invalid.web.module.selected.validation.message"), myTargetModule);
    }

    try {
      final URL url = new URL(data.wsNamespace);
    } catch (MalformedURLException e) {
      return new ValidationResult(WSBundle.message("invalid.url.validation.message"), myWSNamespace);
    }

    try {
      EnvironmentFacade.getInstance().checkIsIdentifier(PsiManager.getInstance(myProject), data.wsName);
    } catch (IncorrectOperationException e) {
      return new ValidationResult(WSBundle.message("invalid.web.service.name.validation.message"), myWSName);
    }

    PsiClass clazz = getCurrentClass();
    if (clazz != null) {
      final Module moduleForFile = ProjectRootManager.getInstance(myProject).getFileIndex().getModuleForFile(clazz.getContainingFile().getVirtualFile());

      if (moduleForFile != data.module) {
        final Module[] dependencies = ModuleRootManager.getInstance(moduleForFile).getDependencies();
        boolean notAcceptableModule = true;

        for(Module module:dependencies) {
          if (module == data.module) {
            notAcceptableModule = false;
            break;
          }
        }

        if (notAcceptableModule) {
          return new ValidationResult("Selected class is not in module",myTargetModule);
        }

      }

      String notAcceptableMessage = currentEngine.checkNotAcceptableClassForDeployment(clazz);
      if (notAcceptableMessage != null) {
        return new ValidationResult(notAcceptableMessage,myClassName);
      }
    } else {
      return new ValidationResult("Class does not exist",myClassName.getTextField());
    }

    if (!data.wsClassName.matches("(?:\\w+\\.)*\\w+")) {
      return new ValidationResult(WSBundle.message("invalid.web.service.class.name.validation.message"), myClassName.getTextField());
    }
    return null;
  }

  protected MyValidationData createValidationData() {
    return new MyValidationData();
  }

  protected JComponent getClassName() {
    return myClassName != null ? myClassName.getTextField() : null;
  }

  protected String getClassNameTextToSet(PsiClass aClass) {
    return aClass.getQualifiedName();
  }

  protected JTable getMethodsTable() {
    return myMethodsTable;
  }


  public void setCurrentClass(PsiClass aClass) {

    try {
      myDoUpdateFromSetClass = true;
      super.setCurrentClass(aClass);
      if (myClassName == null) return;
      setGuessedClassNameAndNamespace(aClass);

      if (aClass != null) {
        final Module moduleForFile = ProjectRootManager.getInstance(myProject).getFileIndex().getModuleForFile(
          aClass.getContainingFile().getVirtualFile()
        );
        if (moduleForFile != null && EnvironmentFacade.getInstance().isWebModule(moduleForFile)) {
          myTargetModule.setSelectedItem(moduleForFile);
        }
      }
    } finally {
      myDoUpdateFromSetClass = false;
    }
  }

  private void setGuessedClassNameAndNamespace(PsiClass aClass) {
    final String classNameText = myClassName.getTextField().getText();
    final int lastDotInClassName = classNameText.lastIndexOf('.');

    if (!myWSNamespaceChanged) {
      final String qualifiedName =
        aClass != null ? buildNSNameFromClass(aClass, currentEngine) :
          lastDotInClassName != -1 ?
            buildNSNameFromPackageText(classNameText.substring(0, lastDotInClassName), currentEngine):
            "somewhere";
      myWSNamespace.setText("http://" + qualifiedName);
    }

    if (!myWSNameChanged) {
      String wsName =
        aClass != null ?
          aClass.getName():
          lastDotInClassName + 1 < classNameText.length()?
            classNameText.substring(lastDotInClassName + 1):
            "*UNDEFINED*";

      if (aClass != null) {
        final PsiAnnotation annotation = AnnotationUtil.findAnnotation(aClass, JWSDPWSEngine.wsClassesSet);

        if (annotation != null) {
          final PsiAnnotationMemberValue annotationMemberValue = annotation.findAttributeValue("name");

          if (annotationMemberValue instanceof PsiLiteralExpression) {
            final String candidateName = StringUtil.stripQuotesAroundValue(annotationMemberValue.getText());
            if (candidateName.length() > 0) wsName = candidateName;
          }
        }
      }
      myWSName.setSelectedItem(wsName);
    }
  }

  public static String buildNSNameFromClass(PsiClass aClass, WSEngine engine) {
    String s = buildNSNameFromPackage(EnvironmentFacade.getInstance().getPackageFor(
      (EnvironmentFacade.getInstance().getDirectoryFromFile(aClass.getContainingFile()))));
    if (requiresEndingSlash(engine)) s += "/";
    return s;
  }

  public static String buildNSNameFromPackageText(String packageText, WSEngine engine) {
    StringBuffer result = new StringBuffer();
    int i = packageText.lastIndexOf('.');
    int prev = packageText.length();

    while(i != -1) {
      if (result.length() != 0) result.append('.');
      if (i + 1 != prev) {
        result.append(packageText.substring(i + 1, prev));
      }
      prev = i;
      i = packageText.lastIndexOf('.', prev - 1);
    }

    if (result.length() != 0) result.append('.');
    result.append(packageText.substring(0, prev));

    if (requiresEndingSlash(engine)) result.append("/");

    return result.toString();
  }

  private static boolean requiresEndingSlash(WSEngine engine) {
    return engine instanceof XFireWSEngine && ((XFireWSEngine)engine).isCxf();
  }

  private static String buildNSNameFromPackage(PsiPackage _package) {
    final PsiPackage parentPackage = _package.getParentPackage();
    if (parentPackage != null) {
      final String s = buildNSNameFromPackage(parentPackage);
      if (s != null) return _package.getName() + "." + s;
    }
    return _package.getName();
  }

  protected JLabel getStatusTextField() {
    return statusText;
  }

  protected JLabel getStatusField() {
    return status;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public JComboBox getWebServicePlatformCombo() {
    return webServicePlatform;
  }

  public JLabel getWebServicePlaformText() {
    return webServicePlatformText;
  }

  public void setupWSPlatformSpecificFields() {
    final String currentPlatform = (String)webServicePlatform.getSelectedItem();
    currentEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(currentPlatform);

    boolean setWsConfig = !(currentEngine instanceof JWSDPWSEngine);

    myWsNamespaceText.setVisible(setWsConfig);
    myWSNamespace.setVisible(setWsConfig);
    myWSName.setVisible(setWsConfig);
    myWSNameText.setVisible(setWsConfig);

    myWsUseOfItems.setVisible(setWsConfig);
    myWsUseOfItemsText.setVisible(setWsConfig);
    myWsServiceStyle.setVisible(setWsConfig);
    myWsServiceStyleText.setVisible(setWsConfig);

    if (!setWsConfig) { // wsname and namespace got from class
      myWSNameChanged = false;
      myWSNamespaceChanged = false;
    }
    setupAvailableServices();

    final boolean axisOrAxis2 = AxisWSEngine.AXIS_PLATFORM.equals(currentPlatform) ||
      Axis2WSEngine.AXIS2_PLATFORM.equals(currentPlatform);

    myMethodsPane.setVisible(false); //.setVisible(axisOrAxis2);
    pack();
  }

  public Module getSelectedModule() {
    return (Module) myTargetModule.getSelectedItem();
  }

  public JComboBox getModuleChooser() {
    return myTargetModule;
  }

  public WSEngine getCurrentEngine() {
    return currentEngine;
  }

  protected void dispose() {
    setGuessedClassNameAndNamespace(getCurrentClass()); // refresh cashes
    super.dispose();
  }

  protected void doOKAction() {
    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    instance.setLastPlatform(getWebServicePlatformCombo().getSelectedItem().toString());
    WebServicesPlugin.getInstance(myProject).setToAddRequiredLibraries(myAddRequiredLibraries.isSelected());
    super.doOKAction();
  }

  public String getWSName() {
    Object selectedItem = myWSName.getSelectedItem();
    return selectedItem != null ? selectedItem.toString():"";
  }

  public String getWSClassName() {
    return myClassName.getTextField().getText();
  }

  public String getWsNamespace() {
    return myWSNamespace.getText();
  }

  public String getUseOfItems() {
    return myWsUseOfItems.getSelectedItem().toString();
  }

  public String getBindingStyle() {
    return myWsServiceStyle.getSelectedItem().toString();
  }

  public boolean isToAddLibs() {
    return myAddRequiredLibraries.isSelected();
  }

  class MyValidationData extends GenerateFromJavaCodeDialogBase.MyValidationData {
    String wsNamespace;
    String wsName;
    String wsClassName;
    Module module;

    protected void doAcquire() {
      wsClassName = getWSClassName();
      wsName = myWSName.getEditor().getItem().toString();
      wsNamespace = getWsNamespace();
      module = (Module) myTargetModule.getSelectedItem();

      super.doAcquire();
    }
  }

  @NotNull
  protected String getHelpId() {
    return "DeployWebServices.html";
  }
}