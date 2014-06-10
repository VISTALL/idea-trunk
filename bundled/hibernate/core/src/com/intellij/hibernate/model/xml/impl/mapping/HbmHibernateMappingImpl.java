package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.javaee.model.xml.impl.RootBaseImpl;
import com.intellij.jpa.model.xml.impl.converters.ClassConverterBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.model.PersistenceListener;
import com.intellij.persistence.model.PersistentEmbeddable;
import com.intellij.persistence.model.helpers.PersistenceMappingsModelHelper;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.util.Consumer;
import com.intellij.util.containers.BidirectionalMultiMap;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmHibernateMappingImpl extends RootBaseImpl implements HbmHibernateMapping, PersistenceMappingsModelHelper {

  private CachedValue<BidirectionalMultiMap<String, String>> myImportMapValue;
  private CachedValue<BidirectionalMultiMap<String, String>> myTypedefMapValue;
  private CachedValue<Map<String, List<HbmEmbeddedAttributeBase>>> myEmbeddablesAttributes;
  private final Map<String, HbmEmbeddableImpl> myEmbeddables = new THashMap<String, HbmEmbeddableImpl>();

  @NotNull
  public PersistenceMappingsModelHelper getModelHelper() {
    return this;
  }

  public PropertyMemberType getDeclaredAccess() {
    final AccessType type = getDefaultAccess().getValue();
    return type == AccessType.FIELD? PropertyMemberType.FIELD : type == AccessType.PROPERTY? PropertyMemberType.GETTER : null;
  }

  public List<? extends PersistenceListener> getPersistentListeners() {
    return Collections.emptyList(); // TODO
  }

  @NotNull
  public List<HbmClassBase> getPersistentEntities() {
    final ArrayList<HbmClassBase> result = new ArrayList<HbmClassBase>();
    final Consumer<HbmClassBase> consumer = new Consumer<HbmClassBase>() {
      public void consume(final HbmClassBase classBase) {
        result.add(classBase);
      }
    };
    HibernateUtil.consumePersistentObjects(this, consumer, null);
    return result;
  }

  @NotNull
  public List<HbmClassBase> getPersistentSuperclasses() {
    return Collections.emptyList();
  }


  @NotNull
  public List<? extends PersistentEmbeddable> getPersistentEmbeddables() {
    final Set<String> keys = getEmbeddableAttributesMap().keySet();
    myEmbeddables.keySet().retainAll(keys);
    for (String key : keys) {
      if (!myEmbeddables.containsKey(key)) {
        myEmbeddables.put(key, new HbmEmbeddableImpl(this, key));
      }
    }
    return new ArrayList<PersistentEmbeddable>(myEmbeddables.values());
  }

  private Map<String, List<HbmEmbeddedAttributeBase>> getEmbeddableAttributesMap() {
    if (myEmbeddablesAttributes == null) {
      myEmbeddablesAttributes = PsiManager.getInstance(getManager().getProject()).getCachedValuesManager()
        .createCachedValue(new CachedValueProvider<Map<String, List<HbmEmbeddedAttributeBase>>>() {
          public Result<Map<String, List<HbmEmbeddedAttributeBase>>> compute() {
            return new Result<Map<String, List<HbmEmbeddedAttributeBase>>>(getEmbeddableAttributesMapInner(), DomUtil.getFile(HbmHibernateMappingImpl.this));
          }
        }, false);
    }
    return myEmbeddablesAttributes.getValue();
  }

  @NotNull
  public List<HbmQuery> getNamedQueries() {
    return getQueries();
  }

  @NotNull
  public List<HbmSqlQuery> getNamedNativeQueries() {
    return getSqlQueries();
  }

  @NotNull
  public BidirectionalMultiMap<String, String> getImportsMap() {
    if (myImportMapValue == null) {
      myImportMapValue = PsiManager.getInstance(getManager().getProject()).getCachedValuesManager()
        .createCachedValue(new CachedValueProvider<BidirectionalMultiMap<String, String>>() {
          public Result<BidirectionalMultiMap<String, String>> compute() {
            final BidirectionalMultiMap<String, String> result = new BidirectionalMultiMap<String, String>();
            for (HbmImport hbmImport : getImports()) {
              final String className = hbmImport.getClazz().getStringValue();
              final String renameTo = hbmImport.getRename().getStringValue();
              if (StringUtil.isNotEmpty(className)) {
                result.put(className, StringUtil.isEmpty(renameTo) ? PsiNameHelper.getShortClassName(className) : renameTo);
              }
            }
            return new Result<BidirectionalMultiMap<String, String>>(result, DomUtil.getFile(HbmHibernateMappingImpl.this));
          }
        }, false);
    }
    final BidirectionalMultiMap<String, String> map = myImportMapValue.getValue();
    assert map != null;
    return map;
  }

  @NotNull
  public BidirectionalMultiMap<String, String> getTypedefMap() {
    if (myTypedefMapValue == null) {
      myTypedefMapValue = PsiManager.getInstance(getManager().getProject()).getCachedValuesManager()
      .createCachedValue(new CachedValueProvider<BidirectionalMultiMap<String, String>>() {
        public Result<BidirectionalMultiMap<String, String>> compute() {
          final BidirectionalMultiMap<String, String> result = new BidirectionalMultiMap<String, String>();
          for (HbmTypedef hbmTypedef : getTypedefs()) {
            final GenericAttributeValue<PsiClass> clazzValue = hbmTypedef.getClazz();
            final PsiClass clazz = clazzValue.getValue();
            final String className = clazz == null ? clazzValue.getStringValue() : clazz.getQualifiedName();
            final String typeName = hbmTypedef.getName().getStringValue();
            if (StringUtil.isNotEmpty(className)) {
              result.put(className, StringUtil.isEmpty(typeName) ? PsiNameHelper.getShortClassName(className) : typeName);
            }
          }
          return new Result<BidirectionalMultiMap<String, String>>(result, DomUtil.getFile(HbmHibernateMappingImpl.this));
        }
      }, false);
    }
    final BidirectionalMultiMap<String, String> map = myTypedefMapValue.getValue();
    assert map != null;
    return map;
  }

  private Map<String, List<HbmEmbeddedAttributeBase>> getEmbeddableAttributesMapInner() {
    final Map<String, List<HbmEmbeddedAttributeBase>> map = new THashMap<String, List<HbmEmbeddedAttributeBase>>();
    HibernateUtil.consumePersistentObjects(this, null, new Consumer<HbmEmbeddedAttributeBase>() {
      public void consume(final HbmEmbeddedAttributeBase o) {
        final String key = getEmbeddableAttributeKey(o);
        if (key != null) {
          List<HbmEmbeddedAttributeBase> list = map.get(key);
          if (list == null) {
            map.put(key, list = new ArrayList<HbmEmbeddedAttributeBase>());
          }
          list.add(o);
        }
      }
    });
    return map;
  }

  @Nullable
  private String getEmbeddableAttributeKey(final HbmEmbeddedAttributeBase o) {
    final String key;
    final PsiClass psiClass = PersistenceCommonUtil.getTargetClass(o);
    if (psiClass == null) {
      key = ClassConverterBase.getQualifiedClassName(o.getClazz().getStringValue(), getPackage().getStringValue());
    }
    else {
      key = psiClass.getQualifiedName();
    }
    return key;
  }


  @Nullable
  HbmEmbeddableImpl getEmbeddableByAttribute(final HbmEmbeddedAttributeBase o) {
    final String key = getEmbeddableAttributeKey(o);
    if (key == null) return null;
    return myEmbeddables.get(key);
  }

  @Nullable
  List<HbmEmbeddedAttributeBase> getEmbeddableAttributesByKey(final String key) {
    return getEmbeddableAttributesMap().get(key);
  }

}
