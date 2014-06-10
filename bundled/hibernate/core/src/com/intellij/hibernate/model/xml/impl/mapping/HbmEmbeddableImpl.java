package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.xml.mapping.HbmAttributeBase;
import com.intellij.hibernate.model.xml.mapping.HbmAttributeVisitorAdapter;
import com.intellij.hibernate.model.xml.mapping.HbmEmbeddedAttributeBase;
import com.intellij.hibernate.model.xml.mapping.HbmHibernateMapping;
import com.intellij.openapi.module.Module;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.PersistentEmbeddable;
import com.intellij.persistence.model.helpers.PersistentObjectModelHelper;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * @author Gregory.Shrago
*/
public class HbmEmbeddableImpl implements PersistentEmbeddable, PersistentObjectModelHelper {
  private final HbmHibernateMappingImpl myMapping;
  private final String myKey;

  public HbmEmbeddableImpl(final HbmHibernateMappingImpl mapping, final String key) {
    myMapping = mapping;
    myKey = key;
  }

  public GenericValue<PsiClass> getClazz() {
    for (HbmEmbeddedAttributeBase attribute : getDefiningAttributes()) {
      if (!attribute.isValid()) continue;
      final PsiClass psiClass = PersistenceCommonUtil.getTargetClass(attribute);
      if (psiClass != null) return ReadOnlyGenericValue.getInstance(psiClass);
    }
    return ReadOnlyGenericValue.getInstance(null);
  }

  @NotNull
  public List<HbmEmbeddedAttributeBase> getDefiningAttributes() {
    final List<HbmEmbeddedAttributeBase> attributes = myMapping.getEmbeddableAttributesByKey(myKey);
    return attributes == null? Collections.<HbmEmbeddedAttributeBase>emptyList() : attributes;
  }

  public PersistentObjectModelHelper getObjectModelHelper() {
    return this;
  }

  public boolean isValid() {
    if (!myMapping.isValid()) return false;
    for (HbmEmbeddedAttributeBase attribute : getDefiningAttributes()) {
      if (attribute.isValid()) return true;
    }
    return false;
  }

  public XmlTag getXmlTag() {
    return null;
  }

  public PsiManager getPsiManager() {
    return myMapping.getPsiManager();
  }

  public Module getModule() {
    return myMapping.getModule();
  }

  public PsiElement getIdentifyingPsiElement() {
    // todo[greg] uncomment when find usages will handle this case
    //for (HbmEmbeddedAttributeBase o : getDefiningAttributes()) {
    //  final PsiElement element = o.getIdentifyingPsiElement();
    //  if (element != null) return element;
    //}
    return null;
  }

  public PsiFile getContainingFile() {
    return myMapping.getContainingFile();
  }

  @NotNull
  public List<? extends PersistentAttribute> getAttributes() {
    final ArrayList<PersistentAttribute> result = new ArrayList<PersistentAttribute>();
    final Set<String> set = new THashSet<String>();
    final HbmAttributeDomElementVisitor visitor = new HbmAttributeDomElementVisitor(new HbmAttributeVisitorAdapter() {
      public void visitAttributeBase(final HbmAttributeBase o) {
        final String name = o.getName().getValue();
        if (set.add(name)) {
          result.add(o);
        }
      }
    });
    for (HbmEmbeddedAttributeBase attribute : getDefiningAttributes()) {
      if (!attribute.isValid()) continue;
      DomUtil.acceptAvailableChildren(attribute, visitor);
    }
    return result;
  }

  public PropertyMemberType getDefaultAccessMode() {
    final HbmHibernateMapping mapping = myMapping.getParentOfType(HbmHibernateMapping.class, false);
    return mapping != null ? mapping.getDefaultAccess().getValue() == AccessType.FIELD ? PropertyMemberType.FIELD : PropertyMemberType.GETTER : PropertyMemberType.GETTER;
  }

  public boolean isAccessModeFixed() {
    return false;
  }
}
