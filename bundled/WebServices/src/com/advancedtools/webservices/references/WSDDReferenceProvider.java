package com.advancedtools.webservices.references;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.DeployUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class WSDDReferenceProvider extends MyReferenceProvider {
  private static final @NonNls String VALUE_ATTR_NAME = "value";
  public static final @NonNls String WSDD_NAMESPACE = "http://xml.apache.org/axis/wsdd/";
  public static final @NonNls String WSDD_JAVA_PROVIDER_NAMESPACE = "http://xml.apache.org/axis/wsdd/providers/java";
  public static final @NonNls String NAME_ATTR_NAME = "name";
  public static final @NonNls String PARAMETER_TAG_NAME = "parameter";
  private static final @NonNls String NAME_ATTR_VALUE = "allowedMethods";
  public static final @NonNls String NAME_ATTR_VALUE2 = "className";
  public static final @NonNls String BEANMAPPING_TAG_NAME = "beanMapping";

  private final MyReferenceProvider myClassProvider;

  public WSDDReferenceProvider(Project project) {
    myClassProvider = EnvironmentFacade.getInstance().acquireClassReferenceProvider(project);
  }

  public String[] getAttributeNames() { return new String[] { VALUE_ATTR_NAME}; }

  public ElementFilter getFilter() {
    return null;
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
    final XmlAttribute attr = (XmlAttribute) psiElement.getParent();
    final XmlTag tag = attr.getParent();

    if (VALUE_ATTR_NAME.equals(attr.getName())) {
      if (PARAMETER_TAG_NAME.equals(tag.getLocalName()) &&
          WSDD_NAMESPACE.equals(tag.getNamespace())
         ) {
        String name = tag.getAttributeValue(NAME_ATTR_NAME);
        if (NAME_ATTR_VALUE.equals(name)) {
          final String s = StringUtil.stripQuotesAroundValue(psiElement.getText());
          if (s.length() > 0) {
            List<PsiReference> refs = new ArrayList<PsiReference>(1);
            int lastOffset = 1;
            int ind = s.indexOf(' ');

            while(ind >= 0) {
              refs.add(new WSMethodReference(psiElement, new TextRange(lastOffset,ind + 1 )));
              lastOffset = ind + 2;
              ind = s.indexOf(' ', ind + 1);
            }
            refs.add(new WSMethodReference(psiElement, new TextRange(lastOffset,s.length() + 1)));

            return refs.toArray(PsiReference.EMPTY_ARRAY);
          }
        } else if (NAME_ATTR_VALUE2.equals(name)) {
          return myClassProvider.getReferencesByElement(psiElement);
        }
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }

  public static class WSMethodReference implements PsiReference, MemberReferenceThatKnowsAboutParentClassName {
    private final PsiElement myElement;
    private final TextRange myRange;
    private String myClassName;

    public WSMethodReference(PsiElement element, TextRange range) {
      myElement = element;
      myRange = range;
    }

    @Nullable
    public PsiElement resolve() {
      final String text = getCanonicalText();

      final PsiElement[] result = new PsiElement[1];
      processMethods(
        new PsiElementProcessor<PsiMethod>() {
          public boolean execute(PsiMethod element) {
            if (element.getName().equals(text)) {
              result[0] = element;
              return false;
            }
            return true;
          }
        }
      );

      if ("*".equals(text)) return myElement; // * match all accessible
      return result[0];
    }

    private void processMethods(PsiElementProcessor<PsiMethod> processor) {
      final PsiElement parent = myElement.getParent().getParent().getParent();

      if (parent instanceof XmlTag) {
        XmlTag parentTag = (XmlTag) parent;
        final XmlTag[] subTags = parentTag.findSubTags("parameter");

        for(XmlTag p:subTags) {
          PsiReference clazzRef = findReference(p);
          final PsiElement psiElement = clazzRef != null ? clazzRef.resolve() : null;

          if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            myClassName = psiClass.getQualifiedName();
            
            for(PsiMethod m:psiClass.getMethods()) {
              if (!DeployUtils.isAcceptableMethod(m)) continue;
              if (!processor.execute(m)) {
                return;
              }
            }
          } 
        }
      }
    }

    protected PsiReference findReference(XmlTag p) {
      if (NAME_ATTR_VALUE2.equals(p.getAttributeValue(NAME_ATTR_NAME))) {
        final String value = p.getAttributeValue(VALUE_ATTR_NAME);

        if (value != null) {
          final PsiReference[] references = p.getAttribute(VALUE_ATTR_NAME, null).getValueElement().getReferences();

          if (references.length > 0) {
            return references[references.length - 1];
          }
        }
      }

      return null;
    }

    public Object[] getVariants() {
      final List<String> results = new ArrayList<String>(1);
      processMethods(new PsiElementProcessor<PsiMethod>() {
        public boolean execute(PsiMethod element) {
          results.add(element.getName());
          return true;
        }
      });
      return results.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
    }

    public boolean isSoft() {
      return false;
    }

    public PsiElement getElement() {
      return myElement;
    }

    public TextRange getRangeInElement() {
      return myRange;
    }

    public String getCanonicalText() {
      String text = myElement.getText();
      return myRange.substring(text);
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      return EnvironmentFacade.getInstance().handleContentChange(
        myElement,
        myRange,
        newElementName
      );
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    public boolean isReferenceTo(PsiElement element) {
      return myElement.getManager().areElementsEquivalent(element, resolve());
    }

    public String getParentClassName() {      
      return myClassName;
    }
  }
}
