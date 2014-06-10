package com.advancedtools.webservices.references;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.filters.ClassFilter;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.OrFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class JAXBJavaReferenceProvider extends MyReferenceProvider {
  private static @NonNls final String NAME_ANNOTATION_METHOD_NAME = "name";
  private static @NonNls final String PROP_ORDER_ANNOTATION_METHOD_NAME = "propOrder";

  public ElementFilter getFilter() {
    return new ParentElementFilter(
      new OrFilter(
        new ElementFilter[] {
          new ClassFilter(PsiNameValuePair.class),
          new ClassFilter(PsiArrayInitializerMemberValue.class)
        }
      )
    );
  }

  static class ClassReference extends BaseRangedReference {
    public ClassReference(PsiLiteralExpression psiElement) {
      super(psiElement, 1, psiElement.getTextLength() - 1);
    }

    @Nullable
    public PsiElement resolve() {
      PsiClass clazz = PsiTreeUtil.getParentOfType(getElement(), PsiClass.class);
      if (clazz != null && StringUtil.capitalize(getCanonicalText()).equals(clazz.getName())) {
        return clazz;
      }
      return null;
    }

    public Object[] getVariants() {
      PsiClass clazz = PsiTreeUtil.getParentOfType(getElement(), PsiClass.class);
      return new Object[] { StringUtil.decapitalize(clazz.getName()) };
    }

    public boolean isSoft() {
      return true;
    }
  }

  static class MethodReference extends BaseRangedReference {
    private PsiSubstitutor mySubstitutor;

    public MethodReference(PsiLiteralExpression psiElement) {
      super(psiElement, 1, psiElement.getTextLength() - 1);
    }

    @Nullable
    public PsiElement resolve() {
      PsiClass clazz = findClass();
      if (clazz != null) {
        return PropertyUtil.findPropertyGetter(clazz, getCanonicalText(), false, true);
      }
      return null;
    }

    private PsiClass findClass() {
      final PsiAnnotation annotation = PsiTreeUtil.getParentOfType(getElement(), PsiAnnotation.class);
      final PsiAnnotationMemberValue memberValue = annotation.findAttributeValue(NAME_ANNOTATION_METHOD_NAME);

      if (memberValue instanceof PsiLiteralExpression) {
        final PsiReference[] references = memberValue.getReferences();
        if (references.length > 0) return (PsiClass) references[references.length - 1].resolve();
      }

      return null;
    }

    public Object[] getVariants() {
      PsiClass clazz = findClass();

      if (clazz != null) {
        final List<String> properties = new ArrayList<String>(3);

        EnvironmentFacade.getInstance().processProperties(clazz, new PsiElementProcessor<PsiMember>() {
          public boolean execute(PsiMember psiMember) {
            if (psiMember instanceof PsiMethod) {
              PsiMethod psiMethod = (PsiMethod) psiMember;

              final PsiType returnType = psiMethod.getReturnType();
              if (returnType instanceof PsiClassType &&
                  "Class".equals(((PsiClassType)returnType).getClassName())) {
                return true; // skip class
              }
              properties.add(PropertyUtil.getPropertyName(psiMethod));
            }
            return true;
          }
        });

        return ArrayUtil.toObjectArray(properties);
      }

      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    public boolean isSoft() {
      return true;
    }
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element) {
    if (!(element instanceof PsiLiteralExpression)) return PsiReference.EMPTY_ARRAY;
    PsiElement parent = element.getParent();

    if (parent instanceof PsiArrayInitializerMemberValue) {
      parent = parent.getParent();
    }

    final PsiNameValuePair pair = (PsiNameValuePair) parent;
    final String name = pair.getName();

    if (NAME_ANNOTATION_METHOD_NAME.equals(name) || PROP_ORDER_ANNOTATION_METHOD_NAME.equals(name)) {
      final PsiAnnotation annotation = PsiTreeUtil.getParentOfType(pair, PsiAnnotation.class);
      final PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();

      if (nameReferenceElement != null && "XmlType".equals(nameReferenceElement.getReferenceName()) &&
          "javax.xml.bind.annotation.XmlType".equals(nameReferenceElement.getQualifiedName())) {
        if (name.equals(NAME_ANNOTATION_METHOD_NAME)) {
          return new PsiReference[] { new ClassReference((PsiLiteralExpression) element) };
        } else {
          return new PsiReference[] { new MethodReference((PsiLiteralExpression) element) };
        }
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

}
