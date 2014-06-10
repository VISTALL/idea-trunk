package com.intellij.hibernate.model.xml.impl.config;

import com.intellij.hibernate.model.xml.config.Mapping;
import com.intellij.hibernate.model.xml.config.Property;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.persistence.model.PersistenceListener;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.helpers.PersistenceUnitModelHelper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Gregory.Shrago
 */
public abstract class SessionFactoryImpl extends BaseImpl implements SessionFactory, PersistenceUnitModelHelper {

  private CachedValue<Properties> myProperties;

  public PersistenceUnitModelHelper getModelHelper() {
    return this;
  }

  @NotNull
  public List<? extends PersistenceListener> getPersistentListeners() {
    return Collections.emptyList(); // TODO
  }

  @NotNull
  public List<? extends GenericValue<PsiPackage>> getPackages() {
    return ContainerUtil.mapNotNull(getMappings(), new NullableFunction<Mapping, GenericValue<PsiPackage>>() {
      public GenericValue<PsiPackage> fun(final Mapping mapping) {
        final GenericAttributeValue<PsiPackage> packageValue = mapping.getPackage();
        return packageValue.getValue() != null ? packageValue : null;
      }
    });
  }

  @Nullable
  public GenericValue<String> getDataSourceName() {
    return null;
  }

  @NotNull
  public Properties getPersistenceUnitProperties() {
    if (myProperties == null) {
      myProperties = getPsiManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<Properties>() {
        public Result<Properties> compute() {
          return new Result<Properties>(getPersistenceUnitPropertiesInner(), getContainingFile());
        }
      }, false);
    }
    return myProperties.getValue();
  }

  @NotNull
  private Properties getPersistenceUnitPropertiesInner() {
    final Properties result = new Properties();
    for (Property<Object> property : getProperties()) {
      result.put(HibernateUtil.getFullPropertyName(property.getName().getValue()), property.getStringValue());
    }
    return result;
  }

  @NotNull
  public List<? extends GenericValue<PsiClass>> getClasses() {
    return ContainerUtil.mapNotNull(getMappings(), new NullableFunction<Mapping, GenericValue<PsiClass>>() {
      public GenericValue<PsiClass> fun(final Mapping mapping) {
        final GenericAttributeValue<PsiClass> classValue = mapping.getClazz();
        return classValue.getValue() != null ? classValue : null;
      }
    });
  }

  @NotNull
  public List<? extends GenericValue<PsiFile>> getJarFiles() {
    return ContainerUtil.mapNotNull(getMappings(), new NullableFunction<Mapping, GenericValue<PsiFile>>() {
      public GenericValue<PsiFile> fun(final Mapping mapping) {
        final GenericAttributeValue<PsiFile> jarFile = mapping.getJar();
        return jarFile.getValue() != null? jarFile : null;
      }
    });
  }

  @NotNull
  public <V extends PersistenceMappings> List<? extends GenericValue<V>> getMappingFiles(final Class<V> mappingsClass) {
    final ArrayList<V> mappings = HibernateUtil.getDomMappings(this, mappingsClass, new ArrayList<V>());
    final ArrayList<GenericValue<V>> list = new ArrayList<GenericValue<V>>();
    for (V mapping : mappings) {
      list.add(ReadOnlyGenericValue.getInstance(mapping));
    }
    return list;
  }
}
