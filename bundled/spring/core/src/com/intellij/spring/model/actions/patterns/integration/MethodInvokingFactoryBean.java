package com.intellij.spring.model.actions.patterns.integration;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.MacroFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.actions.generate.DomTemplateRunner;
import com.intellij.util.xml.impl.DomTemplateRunnerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public abstract class MethodInvokingFactoryBean extends SpringBeanGenerateProvider {
  public MethodInvokingFactoryBean(String text) {
    super(text, null);
  }

    protected void runTemplate(final Editor editor, final PsiFile file, final SpringBean springBean) {
    super.runTemplate(editor, file, springBean);
    ((DomTemplateRunnerImpl)DomTemplateRunner.getInstance(file.getProject())).runTemplate(springBean,editor, getTemplate(springBean));
  }

  protected Template getTemplate(final SpringBean springBean) {
    final TemplateManager manager = TemplateManager.getInstance(springBean.getManager().getProject());
    final Template template = manager.createTemplate("", "");
    template.setToReformat(true);

    Expression completeExpression = new MacroCallNode(MacroFactory.createMacro("complete"));
    Expression targetMethodExpression = getTargetMethodExpression(springBean);

    //<bean id="$BEAN_NAME$" class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean">
    //  <property name="targetObject" ref="$TARGET_OBJECT$" />
    //  <property name="targetMethod" value="$TARGET_METHOD$" />
    //</bean>

    template.addTextSegment("<bean id=\"");
    template.addVariable("BEAN_NAME", completeExpression, completeExpression, true);

    template.addTextSegment("\" class=\""+getClassName()+"\">");
    template.addTextSegment("<property name=\"targetObject\" ref=\"");
    template.addVariable("TARGET_OBJECT", completeExpression, completeExpression, true);
    template.addTextSegment("\" />");
    template.addTextSegment("<property name=\"targetMethod\" value=\"");
    template.addVariable("TARGET_METHOD", targetMethodExpression, targetMethodExpression, true);
    template.addTextSegment("\" /></bean>");


    return template;
  }

  abstract protected String getClassName();

  private static Expression getTargetMethodExpression(final SpringBean springBean) {
    final SpringBean copy = springBean.createStableCopy();
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        return new TextResult("");
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        final PsiClass psiClass = getTargetObjectPsiClass(copy);
        if(psiClass == null) return LookupItem.EMPTY_ARRAY;

        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();
        for (PsiMethod psiMethod : psiClass.getAllMethods()) {
           if (psiMethod.hasModifierProperty(PsiModifier.PUBLIC) && psiMethod.getParameterList().getParametersCount() == 0) {
             items.add(LookupItemUtil.objectToLookupItem(psiMethod));
           }
        }

        return items.toArray(new LookupElement[items.size()]);
      }
    };
  }

  @Nullable
  private static PsiClass getTargetObjectPsiClass(final SpringBean springBean) {
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(springBean, "targetObject");
    if (property != null) {
      final GenericDomValue<SpringBeanPointer> element = property.getRefElement();
      if (element != null) {
        final SpringBeanPointer beanPointer = element.getValue();
        if(beanPointer != null) {
          return beanPointer.getBeanClass();
        }
      }
    }
    return null;
  }
}
