package com.intellij.spring.model.jam.qualifiers;

import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.QualifierAttribute;
import com.intellij.spring.model.xml.SpringQualifier;
import com.intellij.util.xml.NameValue;
import com.intellij.jam.JamElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpringJamQualifier extends CommonModelElement.PsiBase implements JamElement, SpringQualifier {
  private PsiAnnotation myAnno;
  protected final PsiModifierListOwner myModifierListOwner;
  private CommonSpringBean myQualifiedBean;
  private Project myProject;

  public SpringJamQualifier(@NotNull PsiAnnotation anno, @Nullable PsiModifierListOwner modifierListOwner, @Nullable CommonSpringBean qualifiedBean) {
    myAnno = anno;
    myModifierListOwner = modifierListOwner;
    myQualifiedBean = qualifiedBean;

    myProject = anno.getProject();
  }

  public PsiAnnotation getAnnotation() {
    return myAnno;
  }

  @Nullable
  @NameValue
  public String getQualifiedName() {
    return AnnotationModelUtil.getObjectValue(myAnno.findDeclaredAttributeValue(null), String.class)   ;
  }

  @NotNull
  public PsiModifierListOwner getPsiElement() {
    return myModifierListOwner == null ? getType() : myModifierListOwner;
  }

  @NotNull
  public PsiClass getType() {
    final String annoQualifiedName = myAnno.getQualifiedName();

    return annoQualifiedName == null ? null : JavaPsiFacade.getInstance(myProject).findClass(annoQualifiedName, GlobalSearchScope.allScope(myProject));
  }

  public PsiClass getQualifierType() {
    return getType();
  }

  public String getQualifierValue() {
    return getQualifiedName();
  }

  @NotNull
  public List<? extends QualifierAttribute> getQualifierAttributes() {
    final PsiNameValuePair[] attributes = myAnno.getParameterList().getAttributes();
    final ArrayList<QualifierAttribute> list = new ArrayList<QualifierAttribute>();
    for (final PsiNameValuePair pair : attributes) {
      final String name = pair.getName();
      if (name == null || name.equals("value")) {
        continue;
      }
      list.add(new QualifierAttribute() {
        public String getAttributeKey() {
          return name;
        }

        public String getAttributeValue() {
          return AnnotationModelUtil.getObjectValue(pair.getValue(), String.class);
        }
      });
    }
    return list;
  }

  @Nullable
  public CommonSpringBean getQualifiedBean() {
    return myQualifiedBean;
  }
}
