package org.jetbrains.android;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Function;
import com.intellij.util.PsiNavigateUtil;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.resourceManagers.LocalResourceManager;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author coyote
 */
public class AndroidResourcesLineMarkerProvider implements LineMarkerProvider {
  private static final Icon ICON = IconLoader.getIcon("/icons/navigate.png");

  public LineMarkerInfo getLineMarkerInfo(PsiElement psiElement) {
    return null;
  }

  public void collectSlowLineMarkers(List<PsiElement> psiElements, Collection<LineMarkerInfo> lineMarkerInfos) {
    for (PsiElement element : psiElements) {
      addMarkerInfo(element, lineMarkerInfos);
    }
  }

  @NotNull
  private static PsiFile getFileTarget(@NotNull PsiElement target) {
    return target instanceof PsiFile ? (PsiFile)target : target.getContainingFile();
  }

  @NotNull
  private static String getToolTip(@NotNull PsiElement element) {
    String s = "Go to ";
    if (element instanceof PsiField) {
      PsiField field = (PsiField)element;
      PsiClass resClass = field.getContainingClass();
      assert resClass != null;
      PsiClass rClass = resClass.getContainingClass();
      assert rClass != null;
      return s + rClass.getName() + '.' + resClass.getName() + '.' + field.getName();
    }
    else {
      PsiFile file = getFileTarget(element).getOriginalFile();
      String name = file.getName();
      PsiDirectory dir = file.getContainingDirectory();
      if (dir == null) return s + name;
      return s + dir.getName() + '/' + name;
    }
  }

  private static LineMarkerInfo createLineMarkerInfo(@NotNull PsiElement element, @NotNull PsiElement... targets) {
    final String toolTip = targets.length == 1 ? getToolTip(targets[0]) : "Go to resource";
    Function<PsiElement, String> function = new Function<PsiElement, String>() {
      public String fun(PsiElement psiElement) {
        return toolTip;
      }
    };
    DefaultPsiElementCellRenderer renderer = new DefaultPsiElementCellRenderer() {
      @Override
      public String getElementText(PsiElement element) {
        return getFileTarget(element).getName();
      }

      @Override
      public String getContainerText(PsiElement element, String name) {
        PsiDirectory dir = getFileTarget(element).getContainingDirectory();
        return dir == null ? "" : '(' + dir.getName() + ')';
      }
    };
    return new LineMarkerInfo<PsiElement>(element, element.getTextOffset(), ICON, Pass.UPDATE_OVERRIDEN_MARKERS, function,
                                          new MyNavigationHandler(targets, renderer));
  }

  private static void annotateXmlAttributeValue(@NotNull XmlAttributeValue attrValue, @NotNull Collection<LineMarkerInfo> result) {
    AndroidFacet facet = AndroidFacet.getInstance(attrValue);
    if (facet != null) {
      PsiElement parent = attrValue.getParent();
      if (!(parent instanceof XmlAttribute)) return;
      XmlAttribute attr = (XmlAttribute)parent;
      String name = attr.getLocalName();
      if (name.equals("name")) {
        XmlTag tag = PsiTreeUtil.getParentOfType(attr, XmlTag.class);
        if (tag != null) {
          PsiField field = AndroidResourceUtil.findResourceFieldForValueResource(tag);
          if (field != null) result.add(createLineMarkerInfo(tag, field));
        }
      }
      else {
        PsiField field = AndroidResourceUtil.findIdField(attr);
        if (field != null) {
          result.add(createLineMarkerInfo(attrValue, field));
        }
      }
    }
  }

  private static void addMarkerInfo(@NotNull final PsiElement element, @NotNull Collection<LineMarkerInfo> result) {
    PsiFile containingFile = element.getContainingFile();
    if (element instanceof PsiFile) {
      PsiField field = AndroidResourceUtil.findResourceFieldForFileResource((PsiFile)element);
      if (field != null) result.add(createLineMarkerInfo(element, field));
    }
    else if (containingFile != null) {
      if (element instanceof PsiClass) {
        PsiClass c = (PsiClass)element;
        if (AndroidUtils.R_CLASS_NAME.equals(c.getName())) {
          AndroidFacet facet = AndroidFacet.getInstance(element);
          if (facet != null && AndroidUtils.isRClassFile(facet, containingFile)) {
            LocalResourceManager manager = facet.getLocalResourceManager();
            annotateRClass((PsiClass)element, result, manager);
          }
        }
      }
      else if (element instanceof XmlAttributeValue) {
        annotateXmlAttributeValue((XmlAttributeValue)element, result);
      }
    }
  }

  private static void annotateRClass(@NotNull PsiClass rClass,
                                     @NotNull Collection<LineMarkerInfo> result,
                                     @NotNull LocalResourceManager manager) {
    Map<String, List<XmlAttributeValue>> idMap = manager.createIdMap();
    for (PsiClass c : rClass.getInnerClasses()) {
      for (PsiField field : c.getFields()) {
        List<PsiElement> targets = AndroidResourceUtil.findResourcesByField(manager, field, idMap);
        if (targets.size() > 0) {
          result.add(createLineMarkerInfo(field, targets.toArray(new PsiElement[targets.size()])));
        }
      }
    }
  }

  private static class MyNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
    private final PsiElement[] myTargets;
    private final PsiElementListCellRenderer myRenderer;

    private MyNavigationHandler(@NotNull PsiElement[] targets, @NotNull PsiElementListCellRenderer renderer) {
      myTargets = targets;
      myRenderer = renderer;
    }

    public void navigate(MouseEvent event, PsiElement psiElement) {
      if (myTargets.length == 0) {
        final JLabel renderer = HintUtil.createErrorLabel("Empty text");
        final JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(renderer, renderer).createPopup();
        if (event != null) {
          popup.show(new RelativePoint(event));
        }
        return;
      }
      if (myTargets.length == 1) {
        PsiNavigateUtil.navigate(myTargets[0]);
      }
      else {
        final JBPopup popup = NavigationUtil.getPsiElementPopup(myTargets, myRenderer, null);
        if (event != null) {
          popup.show(new RelativePoint(event));
        }
      }
    }
  }
}
