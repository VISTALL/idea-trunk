package org.jetbrains.android;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameJavaVariableProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.history.LocalHistory;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.dom.wrappers.ValueResourceElementWrapper;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.resourceManagers.LocalResourceManager;
import org.jetbrains.android.resourceManagers.ResourceManager;
import org.jetbrains.android.util.AndroidBundle;
import static org.jetbrains.android.util.AndroidBundle.message;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene.Kudelevsky
 */
public class AndroidRenameResourceProcessor extends RenamePsiElementProcessor {
  // for tests
  public static volatile boolean ASK = true;

  public boolean canProcessElement(PsiElement element) {
    if (element instanceof PsiFile) {
      return AndroidFacet.getInstance(element) != null && ResourceManager.isInResourceSubdirectory((PsiFile)element, null);
    }
    else if (element instanceof PsiField) {
      PsiField field = (PsiField)element;
      if (AndroidResourceUtil.isResourceField(field)) {
        return AndroidResourceUtil.findResourcesByField(field).size() > 0;
      }
    }
    else if (element instanceof XmlAttributeValue) {
      LocalResourceManager manager = LocalResourceManager.getInstance(element);
      if (manager != null) {
        if (AndroidResourceUtil.isIdDeclaration((XmlAttributeValue)element)) {
          return true;
        }
        // then it is value resource
        XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
        return tag != null && manager.getValueResourceType(tag) != null;
      }
    }
    return false;
  }

  @Override
  public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames) {
    AndroidFacet facet = AndroidFacet.getInstance(element);
    assert facet != null;
    if (element instanceof PsiFile) {
      prepareResourceFileRenaming((PsiFile)element, newName, allRenames, facet);
    }
    else if (element instanceof XmlAttributeValue) {
      XmlAttributeValue value = (XmlAttributeValue)element;
      if (AndroidResourceUtil.isIdDeclaration(value)) {
        prepareIdRenaming(value, newName, allRenames, facet);
      }
      else {
        prepareValueResourceRenaming(element, newName, allRenames, facet);
      }
    }
    else if (element instanceof PsiField) {
      prepareResourceFieldRenaming((PsiField)element, newName, allRenames);
    }
  }

  private static void prepareIdRenaming(XmlAttributeValue value, String newName, Map<PsiElement, String> allRenames, AndroidFacet facet) {
    LocalResourceManager manager = facet.getLocalResourceManager();
    allRenames.remove(value);
    String id = AndroidResourceUtil.getResourceNameByReferenceText(value.getValue());
    assert id != null;
    List<XmlAttributeValue> idDeclarations = manager.findIdDeclarations(id);
    if (idDeclarations != null) {
      for (XmlAttributeValue idDeclaration : idDeclarations) {
        allRenames.put(new ValueResourceElementWrapper(idDeclaration), newName);
      }
    }
    PsiField resField = AndroidResourceUtil.findIdField(value);
    if (resField != null) {
      allRenames.put(resField, AndroidResourceUtil.getResourceNameByReferenceText(newName));
    }
  }

  @Nullable
  private static String getResourceName(Project project, String newFieldName, String oldResourceName) {
    if (newFieldName.indexOf('_') < 0) return newFieldName;
    if (oldResourceName.indexOf('_') < 0 && oldResourceName.indexOf('.') >= 0) {
      String suggestion = newFieldName.replace('_', '.');
      newFieldName = Messages.showInputDialog(project, AndroidBundle.message("rename.resource.dialog.text", oldResourceName),
                                              RefactoringBundle.message("rename.title"), Messages.getQuestionIcon(), suggestion, null);
    }
    return newFieldName;
  }

  private static void prepareResourceFieldRenaming(PsiField field, String newName, Map<PsiElement, String> allRenames) {
    new RenameJavaVariableProcessor().prepareRenaming(field, newName, allRenames);
    List<PsiElement> resources = AndroidResourceUtil.findResourcesByField(field);
    int r = 0;
    if (ASK) {
      r = Messages.showYesNoDialog(field.getProject(), message("rename.resource.question", field.getName()), message("rename.dialog.title"),
                                   Messages.getQuestionIcon());
    }
    if (r == 0) {
      PsiElement res = resources.get(0);
      String resName = res instanceof XmlAttributeValue ? ((XmlAttributeValue)res).getValue() : ((PsiFile)res).getName();
      String newResName = getResourceName(field.getProject(), newName, resName);
      for (PsiElement resource : resources) {
        if (resource instanceof PsiFile) {
          PsiFile file = (PsiFile)resource;
          String extension = FileUtil.getExtension(file.getName());
          allRenames.put(resource, newResName + '.' + extension);
        }
        else if (resource instanceof XmlAttributeValue) {
          XmlAttributeValue value = (XmlAttributeValue)resource;
          if (AndroidResourceUtil.isIdDeclaration(value)) {
            newResName = AndroidResourceUtil.NEW_ID_PREFIX + newResName;
          }
          allRenames.put(new ValueResourceElementWrapper(value), newResName);
        }
      }
    }
  }

  private static void prepareValueResourceRenaming(PsiElement element,
                                                   String newName,
                                                   Map<PsiElement, String> allRenames,
                                                   AndroidFacet facet) {
    ResourceManager manager = facet.getLocalResourceManager();
    XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
    assert tag != null;
    String type = manager.getValueResourceType(tag);
    assert type != null;
    Project project = tag.getProject();
    DomElement domElement = DomManager.getDomManager(project).getDomElement(tag);
    assert domElement instanceof ResourceElement;
    String name = ((ResourceElement)domElement).getName().getValue();
    assert name != null;
    List<ResourceElement> resources = manager.findValueResources(type, name);
    for (ResourceElement resource : resources) {
      XmlElement xmlElement = resource.getName().getXmlAttributeValue();
      if (!element.getManager().areElementsEquivalent(element, xmlElement)) {
        allRenames.put(xmlElement, newName);
      }
    }
    PsiField resField = AndroidResourceUtil.findResourceFieldForValueResource(tag);
    if (resField != null) {
      allRenames.put(resField, newName);
    }
  }

  private static void prepareResourceFileRenaming(PsiFile file, String newName, Map<PsiElement, String> allRenames, AndroidFacet facet) {
    Project project = file.getProject();
    ResourceManager manager = facet.getLocalResourceManager();
    String type = manager.getFileResourceType(file);
    if (type == null) return;
    String name = file.getName();
    List<PsiFile> resourceFiles = manager.findResourceFiles(type, FileUtil.getNameWithoutExtension(name));
    List<PsiFile> alternativeResources = new ArrayList<PsiFile>();
    for (PsiFile resourceFile : resourceFiles) {
      if (!resourceFile.getManager().areElementsEquivalent(file, resourceFile) && resourceFile.getName().equals(name)) {
        alternativeResources.add(resourceFile);
      }
    }
    if (alternativeResources.size() > 0) {
      int r = 0;
      if (ASK) {
        r = Messages.showYesNoDialog(project, message("rename.alternate.resources.question"), message("rename.dialog.title"),
                                     Messages.getQuestionIcon());
      }
      if (r == 0) {
        for (PsiFile candidate : alternativeResources) {
          allRenames.put(candidate, newName);
        }
      }
      else {
        return;
      }
    }
    PsiField resField = AndroidResourceUtil.findResourceFieldForFileResource(file);
    if (resField != null) {
      allRenames.put(resField, FileUtil.getNameWithoutExtension(newName));
    }
  }

  @Override
  public void renameElement(PsiElement element, final String newName, UsageInfo[] usages, RefactoringElementListener listener)
    throws IncorrectOperationException {
    if (element instanceof PsiField) {
      new RenameJavaVariableProcessor().renameElement(element, newName, usages, listener);
    }
    else {
      super.renameElement(element, newName, usages, listener);
      if (element instanceof PsiFile) {
        VirtualFile virtualFile = ((PsiFile)element).getVirtualFile();
        if (!LocalHistory.isUnderControl(element.getProject(), virtualFile)) {
          DocumentReference ref = DocumentReferenceManager.getInstance().create(virtualFile);
          UndoManager.getInstance(element.getProject()).nonundoableActionPerformed(ref, false);
        }
      }
    }
  }
}
