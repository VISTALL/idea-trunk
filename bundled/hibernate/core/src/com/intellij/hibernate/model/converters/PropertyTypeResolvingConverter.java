package com.intellij.hibernate.model.converters;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupValueWithPriority;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.enums.HibernateTypeType;
import com.intellij.hibernate.model.xml.impl.mapping.HbmHibernateMappingImpl;
import com.intellij.hibernate.model.xml.mapping.HbmAttributeBase;
import com.intellij.javaee.model.common.persistence.mapping.AttributeType;
import com.intellij.jpa.model.xml.impl.converters.ClassConverterBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.BidirectionalMultiMap;
import com.intellij.util.xml.*;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Gregory.Shrago
 */
public class PropertyTypeResolvingConverter extends ResolvingConverter<PsiType> implements CustomReferenceConverter {

  @Override
  public PsiElement getPsiElement(@Nullable PsiType resolvedValue) {
    return getPsiElementInner(resolvedValue);
  }

  @Nullable
  public static PsiElement getPsiElementInner(PsiType resolvedValue) {
    return resolvedValue instanceof PsiClassType ? ((PsiClassType)resolvedValue).resolve() : null;
  }

  @NotNull
  public Collection<? extends PsiType> getVariants(final ConvertContext context) {
    return Collections.emptyList();
  }

  public static LookupElement[] getVariantsInner(final PersistentAttribute attribute, final PsiFile file,
                                          final HbmHibernateMappingImpl mapping, final Module module) {
    final ArrayList<LookupElement> result = new ArrayList<LookupElement>();
    final PsiType existingType;
    if (attribute != null) {
      final PsiType type = attribute.getPsiType();
      if (AttributeType.getAttributeType(attribute).isContainer()) {
        existingType = PersistenceCommonUtil.getTargetEntityType(type);
      }
      else {
        existingType = type;
      }
    }
    else {
      existingType = null;
    }
    addStandardTypdefsAndUserTypes(existingType, result, mapping, file, module);
    return result.toArray(new LookupElement[result.size()]);
  }

  private static void addStandardTypdefsAndUserTypes(final PsiType existingType, final Collection<LookupElement> result,
                                                 final HbmHibernateMappingImpl mapping,
                                                 final PsiFile file, final Module module) {
    final PsiClass[] userTypeClasses = new PsiClass[]{DomJavaUtil.findClass(HibernateConstants.USER_TYPE_CLASS, file, module, null),
      DomJavaUtil.findClass(HibernateConstants.COMPOSITE_USER_TYPE_CLASS, file, module, null)};
    final GlobalSearchScope scope = module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) : file.getResolveScope();
    final THashMap<String, PsiClass> types = new THashMap<String, PsiClass>();
    for (PsiClass typeClass : userTypeClasses) {
      if (typeClass == null) continue;
      for (PsiClass psiClass : ClassInheritorsSearch.search(typeClass, scope, true)) {
        final String qname = psiClass.getQualifiedName();
        if (qname == null) continue;
        types.put(qname, psiClass);
      }
    }
    final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(file.getProject()).getElementFactory();
    for (HibernateTypeType type : HibernateTypeType.values()) {
      final String simple = type.getJavaTypeName();
      final String boxed = PsiTypesUtil.boxIfPossible(type.getJavaTypeName());
      try {
        final PsiType psiType = elementFactory.createTypeFromText(boxed, null);
        final PsiClass psiClass = psiType instanceof PsiClassType ? ((PsiClassType)psiType).resolve() : null;
        if (psiType != null && existingType != null && !psiType.isAssignableFrom(existingType)) continue;
        types.put(boxed, psiClass);
        if (boxed != simple) {
          types.put(simple, null);
        }
      }
      catch (IncorrectOperationException e) {
        // nope
      }
    }
    final List<BidirectionalMultiMap<String, String>> typeDefs = getTypeDefs(mapping);
    for (BidirectionalMultiMap<String, String> typeDef : typeDefs) {
      for (String className : typeDef.getKeys()) {
        if (!types.containsKey(className)) {
          try {
            final PsiType psiType = elementFactory.createTypeFromText(className, null);
            final PsiClass psiClass = psiType instanceof PsiClassType ? ((PsiClassType)psiType).resolve() : null;
            types.put(className, psiClass);
          }
          catch (IncorrectOperationException e) {
            // nope
          }
        }
        for (String typeDefName : typeDef.getValues(className)) {
          if (!types.containsKey(typeDefName)) {
            types.put(typeDefName, null);
          }
        }
      }
    }
    for (String name : types.keySet()) {
      final PsiClass psiClass = types.get(name);
      if (psiClass != null) {
        final String qname = psiClass.getQualifiedName();
        result.add(LookupElementBuilder.create(psiClass, qname).setPresentableText(psiClass.getName()).
          addLookupString(psiClass.getName()).setIcon(psiClass.getIcon(0)));
      }
      else {
        result.add(LookupElementBuilder.create(name).setBold());
      }
    }
    for (HibernateTypeType type : HibernateTypeType.values()) {
      if (types.contains(type.getJavaTypeName()) || types.contains(PsiTypesUtil.boxIfPossible(type.getJavaTypeName()))) {
        result.add(PrioritizedLookupElement.withGrouping(LookupElementBuilder.create(type.getValue()), LookupValueWithPriority.HIGH));
      }
    }
  }

  private static List<BidirectionalMultiMap<String, String>> getTypeDefs(HbmHibernateMappingImpl mapping) {
    if (mapping != null) {
      final ArrayList<BidirectionalMultiMap<String, String>> typedefs = new ArrayList<BidirectionalMultiMap<String, String>>();
      typedefs.add(mapping.getTypedefMap());
      for (PersistencePackage unit : PersistenceHelper.getHelper().getSharedModelBrowser().getPersistenceUnits(mapping)) {
        for (GenericValue<HbmHibernateMappingImpl> value : unit.getModelHelper().getMappingFiles(HbmHibernateMappingImpl.class)) {
          final HbmHibernateMappingImpl otherMapping = value.getValue();
          if (otherMapping != null) {
            typedefs.add(otherMapping.getTypedefMap());
          }
        }
      }
      return typedefs;
    }
    return Collections.emptyList();
  }

  public PsiType fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return fromStringInner(s, context.getFile().getProject(), MappingClassResolveConverter.getRoot(context));
  }

  public static PsiType fromStringInner(String s, final Project project, final HbmHibernateMappingImpl mapping) {
    if (s == null) return null;
    final HibernateTypeType basicType = NamedEnumUtil.getEnumElementByValue(HibernateTypeType.class, s);
    try {
      final String typeName;
      if (basicType == null) {
        final String typedefClassName = getFromTypeDefs(s, mapping);
        if (StringUtil.isNotEmpty(typedefClassName)) {
          typeName = typedefClassName;
        }
        else if (TypeConversionUtil.isPrimitive(s)) {
          typeName = s;
        }
        else {
          typeName = ClassConverterBase.getQualifiedClassName(s, MappingClassResolveConverter.getPackageName(mapping));
        }
      }
      else {
        typeName = basicType.getJavaTypeName();
      }
      assert typeName != null;
      final PsiType type = JavaPsiFacade.getInstance(project).getElementFactory().createTypeFromText(typeName, null);
      return type instanceof PsiClassType && ((PsiClassType)type).resolve() == null? null : type;
    }
    catch(IncorrectOperationException e){
      return null;
    }
  }

  @Nullable
  private static String getFromTypeDefs(final String s, final HbmHibernateMappingImpl mapping) {
    if (mapping == null) return null;
    Set<String> keys = mapping.getTypedefMap().getKeys(s);
    if (keys != null && !keys.isEmpty()) return keys.iterator().next();
    for (PersistencePackage unit : PersistenceHelper.getHelper().getSharedModelBrowser().getPersistenceUnits(mapping)) {
      for (GenericValue<HbmHibernateMappingImpl> value : unit.getModelHelper().getMappingFiles(HbmHibernateMappingImpl.class)) {
        final HbmHibernateMappingImpl otherMapping = value.getValue();
        if (otherMapping != null) {
          keys = otherMapping.getTypedefMap().getKeys(s);
          if (keys != null && !keys.isEmpty()) return keys.iterator().next();
        }
      }
    }
    return null;
  }

  public String toString(@Nullable PsiType psiType, final ConvertContext context) {
    return psiType == null? null : psiType.getCanonicalText();
  }

  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
    return ClassConverterBase.getCreateClassQuickFixes(context, MappingClassResolveConverter.getPackageName(context));
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    return new PsiReference[] {
      new PsiReferenceBase<PsiElement>(element, true) {

      public PsiElement resolve() {
        final PsiType type = fromString(genericDomValue.getStringValue(), context);
        if (type instanceof PsiClassType) {
          return ((PsiClassType)type).resolve();
        }
        else if (type != null) {
          return DomUtil.getValueElement(genericDomValue);
        }
        return null;
      }

      @NotNull
      public Object[] getVariants() {
        return getVariantsInner(DomUtil.getParentOfType(context.getInvocationElement(), HbmAttributeBase.class, true), context.getFile(),
                                MappingClassResolveConverter.getRoot(context), context.getModule());
      }

        @Override
        public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
          if (element instanceof PsiClass) {
            genericDomValue.setStringValue(((PsiClass)element).getQualifiedName());
            return genericDomValue.getXmlTag();
          }
          else {
            return super.bindToElement(element);
          }
        }
      }};
  }

}
