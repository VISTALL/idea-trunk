package com.intellij.spring.references;

import com.intellij.lang.properties.PropertiesReferenceProvider;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.filters.*;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.xml.CustomBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomUtil;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class SpringReferenceContributor extends PsiReferenceContributor {
  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    PsiReferenceProvider propertiesReferenceProvider = new PropertiesReferenceProvider(false);

    //duplicated in FtlLiteralExpression
    XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{"code"}, new ScopeFilter(new ParentElementFilter(
      new AndFilter(new NamespaceFilter(XmlUtil.SPRING_URI),
                    new AndFilter(XmlTagFilter.INSTANCE, new TextFilter("message", "theme"))), 2)), propertiesReferenceProvider);

    registrar.registerReferenceProvider(PsiJavaPatterns.literalExpression().and(new FilterPattern(new ParentElementFilter(new PsiMethodCallFilter("org.springframework.beans.factory.BeanFactory", SpringBeanNamesReferenceProvider.METHODS), 2))), new SpringBeanNamesReferenceProvider());

    registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().with(new PatternCondition<XmlAttributeValue>("customBeanId") {
      public boolean accepts(@NotNull final XmlAttributeValue attributeValue, final ProcessingContext context) {
        final DomSpringBean element = DomUtil.findDomElement(attributeValue, DomSpringBean.class);
        if (element instanceof CustomBeanWrapper) {
          for (final CustomBean customBean : ((CustomBeanWrapper)element).getCustomBeans()) {
            if (customBean.getIdAttribute() == attributeValue.getParent()) {
              context.put("bean", customBean);
              return true;
            }
          }
        }
        return false;
      }
    }), new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        CustomBean bean = (CustomBean)context.get("bean");
        return new PsiReference[]{PsiReferenceBase.createSelfReference(element, bean.getIdentifyingPsiElement())};
      }
    });

    registrar.registerReferenceProvider(
        PsiJavaPatterns.literalExpression().annotationParam(SpringAnnotationsConstants.RESOURCE_ANNOTATION, "name"), new PsiReferenceProvider() {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
          final PsiMember member = PsiTreeUtil.getParentOfType(element, PsiMember.class);
          final PsiType type = PropertyUtil.getPropertyType(member);
          final PsiClass required = type instanceof PsiClassType ? ((PsiClassType)type).resolve() : null; 
          return new PsiReference[] { new SpringBeanReference((PsiLiteralExpression)element, required) {
              @Override
              public boolean isSoft() {
                return true;
              }
            }
          };
        }
      }, PsiReferenceRegistrar.HIGHER_PRIORITY);
  }
}
