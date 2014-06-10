package com.intellij.spring.refactoring;

import com.intellij.lang.refactoring.InlineHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.impl.model.beans.SpringBeanImpl;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.reflect.DomCollectionChildDescription;
import com.intellij.util.xml.reflect.DomFixedChildDescription;
import com.intellij.util.xml.reflect.DomGenericInfo;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringInlineHandler implements InlineHandler {

  private final static Logger LOG = Logger.getInstance("#com.intellij.spring.refactoring.SpringInlineHandler");
  @NonNls private static final String PARENT_ATTR = "parent";

  public Settings prepareInlineElement(final PsiElement element, final Editor editor, final boolean invokedOnReference) {
    return new Settings() {
      public boolean isOnlyOneReferenceToInline() {
        return false;
      }
    };
  }

  public void removeDefinition(final PsiElement element) {
    final DomElement domElement;
    if (element instanceof XmlTag) {
      domElement = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag)element);
      if (domElement != null) {
        domElement.undefine();
      }
    }
  }

  public Inliner createInliner(final PsiElement element, Settings settings) {
    if (!(element instanceof XmlTag)) {
      return null;
    }
    return new Inliner() {
      public Map<PsiElement, String> getConflicts(final PsiReference reference, final PsiElement referenced) {
        return null;
      }

      public void inlineUsage(final UsageInfo usage, final PsiElement referenced) {        
        if (!(referenced instanceof XmlTag)) {
          return;
        }
        final Project project = referenced.getProject();
        final DomManager domManager = DomManager.getDomManager(project);
        final DomSpringBean bean = (DomSpringBean)domManager.getDomElement((XmlTag)referenced);

        PsiElement psiElement = usage.getElement();
        if (psiElement instanceof XmlAttributeValue) {
          final XmlAttribute attribute = (XmlAttribute)psiElement.getParent();
          final GenericAttributeValue value = domManager.getDomElement(attribute);
          assert value != null;
          final DomElement parent = value.getParent();
          assert parent != null;
          if (parent instanceof SpringBean) {
            final String attrName = attribute.getName();
            if (attrName.equals(PARENT_ATTR)) {
              SpringBean thisBean = (SpringBean)parent;
              mergeValue(thisBean, thisBean.getScope());
              mergeValue(thisBean, thisBean.getAbstract());
              mergeValue(thisBean, thisBean.getLazyInit());

              mergeValue(thisBean, thisBean.getAutowireCandidate());
              mergeValue(thisBean, thisBean.getAutowire());
              mergeValue(thisBean, thisBean.getDependencyCheck());
              mergeValue(thisBean, thisBean.getDependsOn());

              mergeValue(thisBean, thisBean.getFactoryBean());
              mergeValue(thisBean, thisBean.getFactoryMethod());
              mergeValue(thisBean, thisBean.getInitMethod());
              mergeValue(thisBean, thisBean.getDestroyMethod());

              mergeValue(thisBean, thisBean.getDescription());

              mergeList(thisBean, SpringBeanImpl.CTOR_ARGS_GETTER, new Function<SpringBean, ConstructorArg>() {
                public ConstructorArg fun(final SpringBean springBean) {
                  return springBean.addConstructorArg();
                }
              });
              mergeList(thisBean, SpringBeanImpl.PROPERTIES_GETTER, new Function<SpringBean, SpringPropertyDefinition>() {
                public SpringPropertyDefinition fun(final SpringBean springBean) {
                  return springBean.addProperty();
                }
              });
              mergeList(thisBean, new Function<SpringBean, Collection<ReplacedMethod>>() {
                public Collection<ReplacedMethod> fun(final SpringBean springBean) {
                  return springBean.getReplacedMethods();
                }
              }, new Function<SpringBean, ReplacedMethod>() {
                public ReplacedMethod fun(final SpringBean springBean) {
                  return springBean.addReplacedMethod();
                }
              });
              value.undefine();
              reformat(parent);
            }
          }
          else if (parent instanceof SpringElementsHolder) {
            
            copyBean(bean, parent);
            value.undefine();
            reformat(parent);
          }
          else {
            final DomElement grandParent = parent.getParent();
            if (grandParent instanceof SpringElementsHolder) {
              copyBean(bean, grandParent);
              parent.undefine();
              reformat(grandParent);
            } else if (grandParent instanceof CollectionElements) {
              copyBean(bean, grandParent);
              parent.undefine();
              reformat(grandParent);
            }
            else {
              LOG.error("Cannot inline " + attribute);
            }
          }
        }
      }
    };
  }

  private static <T extends GenericDomValue<?>> void mergeValue(SpringBean springBean, T value) {
    final T mergedValue = SpringUtils.getMergedValue(springBean, value);
    if (mergedValue != value) {
      value.setStringValue(mergedValue.getStringValue());
    }
  }

  public static <T extends DomElement> void mergeList(final SpringBean springBean,
                                                      final Function<SpringBean, Collection<T>> getter,
                                                      final Function<SpringBean, T> adder) {
    final Set<T> merged = SpringUtils.getMergedSet(springBean, getter);
    final Collection<T> existing = getter.fun(springBean);
    for (T t : existing) {
      if (!merged.contains(t)) {
        t.undefine();
      } else {
        merged.remove(t);
      }
    }
    for (T t : merged) {
      final T newElement = adder.fun(springBean);
      newElement.copyFrom(t);
    }
  }
  
  private static void reformat(final DomElement domElement) {
    try {
      CodeStyleManager.getInstance(domElement.getManager().getProject()).reformat(domElement.getXmlTag());
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  private static void copyBean(final DomSpringBean from, final DomElement parent) {
    final DomCollectionChildDescription description = (DomCollectionChildDescription)from.getChildDescription();
    final DomGenericInfo info = parent.getGenericInfo();
    final String name = description.getXmlElementName();
    final String namespaceKey = description.getXmlName().getNamespaceKey();
    final DomSpringBean to;
    DomCollectionChildDescription targetDescription = info.getCollectionChildDescription(name, namespaceKey);
    if (targetDescription != null) {
      to = (DomSpringBean)targetDescription.addValue(parent);
    } else {
      final DomFixedChildDescription fixedDescr = info.getFixedChildDescription(name, namespaceKey);
      assert fixedDescr != null;
      to = (DomSpringBean)fixedDescr.getValues(parent).get(0);
    }

    to.copyFrom(from);
    to.getId().undefine();
    if (to instanceof SpringBean) {
      ((SpringBean)to).getName().undefine();
    }
  }
}
