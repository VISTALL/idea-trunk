package com.intellij.webBeans.gutter;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.jam.JamService;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.Function;
import com.intellij.util.Icons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.beans.AbstractWebBeanDescriptor;
import com.intellij.webBeans.beans.WebBeanDescriptor;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.jam.WebBeansJamModel;
import com.intellij.webBeans.jam.events.WebBeansFires;
import com.intellij.webBeans.jam.events.WebBeansObserves;
import com.intellij.webBeans.manager.WebBeansManager;
import com.intellij.webBeans.resources.WebBeansBundle;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

public class WebBeansEventsModelClassAnnotator implements Annotator {

  private static final PsiElementListCellRenderer listRenderer = new PsiElementListCellRenderer<PsiMember>() {
    public String getElementText(final PsiMember element) {
      return element.getName();
    }

    protected String getContainerText(final PsiMember element, final String name) {
      return null;
    }

    protected int getIconFlags() {
      return 0;
    }
  };

  private static final PsiElementListCellRenderer psiParametereListRenderer = new PsiElementListCellRenderer<PsiParameter>() {
    public String getElementText(final PsiParameter element) {
      PsiMethod method = (PsiMethod)element.getParent().getParent();
      StringBuffer sb = new StringBuffer();
      sb.append(method.getName());
      sb.append("(");
      List<String> params = new ArrayList<String>();
      for (PsiParameter psiParameter : method.getParameterList().getParameters()) {
        if (psiParameter.equals(element)) {
          params.add(element.getText());
        }
        else {
          params.add(psiParameter.getType().getPresentableText());
        }
      }
      sb.append(StringUtil.join(params, ","));
      sb.append(")");


      return sb.toString();
    }

    @Override
    protected Icon getIcon(PsiElement element) {
      return Icons.METHOD_ICON;
    }

    protected String getContainerText(final PsiParameter element, final String name) {
      return "(" + element.getContainingFile().getName() + ")";
    }

    protected int getIconFlags() {
      return 0;
    }
  };

  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof PsiIdentifier) {
      final PsiElement parent = psiElement.getParent();
      if (parent instanceof PsiClass) {
        Module module = ModuleUtil.findModuleForPsiElement(parent);
        if (module != null && WebBeansCommonUtils.isWebBeansFacetDefined(module)) {
          PsiClass psiClass = (PsiClass)parent;
          JamService service = JamService.getJamService(psiClass.getProject());

          List<WebBeansFires> fires = service.getAnnotatedMembersList(psiClass, WebBeansFires.SEM_KEY, true, true, true, true);

          if (fires.size() > 0) {
            final List<WebBeansObserves> observesList = WebBeansJamModel.getModel(module).getWebBeansObserves();
            for (WebBeansFires fire : fires) {
              annotateFires(fire, holder, module, observesList);
            }
          }

          List<PsiParameter> observesParameters = new ArrayList<PsiParameter>();
          for (PsiMethod psiMethod : psiClass.getMethods()) {
            for (PsiParameter psiParameter : psiMethod.getParameterList().getParameters()) {
              if (AnnotationUtil.isAnnotated(psiParameter, WebBeansAnnoConstants.OBSERVES_ANNOTATION, false)) {
                observesParameters.add(psiParameter);
              }
            }
          }
          if (observesParameters.size() > 0) {
            List<WebBeansFires> allFires = WebBeansJamModel.getModel(module).getWebBeansFires();
            for (PsiParameter psiParameter : observesParameters) {
              annotateObserves(psiParameter, holder, module, allFires);
            }
          }
        }
      }
    }
  }

  private void annotateObserves(PsiParameter psiParameter, AnnotationHolder holder, Module module, List<WebBeansFires> allFires) {
    final WebBeansFires[] fireses = collectFires(psiParameter, allFires, module);
    if (fireses.length > 0) {
      addGotoFiresIcon(holder, psiParameter, new NotNullLazyValue<Collection<? extends PsiElement>>() {
        @NotNull
        @Override
        protected Collection<PsiMember> compute() {
          return ContainerUtil.mapNotNull(fireses, new Function<WebBeansFires, PsiMember>() {
            public PsiMember fun(WebBeansFires fires) {
              return fires.getPsiElement();
            }
          });
        }
      });
    }
  }

  private static WebBeansFires[] collectFires(PsiParameter observesParameter, List<WebBeansFires> allFires, Module module) {
    Set<WebBeansFires> observes2fires = new HashSet<WebBeansFires>();
    final PsiType observesType = observesParameter.getType();
    Set<PsiClass> observesBindingTypes = WebBeansCommonUtils.getBindingTypesClasses(observesParameter, module);

    for (WebBeansFires fires : allFires) {
      PsiType firesType = fires.getEventType();
      if (firesType != null && firesType.isAssignableFrom(observesType)) {
        if (isAssignableBindingTypes(fires.getBindingTypes(), observesBindingTypes)) {
          observes2fires.add(fires);
        }
      }
    }

    return observes2fires.toArray(new WebBeansFires[observes2fires.size()]);
  }

  private static void annotateFires(WebBeansFires fires, AnnotationHolder holder, Module module, List<WebBeansObserves> allObservesList) {
    final WebBeansObserves[] observes = collectObserves(fires, allObservesList);
    if (observes.length > 0) {
      addGotoObservesIcon(holder, fires.getPsiElement(), new NotNullLazyValue<Collection<? extends PsiElement>>() {
        @NotNull
        @Override
        protected Collection<PsiParameter> compute() {
          return ContainerUtil.mapNotNull(observes, new Function<WebBeansObserves, PsiParameter>() {
            public PsiParameter fun(WebBeansObserves webBeansObserves) {
              return webBeansObserves.getPsiElement();
            }
          });
        }
      });
    }
  }

  private static void addGotoObservesIcon(final AnnotationHolder holder,
                                          final PsiElement psiIdentifier,
                                          final NotNullLazyValue<Collection<? extends PsiElement>> targets) {

    NavigationGutterIconBuilder.create(WebBeansIcons.GOTO_OBSERVES).
      setCellRenderer(psiParametereListRenderer).
      setTargets(targets).
      setPopupTitle(WebBeansBundle.message("gutter.choose.goto.observes")).
      setTooltipText(WebBeansBundle.message("gutter.navigate.to.observes")).
      install(holder, psiIdentifier);
  }

  private static void addGotoFiresIcon(AnnotationHolder holder,
                                       PsiParameter psiParameter,
                                       NotNullLazyValue<Collection<? extends PsiElement>> targets) {
    NavigationGutterIconBuilder.create(WebBeansIcons.GOTO_FIRES).
      setTargets(targets).
      setPopupTitle(WebBeansBundle.message("gutter.choose.goto.fires")).
      setTooltipText(WebBeansBundle.message("gutter.navigate.to.fires")).
      install(holder, psiParameter);
  }


  private static WebBeansObserves[] collectObserves(final WebBeansFires fires, final List<WebBeansObserves> allObservesList) {
    List<WebBeansObserves> fire2observes = new ArrayList<WebBeansObserves>();
    final PsiType firesType = fires.getEventType();
    final Set<PsiClass> firesBindingTypes = fires.getBindingTypes();

    if (firesType != null) {
      for (WebBeansObserves observes : allObservesList) {
        PsiType type = observes.getType();
        if (type != null && firesType.isAssignableFrom(type)) {
          if (isAssignableBindingTypes(firesBindingTypes, observes.getBindingTypes())) {
            fire2observes.add(observes);
          }
        }
      }
    }
    return fire2observes.toArray(new WebBeansObserves[fire2observes.size()]);
  }

  private static boolean isAssignableBindingTypes(Set<PsiClass> firstBindingTypes, Set<PsiClass> secondBindingTypes) {
    if (secondBindingTypes.size() == 0) return true;
    if (firstBindingTypes.size() != secondBindingTypes.size()) return false;
    for (PsiClass firstBindingType : firstBindingTypes) {
      boolean assignable = false;
      for (PsiClass secondBindingType : secondBindingTypes) {
        if (InheritanceUtil.isInheritorOrSelf(secondBindingType, firstBindingType, true)) {
          assignable = true;
          break;
        }
      }
      if (!assignable) return false;
    }

    return true;
  }


  private static Set<WebBeanDescriptor> getInjectableBeans(PsiModifierListOwner psiMember, Module module, PsiType type) {
    //todo primitive types
    if (type instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)type).resolve();
      Set<PsiClass> bindingTypes = WebBeansCommonUtils.getBindingTypesClasses(psiMember, module);

      return WebBeansManager.getService(module).resolveWebBeanByType(psiClass, bindingTypes.toArray(new PsiClass[bindingTypes.size()]));

    }
    return Collections.emptySet();
  }

  private static void addInjectableWebBeansGutterIcon(AnnotationHolder holder,
                                                      final Set<WebBeanDescriptor> beanDescriptors,
                                                      PsiIdentifier psiIdentifier) {
    if (beanDescriptors != null && beanDescriptors.size() > 0) {
      addInjectedWebBeanGutterIcon(holder, psiIdentifier, new NotNullLazyValue<Collection<? extends PsiElement>>() {
        @NotNull
        @Override
        protected Collection<PsiMember> compute() {
          return ContainerUtil.mapNotNull(beanDescriptors, new Function<WebBeanDescriptor, PsiMember>() {
            public PsiMember fun(WebBeanDescriptor webBeanDescriptor) {
              if (webBeanDescriptor instanceof AbstractWebBeanDescriptor) {
                return ((AbstractWebBeanDescriptor)webBeanDescriptor).getAnnotatedItem();
              }
              return null;
            }
          });
        }
      });
    }
  }

  private static void annotateMethod(final PsiMethod method, final AnnotationHolder holder, Module module) {
    Set<WebBeanDescriptor> descriptors = new HashSet<WebBeanDescriptor>();
    for (PsiParameter psiParameter : method.getParameterList().getParameters()) {
      descriptors.addAll(getInjectableBeans(psiParameter, module, psiParameter.getType()));
    }
    addInjectableWebBeansGutterIcon(holder, descriptors, method.getNameIdentifier());
  }

  private static void addInjectedWebBeanGutterIcon(final AnnotationHolder holder,
                                                   final PsiIdentifier psiIdentifier,
                                                   final NotNullLazyValue<Collection<? extends PsiElement>> targets) {

    NavigationGutterIconBuilder.create(WebBeansIcons.WEB_BEAN).
      setTargets(targets).
      setPopupTitle(WebBeansBundle.message("gutter.choose.web.beans")).
      setTooltipText(WebBeansBundle.message("gutter.navigate.to.web.beans")).
      install(holder, psiIdentifier);
  }
}