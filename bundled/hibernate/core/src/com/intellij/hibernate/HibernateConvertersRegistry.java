package com.intellij.hibernate;

import com.intellij.hibernate.model.HibernateConstants;
import static com.intellij.hibernate.model.HibernatePropertiesConstants.*;
import com.intellij.hibernate.model.converters.ClassWithShortcutsResolvingConverter;
import com.intellij.hibernate.model.converters.GeneratorClassResolvingConverter;
import com.intellij.hibernate.model.enums.EntityModeType;
import com.intellij.hibernate.model.enums.HBM2DDLAutoType;
import com.intellij.hibernate.model.enums.ReleaseConnectionsType;
import com.intellij.hibernate.model.xml.config.Property;
import com.intellij.hibernate.model.xml.mapping.HbmGenerator;
import com.intellij.hibernate.model.xml.mapping.HbmParam;
import com.intellij.javaee.model.JavaeePersistenceORMResolveConverters;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.util.SmartList;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.*;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Gregory.Shrago
 */
public class HibernateConvertersRegistry {

  final static Map<String, String> SESSION_CONTEXT_MAP = new THashMap<String, String>();
  static {
    SESSION_CONTEXT_MAP.put("jta", "org.hibernate.context.JTASessionContext");
    SESSION_CONTEXT_MAP.put("thread", "org.hibernate.context.ThreadLocalSessionContext");
    SESSION_CONTEXT_MAP.put("managed", "org.hibernate.context.ManagedSessionContext");
  }

  private final Map<Class, Collection<Info>> myInfoMap = new THashMap<Class, Collection<Info>>();

  public HibernateConvertersRegistry(ConverterManager converterManager) {
    // configuration properties
    registerConfigProperty(AUTO_CLOSE_SESSION, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(CONNECTION_PROVIDER, getPsiClassConverter("org.hibernate.connection.ConnectionProvider"));
    registerConfigProperty(DRIVER, getPsiClassConverter("java.sql.Driver"));
    registerConfigProperty(ISOLATION, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(URL, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(USER, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(PASS, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(AUTOCOMMIT, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(POOL_SIZE, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(DATASOURCE, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(JNDI_CLASS, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(JNDI_URL, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(SESSION_FACTORY_NAME, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(DIALECT, getPsiClassConverter("org.hibernate.dialect.Dialect"));
    registerConfigProperty(DEFAULT_SCHEMA, getSchemaConverter(converterManager));
    registerConfigProperty(DEFAULT_CATALOG, getCatalogConverter(converterManager));
    registerConfigProperty(SHOW_SQL, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(FORMAT_SQL, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(USE_SQL_COMMENTS, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(MAX_FETCH_DEPTH, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(DEFAULT_BATCH_FETCH_SIZE, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(USE_STREAMS_FOR_BINARY, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(USE_SCROLLABLE_RESULTSET, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(USE_GET_GENERATED_KEYS, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(STATEMENT_FETCH_SIZE, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(STATEMENT_BATCH_SIZE, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(BATCH_STRATEGY, getPsiClassConverter("org.hibernate.jdbc.Batcher"));
    registerConfigProperty(BATCH_VERSIONED_DATA, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(OUTPUT_STYLESHEET, getResourceConverter());
    registerConfigProperty(C3P0_MAX_SIZE, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(C3P0_MIN_SIZE, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(C3P0_TIMEOUT, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(C3P0_MAX_STATEMENTS, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(C3P0_ACQUIRE_INCREMENT, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(C3P0_IDLE_TEST_PERIOD, ResolvingConverter.INTEGER_CONVERTER);
    registerConfigProperty(PROXOOL_XML, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(PROXOOL_PROPERTIES, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(PROXOOL_EXISTING_POOL, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(PROXOOL_POOL_ALIAS, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(AUTO_CLOSE_SESSION, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(FLUSH_BEFORE_COMPLETION, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(RELEASE_CONNECTIONS, EnumConverter.createEnumConverter(ReleaseConnectionsType.class));
    registerConfigProperty(CURRENT_SESSION_CONTEXT_CLASS, getPsiClassConverter("org.hibernate.context.CurrentSessionContext", SESSION_CONTEXT_MAP));
    registerConfigProperty(TRANSACTION_STRATEGY, getPsiClassConverter("org.hibernate.transaction.TransactionFactory"));
    registerConfigProperty(TRANSACTION_MANAGER_STRATEGY, getPsiClassConverter("org.hibernate.transaction.TransactionManagerLookup"));
    registerConfigProperty(USER_TRANSACTION, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(CACHE_PROVIDER, getPsiClassConverter("org.hibernate.cache.CacheProvider"));
    registerConfigProperty(CACHE_PROVIDER_CONFIG, getFilePathConverter());
    registerConfigProperty(CACHE_NAMESPACE, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(USE_QUERY_CACHE, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(QUERY_CACHE_FACTORY, getPsiClassConverter("org.hibernate.cache.QueryCacheFactory"));
    registerConfigProperty(USE_SECOND_LEVEL_CACHE, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(USE_MINIMAL_PUTS, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(CACHE_REGION_PREFIX, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(USE_STRUCTURED_CACHE, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(GENERATE_STATISTICS, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(USE_IDENTIFIER_ROLLBACK, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(USE_REFLECTION_OPTIMIZER, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(QUERY_TRANSLATOR, getPsiClassConverter("org.hibernate.hql.QueryTranslatorFactory"));
    registerConfigProperty(QUERY_SUBSTITUTIONS, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(HBM2DDL_AUTO, EnumConverter.createEnumConverter(HBM2DDLAutoType.class));
    registerConfigProperty(SQL_EXCEPTION_CONVERTER, getPsiClassConverter("org.hibernate.exception.SQLExceptionConverter"));
    registerConfigProperty(WRAP_RESULT_SETS, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(ORDER_UPDATES, ResolvingConverter.BOOLEAN_CONVERTER);
    registerConfigProperty(DEFAULT_ENTITY_MODE, EnumConverter.createEnumConverter(EntityModeType.class));
    registerConfigProperty(JACC_CONTEXTID, ResolvingConverter.EMPTY_CONVERTER);
    registerConfigProperty(BYTECODE_PROVIDER, getPsiClassConverter("org.hibernate.bytecode.BytecodeProvider"));
    registerConfigProperty(JPAQL_STRICT_COMPLIANCE, ResolvingConverter.BOOLEAN_CONVERTER);

    // generator parameters
    {
      final Condition<HbmParam> genAssignedCondition = createGeneratorTypeCondition(HibernateConstants.GEN_ASSIGNED);
      registerValueConverter(HbmParam.class, genAssignedCondition, "entity_name", getEntityConverter());
    }
    {
      final Condition<HbmParam> genIncrementCondition = createGeneratorTypeCondition(HibernateConstants.GEN_INCREMENT);
      registerValueConverter(HbmParam.class, genIncrementCondition, "schema", getSchemaConverter(converterManager));
      registerValueConverter(HbmParam.class, genIncrementCondition, "catalog", getCatalogConverter(converterManager));
      registerValueConverter(HbmParam.class, genIncrementCondition, "tables", getTableConverter(true, converterManager));
      registerValueConverter(HbmParam.class, genIncrementCondition, "identity_tables", getTableConverter(true, converterManager));
      registerValueConverter(HbmParam.class, genIncrementCondition, "target_column", getColumnConverter(converterManager));
      registerValueConverter(HbmParam.class, genIncrementCondition, "column", getCatalogConverter(converterManager));
    }
    //final Condition genIdentityCondition = createGeneratorTypeCondition(HibernateConstants.GEN_IDENTITY);
    {
      final Condition<HbmParam> genSequenceCondition = createGeneratorTypeCondition(HibernateConstants.GEN_SEQUENCE);
      registerValueConverter(HbmParam.class, genSequenceCondition, "schema", getSchemaConverter(converterManager));
      registerValueConverter(HbmParam.class, genSequenceCondition, "catalog", getCatalogConverter(converterManager));
      registerValueConverter(HbmParam.class, genSequenceCondition, "sequence", getSequenceConverter(converterManager));
      registerValueConverter(HbmParam.class, genSequenceCondition, "parameters", ResolvingConverter.EMPTY_CONVERTER);
    }
    {
      final Condition<HbmParam> genHiLoCondition = createGeneratorTypeCondition(HibernateConstants.GEN_HILO);
      registerValueConverter(HbmParam.class, genHiLoCondition, "schema", getSchemaConverter(converterManager));
      registerValueConverter(HbmParam.class, genHiLoCondition, "catalog", getCatalogConverter(converterManager));
      registerValueConverter(HbmParam.class, genHiLoCondition, "table", getTableConverter(false, converterManager));
      registerValueConverter(HbmParam.class, genHiLoCondition, "max_lo", ResolvingConverter.INTEGER_CONVERTER);
    }
    {
      final Condition<HbmParam> genSeqHiLoCondition = createGeneratorTypeCondition(HibernateConstants.GEN_SEQHILO);
      registerValueConverter(HbmParam.class, genSeqHiLoCondition, "schema", getSchemaConverter(converterManager));
      registerValueConverter(HbmParam.class, genSeqHiLoCondition, "catalog", getCatalogConverter(converterManager));
      registerValueConverter(HbmParam.class, genSeqHiLoCondition, "sequence", getSequenceConverter(converterManager));
      registerValueConverter(HbmParam.class, genSeqHiLoCondition, "parameters", ResolvingConverter.EMPTY_CONVERTER);
      registerValueConverter(HbmParam.class, genSeqHiLoCondition, "max_lo", ResolvingConverter.INTEGER_CONVERTER);
    }
    {
      final Condition<HbmParam> genSelectCondition = createGeneratorTypeCondition(HibernateConstants.GEN_SELECT);
      registerValueConverter(HbmParam.class, genSelectCondition, "key", getPropertyConverter());
    }
    {
      final Condition<HbmParam> genForeignCondition = createGeneratorTypeCondition(HibernateConstants.GEN_FOREIGN);
      registerValueConverter(HbmParam.class, genForeignCondition, "property", getPropertyConverter());
      registerValueConverter(HbmParam.class, genForeignCondition, "entity_name", getEntityConverter());
    }
    {
      final Condition<HbmParam> genSequenceIdentityCondition = createGeneratorTypeCondition(HibernateConstants.GEN_SEQUENCE_IDENTITY);
      registerValueConverter(HbmParam.class, genSequenceIdentityCondition, "schema", getSchemaConverter(converterManager));
      registerValueConverter(HbmParam.class, genSequenceIdentityCondition, "catalog", getCatalogConverter(converterManager));
      registerValueConverter(HbmParam.class, genSequenceIdentityCondition, "sequence", getSequenceConverter(converterManager));
      registerValueConverter(HbmParam.class, genSequenceIdentityCondition, "parameters", ResolvingConverter.EMPTY_CONVERTER);
    }
  }

  private Condition<HbmParam> createGeneratorTypeCondition(final String generatorClassName) {
    return new Condition<HbmParam>() {
      public boolean value(final HbmParam domElement) {
        final DomElement parent = domElement.getParent();
        if (!(parent instanceof HbmGenerator)) return false;
        final HbmGenerator generator = (HbmGenerator)parent;
        final String className = GeneratorClassResolvingConverter.getGeneratorClassName(generator.getClazz().getStringValue());
        return Comparing.equal(className, generatorClassName);
      }
    };
  }

  public static Converter getPsiClassConverter(final String baseClassName) {
    return getPsiClassConverter(baseClassName, Collections.<String, String>emptyMap());
  }

  public static Converter getPsiClassConverter(final String baseClassName, final Map<String, String> shortcutMap) {
    return new ClassWithShortcutsResolvingConverter() {
      @Nullable
      protected String getBaseClassName(ConvertContext context) {
        return baseClassName;
      }

      @NotNull
      protected Map<String, String> getShortcutsMap() {
        return shortcutMap;
      }
    };
  }

  private static Converter getSchemaConverter(final ConverterManager converterManager) {
    return converterManager.getConverterInstance(JavaeePersistenceORMResolveConverters.SchemaResolver.class);
  }

  private static Converter getCatalogConverter(final ConverterManager converterManager) {
    return converterManager.getConverterInstance(JavaeePersistenceORMResolveConverters.CatalogResolver.class);
  }

  private static Converter getTableConverter(final boolean isList, final ConverterManager converterManager) {
    if (!isList) {
      return converterManager.getConverterInstance(JavaeePersistenceORMResolveConverters.TableResolver.class);
    }
    //TODO
    return ResolvingConverter.EMPTY_CONVERTER;
  }

  private static Converter getSequenceConverter(final ConverterManager converterManager) {
    return converterManager.getConverterInstance(JavaeePersistenceORMResolveConverters.SequenceResolver.class);
  }

  private static Converter getColumnConverter(final ConverterManager converterManager) {
    return converterManager.getConverterInstance(JavaeePersistenceORMResolveConverters.ColumnResolver.class);
  }

  private static Converter getEntityConverter() {
    return ResolvingConverter.EMPTY_CONVERTER;
    // TODO
  }

  private static Converter getPropertyConverter() {
    return ResolvingConverter.EMPTY_CONVERTER;
    // TODO
  }

  private static Converter getResourceConverter() {
    return ResolvingConverter.EMPTY_CONVERTER;
    // TODO
  }

  private static Converter getFilePathConverter() {
    return ResolvingConverter.EMPTY_CONVERTER;
    // TODO
  }

  private void registerConfigProperty(final String name, final Converter converter) {
    registerValueConverter(Property.class, Condition.TRUE, name, converter);
    registerValueConverter(Property.class, Condition.TRUE, HIBERNATE_PREFIX +name, converter);
  }

  @NotNull
  public Converter getValueConverter(final DomElement domElement) {
    final Info info = getInfo(domElement);
    final String name = info == null? null : ElementPresentationManager.getElementName(domElement);
    final Converter converter = name == null ? null : info.myMap.get(name);
    return converter == null? Converter.EMPTY_CONVERTER : converter;
  }

  @NotNull
  public Converter getNameConverter(final DomElement domElement) {
    final Info info = getInfo(domElement);
    return info == null? Converter.EMPTY_CONVERTER : info.myNameConverter;
  }

  @NotNull
  public <T extends DomElement> Map<String, Converter> getConverters(final Class<T> domClass) {
    final Collection<Info> infos = myInfoMap.get(domClass);
    if (infos == null) return Collections.emptyMap();
    final THashMap<String, Converter> map = new THashMap<String, Converter>();
    for (Info info : infos) {
      map.putAll(info.myMap);
    }
    return map;
  }

  @Nullable
  public <T extends DomElement> Converter getConverter(final Class<T> domClass, final String name) {
    final Collection<Info> infos = myInfoMap.get(domClass);
    if (infos == null) return null;
    for (Info info : infos) {
      if (info.myMap.containsKey(name)) return info.myMap.get(name);
    }
    return null;
  }

  @Nullable
  private Info getInfo(final DomElement domElement) {
    Class key = null;
    for (Class<?> aClass : domElement.getClass().getInterfaces()) {
      if (ReflectionCache.isAssignable(CommonModelElement.class, aClass)) {
        key = aClass;
        break;
      }
    }
    if (key == null) {
      return null;
    }
    final Collection<Info> infos = myInfoMap.get(key);
    if (infos == null) return null;
    for (Info info : infos) {
      if (info.myCondition.value(domElement)) return info;
    }
    return null;
  }

  public <T extends DomElement> void registerValueConverter(final Class<T> paramClass, final Condition<T> condition, @NonNls final String paramName, final Converter converter) {
    final Info info = getOrCreateInfo(paramClass, condition);
    info.myMap.put(paramName, converter);
  }

  private <T extends DomElement> Info getOrCreateInfo(final Class<T> paramClass, final Condition<T> condition) {
    Collection<Info> infos = myInfoMap.get(paramClass);
    if (infos == null) {
      myInfoMap.put(paramClass, infos = new SmartList<Info>());
    }
    for (Info info : infos) {
      if (info.myCondition == condition) {
        return info;
      }
    }
    final Info info = new Info();
    info.myMap = new THashMap<String, Converter>();
    info.myNameConverter = new NameConverter(info);
    info.myCondition = (Condition<DomElement>)condition;
    infos.add(info);
    return info;
  }

  private static class Info {
    Condition<DomElement> myCondition;
    Map<String, Converter> myMap;
    Converter myNameConverter;
  }

  private static class NameConverter extends ResolvingConverter<String> {

    private final Info myInfo;

    public NameConverter(final Info info) {
      myInfo = info;
    }

    @NotNull
    public Collection<? extends String> getVariants(final ConvertContext context) {
      return myInfo.myMap.keySet();
    }

    public String fromString(@Nullable @NonNls String s, final ConvertContext context) {
      return s;
    }

    public String toString(@Nullable String s, final ConvertContext context) {
      return s;
    }

  }
}
