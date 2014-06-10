package com.intellij.hibernate.facet;

import com.intellij.facet.*;
import com.intellij.hibernate.HibernateInspectionToolProvider;
import com.intellij.jpa.highlighting.HibernateModelValidator;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.hibernate.model.HibernatePropertiesConstants;
import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.model.xml.mapping.HbmHibernateMapping;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.javaee.JavaeePersistenceDescriptorsConstants;
import com.intellij.javaee.JavaeeUtil;
import com.intellij.javaee.model.common.persistence.mapping.EntityMappings;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.jpa.JpaInspectionToolProvider;
import com.intellij.jpa.facet.JpaFacetImpl;
import com.intellij.jpa.model.annotations.mapping.EntityMappingsImpl;
import com.intellij.jpa.model.common.MergedPersistenceMappings;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.LanguageLevelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistencePackageDefaults;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.model.validators.ModelValidator;
import com.intellij.persistence.util.PersistenceModelBrowser;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.ql.psi.impl.QLLanguage;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.descriptors.*;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericValueUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Gregory.Shrago
 */
public class HibernateFacet extends PersistenceFacetBase<HibernateFacetConfiguration, PersistencePackage> {

  public static final FacetTypeId<HibernateFacet> ID = new FacetTypeId<HibernateFacet>("hibernate");

  private final ConfigFileContainer myDescriptorsContainer;
  private EntityMappingsImpl myAnnotationsPersistenceRoot;

  private final JpaFacetImpl.UnitDataSourceMap<PersistencePackage> myUnitDataSourceMap;
  private final CachedValue<List<PersistencePackage>> myUnitsValue;

  @Nullable
  public static HibernateFacet getInstance(@NotNull final Module module) {
    return FacetManager.getInstance(module).getFacetByType(ID);
  }

  public HibernateFacet(@NotNull final FacetType<HibernateFacet, HibernateFacetConfiguration> facetType, @NotNull final Module module,
                        final String name, @NotNull final HibernateFacetConfiguration configuration, Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    myUnitDataSourceMap = new JpaFacetImpl.UnitDataSourceMap<PersistencePackage>(configuration.getUnitToDataSourceMap(), this);
    myDescriptorsContainer = ConfigFileFactory.getInstance().createConfigFileContainer(getModule().getProject(), configuration.getDescriptorsConfiguration().getMetaDataProvider(), configuration.getDescriptorsConfiguration());
    myDescriptorsContainer.addListener(new ConfigFileAdapter() {
      protected void configChanged(final ConfigFile configFile) {
        getModule().getMessageBus().syncPublisher(FacetManager.FACETS_TOPIC).facetConfigurationChanged(HibernateFacet.this);
      }
    });
    Disposer.register(this, myDescriptorsContainer);
    myUnitsValue = PsiManager.getInstance(module.getProject()).getCachedValuesManager().createCachedValue(new CachedValueProvider<List<PersistencePackage>>() {
      public Result<List<PersistencePackage>> compute() {
        return getPersistenceUnitsInner();
      }
    }, false);
  }

  @NotNull
  public List<PersistencePackage> getExtensionSessionFactories() {
    return ContainerUtil.concat(Extensions.getExtensions(HibernateSessionFactoryProvider.EP_NAME), new Function<HibernateSessionFactoryProvider, Collection<? extends PersistencePackage>>() {
      public Collection<PersistencePackage> fun(final HibernateSessionFactoryProvider provider) {
        return provider.getSessionFactories(HibernateFacet.this);
      }
    });
  }

  @NotNull
  public List<HibernateConfiguration> getHibernateConfigurations() {
    final ArrayList<HibernateConfiguration> result = new ArrayList<HibernateConfiguration>();
    for (ConfigFile configFile : myDescriptorsContainer.getConfigFiles()) {
      if (configFile.getMetaData() == HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA) {
        ContainerUtil.addIfNotNull(JamCommonUtil.getRootElement(configFile.getPsiFile(), HibernateConfiguration.class, getModule()), result);
      }
    }
    return result;
  }

  public List<HibernateConfiguration> getDefaultHibernateConfigurations() {
    final ArrayList<HibernateConfiguration> result = new ArrayList<HibernateConfiguration>();
    final PsiManager psiManager = PsiManager.getInstance(getModule().getProject());
    for (VirtualFile root : ModuleRootManager.getInstance(getModule()).getSourceRoots()) {
      for (VirtualFile child : root.getChildren()) {
        if (child.isDirectory()) continue;
        if (!child.getName().endsWith(".cfg.xml")) continue;
        ContainerUtil.addIfNotNull(JamCommonUtil.getRootElement(psiManager.findFile(child), HibernateConfiguration.class, getModule()), result);
      }
    }
    return result;
  }

  public ConfigFile[] getDescriptors() {
    return myDescriptorsContainer.getConfigFiles();
  }

  public ConfigFileContainer getDescriptorsContainer() {
    return myDescriptorsContainer;
  }

  @NotNull
  public List<PersistencePackage> getPersistenceUnits() {
    return myUnitsValue.getValue();
  }

  private CachedValueProvider.Result<List<PersistencePackage>> getPersistenceUnitsInner() {
    final List<PersistencePackage> result = new ArrayList<PersistencePackage>();
    for (HibernateConfiguration configuration : getHibernateConfigurations()) {
      result.add(configuration.getSessionFactory());
    }
    result.addAll(getExtensionSessionFactories());
    return new CachedValueProvider.Result<List<PersistencePackage>>(
      result, PsiModificationTracker.MODIFICATION_COUNT, FacetModificationTrackingService.getInstance(this).getFacetModificationTracker(this));
  }

  @Nullable
  public EntityMappings getAnnotationEntityMappings() {
    refreshModel();
    return myAnnotationsPersistenceRoot;
  }

  @NotNull
  public PersistenceMappings getEntityMappings(@NotNull final PersistencePackage unit) {
    assert unit.isValid();
    refreshModel();
    final Set<PersistenceMappings> xmlMappings = HibernateUtil.getGenericValues(unit.getModelHelper().getMappingFiles(PersistenceMappings.class), new THashSet<PersistenceMappings>());
    final ArrayList<PersistenceMappings> allMappings = new ArrayList<PersistenceMappings>(xmlMappings);
    final Collection<String> classNames = GenericValueUtil.getClassStringCollection(unit.getModelHelper().getClasses(), new THashSet<String>());
    final Collection<String> packageNames = HibernateUtil.getStringValues(unit.getModelHelper().getPackages(), new THashSet<String>());
    final Collection<String> jarFiles = HibernateUtil.getStringValues(unit.getModelHelper().getJarFiles(), new THashSet<String>());
    if (myAnnotationsPersistenceRoot != null) {
      final PersistenceMappings entityMappings = myAnnotationsPersistenceRoot.createCustomMappings(null, classNames, jarFiles, packageNames);
      allMappings.add(0, entityMappings);
    }
    return allMappings.isEmpty()?
      DomManager.getDomManager(getModule().getProject()).createMockElement(HbmHibernateMapping.class, getModule(), true) :
      new MergedPersistenceMappings(this, allMappings);
  }

  @NotNull
  public List<PersistenceMappings> getDefaultEntityMappings(@NotNull final PersistencePackage unit) {
    return Collections.emptyList();
  }

  @NotNull
  public Class<SessionFactory> getPersistenceUnitClass() {
    return SessionFactory.class;
  }

  @NotNull
  public Map<ConfigFileMetaData, Class<? extends PersistenceMappings>> getSupportedDomMappingFormats() {
    final Map<ConfigFileMetaData, Class<? extends PersistenceMappings>> map = new LinkedHashMap<ConfigFileMetaData, Class<? extends PersistenceMappings>>();
    map.put(HibernateDescriptorsConstants.HIBERNATE_MAPPING_META_DATA, HbmHibernateMapping.class);
    map.put(JavaeePersistenceDescriptorsConstants.ORM_XML_META_DATA, com.intellij.javaee.model.xml.persistence.mapping.EntityMappings.class);
    return map;
  }

  public String getDataSourceId(@NotNull final PersistencePackage unit) {
    return myUnitDataSourceMap.getDataSourceID(unit);
  }

  public void setDataSourceId(@NotNull final PersistencePackage unit, final String dataSourceName) {
    myUnitDataSourceMap.setDataSourceID(unit, dataSourceName);
  }

  public Language getQlLanguage() {
    return QLLanguage.HIBERNATE_QL;
  }

  @NotNull
  public ModelValidator getModelValidator(@Nullable final PersistencePackage unit) {
    final PersistenceModelBrowser browser = PersistenceCommonUtil.createFacetAndUnitModelBrowser(this, unit, null);
    return new HibernateModelValidator(browser);
  }

  @NotNull
  public Class[] getInspectionToolClasses() {
    return ArrayUtil.mergeArrays(new JpaInspectionToolProvider().getInspectionClasses(), new HibernateInspectionToolProvider().getInspectionClasses(), Class.class);
  }

  private static final Key<CachedValue<PersistencePackageDefaults>> SESSION_FACTORY_DEFAULTS = Key.create("SESSION_FACTORY_DEFAULTS");

  @NotNull
  public PersistencePackageDefaults getPersistenceUnitDefaults(@NotNull final PersistencePackage unit) {
    assert unit.isValid();
    CachedValue<PersistencePackageDefaults> cachedValue = unit.getUserData(SESSION_FACTORY_DEFAULTS);
    if (cachedValue == null) {
      cachedValue = unit.getPsiManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<PersistencePackageDefaults>() {
        public Result<PersistencePackageDefaults> compute() {
          final Properties properties = unit.getModelHelper().getPersistenceUnitProperties();
          final String schema = properties.getProperty(HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.DEFAULT_SCHEMA));
          final String catalog = properties.getProperty(HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.DEFAULT_CATALOG));
          final PersistencePackageDefaults result = new PersistencePackageDefaults() {
            @Nullable
            public String getSchema() {
              return schema;
            }

            @Nullable
            public String getCatalog() {
              return catalog;
            }

            @Nullable
            public PropertyMemberType getAccess() {
              return null;
            }
          };
          return new Result<PersistencePackageDefaults>(result, unit.getContainingFile());
        }
      }, false);
      unit.putUserData(SESSION_FACTORY_DEFAULTS, cachedValue);
    }
    return cachedValue.getValue();
  }

  private void refreshModel() {
    final boolean isJdk5 = LanguageLevel.JDK_1_5.compareTo(LanguageLevelUtil.getEffectiveLanguageLevel(getModule())) <= 0;
    if (!getPersistenceUnits().isEmpty()) {
      if (isJdk5 && myAnnotationsPersistenceRoot == null) {
        myAnnotationsPersistenceRoot = new EntityMappingsImpl(this);
      }
    }
    else if (ApplicationManager.getApplication().isUnitTestMode()) {
      if (myAnnotationsPersistenceRoot == null) {
        myAnnotationsPersistenceRoot = new EntityMappingsImpl(this);
      }
    }
    else if (myAnnotationsPersistenceRoot != null) {
      myAnnotationsPersistenceRoot = null;
    }
  }


  public void initFacet() {
    JavaeeUtil.installDomAndJamListeners(this, myDescriptorsContainer);
  }

  public void disposeFacet() {
    if (myAnnotationsPersistenceRoot != null) {
      myAnnotationsPersistenceRoot = null;
    }
  }
}
