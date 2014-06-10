package com.intellij.hibernate.springIntegration;

import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.model.PersistenceListener;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.model.helpers.PersistenceUnitModelHelper;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.NotNullFunction;
import com.intellij.util.PairProcessor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Gregory.Shrago
*/
public class SpringBeanPersistenceUnit implements PersistencePackage, PersistenceUnitModelHelper {
  public static final String SESSION_FACTORY = "org.springframework.orm.hibernate3.LocalSessionFactoryBean";
  @NonNls public static final String SESSION_FACTORY_1_0 = "org.springframework.orm.hibernate.LocalSessionFactoryBean";
  public static final String ANNO_SESSION_FACTORY = "org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean";

  @NonNls public static final String CACHEABLE_MAPPING_LOCATIONS = "cacheableMappingLocations";
  @NonNls public static final String MAPPING_LOCATIONS = "mappingLocations";
  @NonNls public static final String MAPPING_RESOURCES = "mappingResources";
  @NonNls public static final String MAPPING_DIRECTORY_LOCATIONS = "mappingDirectoryLocations";
  @NonNls public static final String CONFIG_LOCATION = "configLocation";
  @NonNls public static final String DATASOURCE = "dataSource";

  @NonNls public static final String ANNOTATED_CLASSES = "annotatedClasses";
  @NonNls public static final String ANNOTATED_PACKAGES = "annotatedPackages";

  @NonNls public static final String HIBERNATE_PROPERTIES = "hibernateProperties";

  private final SpringBean myBean;
  private final Module myModule;
  private final boolean mySupportsAnnotations;
  private final CachedValue<Properties> myProperties;
  private final CachedValue<SessionFactory> mySessionFactory;

  public SpringBeanPersistenceUnit(final SpringBean bean, final Module module, final boolean supportsAnnotations) {
    myBean = bean;
    myModule = module;
    mySupportsAnnotations = supportsAnnotations;
    myProperties = getPsiManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<Properties>() {
      public Result<Properties> compute() {
        final SessionFactory sessionFactory = mySessionFactory.getValue();
        final Object[] dependencies = sessionFactory == null ? new Object[]{getContainingFile()} : new Object[]{getContainingFile(), sessionFactory.getContainingFile()};
        return new Result<Properties>(getPersistenceUnitPropertiesInner(), dependencies);
      }
    }, false);
    mySessionFactory = getPsiManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<SessionFactory>() {
      public Result<SessionFactory> compute() {
        final SessionFactory factory = getSessionFactoryInner();
        final Object[] dependencies = factory == null? new Object[]{getContainingFile()} : new Object[]{getContainingFile(), factory.getContainingFile()};
        return new Result<SessionFactory>(factory, dependencies);
      }
    }, false);
    final XmlElement element = myBean.getXmlElement();
    if (element != null) {
      element.putUserData(PersistencePackage.PERSISTENCE_UNIT_KEY, this);
    }
  }

  public <T> T getUserData(final Key<T> key) {
    return myBean.getUserData(key);
  }

  public <T> void putUserData(final Key<T> key, final T value) {
    myBean.putUserData(key, value);
  }

  public boolean supportsAnnotations() {
    return mySupportsAnnotations;
  }

  public GenericValue<String> getName() {
    return myBean.getId();
  }

  public PersistenceUnitModelHelper getModelHelper() {
    return this;
  }

  public boolean isValid() {
    return myBean.isValid();
  }

  public SpringBean getBean() {
    return myBean;
  }

  @Nullable
  public XmlTag getXmlTag() {
    return myBean.getXmlTag();
  }

  public PsiManager getPsiManager() {
    final XmlElement element = myBean.getXmlElement();
    return element == null? PsiManager.getInstance(myModule.getProject()) : element.getManager();
  }

  @Nullable
  public Module getModule() {
    return myModule;
  }

  @Nullable
  public PsiElement getIdentifyingPsiElement() {
    return myBean.getXmlElement();
  }

  @Nullable
  public PsiFile getContainingFile() {
    final XmlElement element = myBean.getXmlElement();
    return element == null ? null : element.getContainingFile();
  }

  @NotNull
  public <V extends PersistenceMappings> List<? extends GenericValue<V>> getMappingFiles(final Class<V> mappingsClass) {
    final ArrayList<V> mappings = new ArrayList<V>();
    addMappings(mappings, mappingsClass, SpringUtils.findPropertyByName(myBean, MAPPING_LOCATIONS, true), false);
    addMappings(mappings, mappingsClass, SpringUtils.findPropertyByName(myBean, CACHEABLE_MAPPING_LOCATIONS, true), false);
    addMappings(mappings, mappingsClass, SpringUtils.findPropertyByName(myBean, MAPPING_RESOURCES, true), false);
    addMappings(mappings, mappingsClass, SpringUtils.findPropertyByName(myBean, MAPPING_DIRECTORY_LOCATIONS, true), true);
    final List<GenericValue<V>> result = ContainerUtil.map2List(mappings, new NotNullFunction<V, GenericValue<V>>() {
      @NotNull
      public GenericValue<V> fun(final V v) {
        return ReadOnlyGenericValue.getInstance(v);
      }
    });
    final SessionFactory sessionFactory = mySessionFactory.getValue();
    if (sessionFactory != null) {
      result.addAll(sessionFactory.getModelHelper().getMappingFiles(mappingsClass));
    }
    return result;
  }

  private <V extends PersistenceMappings> void addMappings(final Collection<V> result, final Class<V> mappingsClass, final SpringPropertyDefinition property, boolean directory) {
    if (!(property instanceof SpringProperty)) return;
    if (directory) {
      for (PsiDirectory psiDirectory : ResourceResolverUtils.getResourceItems((SpringProperty)property, new THashSet<PsiDirectory>(), ResourceResolverUtils.DIRECTORY_FILTER)) {
        addMappingsFromDirectory(result, mappingsClass, psiDirectory);
      }
    }
    else {
      for (PsiFile psiFile : ResourceResolverUtils.getResourceItems((SpringProperty)property, new THashSet<PsiFile>(), ResourceResolverUtils.FILE_FILTER)) {
        ContainerUtil.addIfNotNull(JamCommonUtil.getRootElement(psiFile, mappingsClass, myModule), result);
      }
    }
  }

  private <V extends PersistenceMappings> void addMappingsFromDirectory(final Collection<V> result, final Class<V> mappingsClass, final PsiDirectory psiDirectory) {
    for (PsiFile psiFile : psiDirectory.getFiles()) {
      ContainerUtil.addIfNotNull(JamCommonUtil.getRootElement(psiFile, mappingsClass, myModule), result);
    }
    for (PsiDirectory directory : psiDirectory.getSubdirectories()) {
      addMappingsFromDirectory(result, mappingsClass, directory);
    }
  }

  @NotNull
  public List<? extends PersistenceListener> getPersistentListeners() {
    return Collections.emptyList();
  }

  @NotNull
  public List<? extends GenericValue<PsiFile>> getJarFiles() {
    final SessionFactory sessionFactory = mySessionFactory.getValue();
    return sessionFactory == null? Collections.<GenericValue<PsiFile>>emptyList() : sessionFactory.getModelHelper().getJarFiles();
  }

  @NotNull
  public List<? extends GenericValue<PsiClass>> getClasses() {
    if (!mySupportsAnnotations) return Collections.emptyList();
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(myBean, ANNOTATED_CLASSES, true);
    if (!(property instanceof SpringProperty)) return Collections.emptyList();
    final ArrayList<GenericValue<PsiClass>> result = new ArrayList<GenericValue<PsiClass>>();
    ResourceResolverUtils.processSpringValues((SpringProperty)property, new PairProcessor<GenericDomValue, String>() {
      public boolean process(final GenericDomValue domValue, final String s) {
        final Object o = domValue.getValue();
        if (o instanceof PsiClass) {
          result.add((GenericValue<PsiClass>)domValue);
        }
        return true;
      }
    });
    final SessionFactory sessionFactory = mySessionFactory.getValue();
    if (sessionFactory != null) {
      result.addAll(sessionFactory.getModelHelper().getClasses());
    }
    return result;
  }

  @NotNull
  public List<? extends GenericValue<PsiPackage>> getPackages() {
    if (!mySupportsAnnotations) return Collections.emptyList();
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(myBean, ANNOTATED_PACKAGES, true);
    if (!(property instanceof SpringProperty)) return Collections.emptyList();
    final PsiManager manager = PsiManager.getInstance(myModule.getProject());
    final ArrayList<GenericValue<PsiPackage>> result = new ArrayList<GenericValue<PsiPackage>>();
    ResourceResolverUtils.processSpringValues((SpringProperty)property, new PairProcessor<GenericDomValue, String>() {
      public boolean process(final GenericDomValue domValue, final String s) {
        ResourceResolverUtils.processSeparatedString(s, ",", new PairProcessor<String, Integer>() {
          public boolean process(final String s, final Integer integer) {
            final PsiPackage aPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(s.trim());
            if (aPackage != null) {
              result.add(ReadOnlyGenericValue.getInstance(aPackage));
            }
            return true;
          }
        });
        return true;
      }
    });
    final SessionFactory sessionFactory = mySessionFactory.getValue();
    if (sessionFactory != null) {
      result.addAll(sessionFactory.getModelHelper().getPackages());
    }
    return result;
  }

  @Nullable
  public GenericValue<String> getDataSourceName() {
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(myBean, DATASOURCE, true);
    if (property != null) {
      final SpringBeanPointer springBean = SpringUtils.getReferencedSpringBean(property);
      if (springBean != null) {
        final String name = springBean.getName();
        return StringUtil.isNotEmpty(name)? ReadOnlyGenericValue.getInstance(name) : null;
      }
    }
    return null;
  }

  @NotNull
  public Properties getPersistenceUnitProperties() {
    return myProperties.getValue();
  }

  @NotNull
  private Properties getPersistenceUnitPropertiesInner() {
    final Properties result = new Properties();
    final SessionFactory sessionFactory = mySessionFactory.getValue();
    if (sessionFactory != null) {
      result.putAll(sessionFactory.getModelHelper().getPersistenceUnitProperties());
    }
    final SpringPropertyDefinition property = SpringUtils.findPropertyByName(myBean, HIBERNATE_PROPERTIES, true);
    if (property instanceof SpringProperty) {
      final Props props = ((SpringProperty)property).getProps();
      for (Prop prop : props.getProps()) {
        final Collection<String> variants = SpringUtils.getValueVariants(prop);
        result.put(HibernateUtil.getFullPropertyName(prop.getKey().getValue()), variants.isEmpty()? "" : variants.iterator().next());
      }
    }
    return result;
  }

  @Nullable
  private SessionFactory getSessionFactoryInner() {
    final SpringPropertyDefinition configProperty = SpringUtils.findPropertyByName(myBean, SpringBeanPersistenceUnit.CONFIG_LOCATION, true);
    if (configProperty instanceof SpringProperty) {
      final ArrayList<PsiFileSystemItem> files = ResourceResolverUtils
        .getResourceItems((SpringProperty)configProperty, new ArrayList<PsiFileSystemItem>(), ResourceResolverUtils.FILE_FILTER);
      if (!files.isEmpty()) {
        final HibernateConfiguration element = JamCommonUtil.getRootElement((PsiFile)files.get(0), HibernateConfiguration.class, myModule);
        if (element != null) {
          return element.getSessionFactory();
        }
      }
    }
    return null;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SpringBeanPersistenceUnit that = (SpringBeanPersistenceUnit)o;

    if (!myBean.equals(that.myBean)) return false;
    if (!myModule.equals(that.myModule)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = myBean.hashCode();
    result = 31 * result + myModule.hashCode();
    return result;
  }
}
