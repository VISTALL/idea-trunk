package com.intellij.spring.model.actions.generate;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.actions.generate.AbstractDomGenerateProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class SpringPropertiesGenerateProvider extends AbstractDomGenerateProvider<SpringProperty> {
  public SpringPropertiesGenerateProvider() {
    super(SpringBundle.message("spring.generate.properties"), SpringProperty.class);
  }

  protected DomElement getParentDomElement(final Project project, final Editor editor, final PsiFile file) {
    return SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
  }

  public SpringProperty generate(@Nullable final DomElement parent, final Editor editor) {
    if (parent instanceof SpringBean) {
      final SpringBean springBean = (SpringBean)parent;

      Collection<PsiMethod> setters = getNonInjectedPropertySetters(springBean);

      PsiMethodMember[] psiMethodMembers = getPsiMethodMembers(setters);

      final Project project = parent.getManager().getProject();
      MemberChooser<PsiMethodMember> chooser = new MemberChooser<PsiMethodMember>(psiMethodMembers, false, true, project);
      chooser.setTitle(SpringBundle.message("spring.bean.properties.chooser.title"));
      chooser.setCopyJavadocVisible(false);
      chooser.show();

      if (chooser.getExitCode() == MemberChooser.OK_EXIT_CODE) {
        final PsiMethodMember[] members = chooser.getSelectedElements(new PsiMethodMember[0]);
        if (members != null && members.length > 0) {

          final PsiMethod[] methods = new PsiMethod[members.length];
          for (int i = 0; i < members.length; i++) {
            methods[i] = members[i].getElement();
          }
          doGenerate(editor, springBean, project, methods);

        }
      }
    }
    return null;
  }

  public static void doGenerate(final Editor editor, final SpringBean springBean, final Project project, final PsiMethod... methods) {
    final SpringTemplateBuilder builder = new SpringTemplateBuilder(project);
    final SpringModel model = SpringUtils.getSpringModel(springBean);
    for (PsiMethod method : methods) {
      createProperty(method, model, builder);
    }
    SpringTemplateBuilder.preparePlace(editor, project, springBean.addProperty());
    builder.startTemplate(editor);
  }

  private static PsiMethodMember[] getPsiMethodMembers(final Collection<PsiMethod> setters) {
    List<PsiMethodMember> psiMethodMembers = new ArrayList<PsiMethodMember>();
    for (final PsiMethod psiMethod : setters) {
      psiMethodMembers.add(new PsiMethodMember(psiMethod) {
        public void renderTreeNode(final SimpleColoredComponent component, final JTree tree) {
          component.append(PropertyUtil.getPropertyNameBySetter(psiMethod), getTextAttributes(tree));
          component.append(": ", getTextAttributes(tree));
          component
            .append(psiMethod.getParameterList().getParameters()[0].getType().getCanonicalText(), SimpleTextAttributes.GRAYED_ATTRIBUTES);

          component.setIcon(SpringIcons.SPRING_BEAN_PROPERTY_ICON);
        }

        public String getText() {
          return PropertyUtil.getPropertyNameBySetter(psiMethod);
        }
      });
    }
    return psiMethodMembers.toArray(new PsiMethodMember[psiMethodMembers.size()]);
  }

  public static Collection<PsiMethod> getNonInjectedPropertySetters(final SpringBean springBean) {
    Map<MethodSignature, PsiMethod> map = new LinkedHashMap<MethodSignature, PsiMethod>();

    PsiClass psiClass = springBean.getBeanClass();
    if (psiClass != null) {
      for (PsiMethod method : psiClass.getAllMethods()) {
        if (PropertyUtil.isSimplePropertySetter(method) && method.hasModifierProperty(PsiModifier.PUBLIC) &&
            !method.hasModifierProperty(PsiModifier.STATIC) &&
            SpringUtils.findPropertyByName(springBean, PropertyUtil.getPropertyNameBySetter(method)) == null) {
          final MethodSignature key = method.getSignature(PsiSubstitutor.UNKNOWN);

          if (!map.containsKey(key)) map.put(key, method);
        }
      }
    }

    return map.values();
  }

  private static void createProperty(@NotNull final PsiMethod method, final SpringModel model, final SpringTemplateBuilder builder) {
    final PsiType type = method.getParameterList().getParameters()[0].getType();
    @NonNls final String name = PropertyUtil.getPropertyName(method);
    builder.addTextSegment("<property name=\"" + name + "\"");
    builder.createValueAndClose(type, model, "property");
  }

  public boolean isAvailableForElement(@NotNull final DomElement contextElement) {
    final SpringBean springBean = DomUtil.getParentOfType(contextElement, SpringBean.class, false);
    return springBean != null && getNonInjectedPropertySetters(springBean).size() > 0;
  }

  protected void doNavigate(final DomElementNavigationProvider navigateProvider, final DomElement element) {
  }
}
