package com.intellij.hibernate.model;

import org.jetbrains.annotations.NonNls;

/**
 * @author Gregory.Shrago
 */
public interface HibernatePropertiesConstants {
  @NonNls String HIBERNATE_PREFIX = "hibernate.";

  @NonNls String CONNECTION_PROVIDER = "connection.provider_class";
  @NonNls String DRIVER = "connection.driver_class";
  @NonNls String ISOLATION = "connection.isolation";
  @NonNls String URL = "connection.url";
  @NonNls String USER = "connection.username";
  @NonNls String PASS = "connection.password";
  @NonNls String AUTOCOMMIT = "connection.autocommit";
  @NonNls String POOL_SIZE = "connection.pool_size";
  @NonNls String DATASOURCE = "connection.datasource";
  @NonNls String CONNECTION_PREFIX = "connection";
  @NonNls String JNDI_CLASS = "jndi.class";
  @NonNls String JNDI_URL = "jndi.url";
  @NonNls String JNDI_PREFIX = "jndi";
  @NonNls String SESSION_FACTORY_NAME = "session_factory_name";
  @NonNls String DIALECT = "dialect";
  @NonNls String DEFAULT_SCHEMA = "default_schema";
  @NonNls String DEFAULT_CATALOG = "default_catalog";
  @NonNls String SHOW_SQL = "show_sql";
  @NonNls String FORMAT_SQL = "format_sql";
  @NonNls String USE_SQL_COMMENTS = "use_sql_comments";
  @NonNls String MAX_FETCH_DEPTH = "max_fetch_depth";
  @NonNls String DEFAULT_BATCH_FETCH_SIZE = "default_batch_fetch_size";
  @NonNls String USE_STREAMS_FOR_BINARY = "jdbc.use_streams_for_binary";
  @NonNls String USE_SCROLLABLE_RESULTSET = "jdbc.use_scrollable_resultset";
  @NonNls String USE_GET_GENERATED_KEYS = "jdbc.use_get_generated_keys";
  @NonNls String STATEMENT_FETCH_SIZE = "jdbc.fetch_size";
  @NonNls String STATEMENT_BATCH_SIZE = "jdbc.batch_size";
  @NonNls String BATCH_STRATEGY = "jdbc.factory_class";
  @NonNls String BATCH_VERSIONED_DATA = "jdbc.batch_versioned_data";
  @NonNls String OUTPUT_STYLESHEET = "xml.output_stylesheet";
  @NonNls String C3P0_MAX_SIZE = "c3p0.max_size";
  @NonNls String C3P0_MIN_SIZE = "c3p0.min_size";
  @NonNls String C3P0_TIMEOUT = "c3p0.timeout";
  @NonNls String C3P0_MAX_STATEMENTS = "c3p0.max_statements";
  @NonNls String C3P0_ACQUIRE_INCREMENT = "c3p0.acquire_increment";
  @NonNls String C3P0_IDLE_TEST_PERIOD = "c3p0.idle_test_period";
  @NonNls String PROXOOL_PREFIX = "proxool";
  @NonNls String PROXOOL_XML = "proxool.xml";
  @NonNls String PROXOOL_PROPERTIES = "proxool.properties";
  @NonNls String PROXOOL_EXISTING_POOL = "proxool.existing_pool";
  @NonNls String PROXOOL_POOL_ALIAS = "proxool.pool_alias";
  @NonNls String AUTO_CLOSE_SESSION = "transaction.auto_close_session";
  @NonNls String FLUSH_BEFORE_COMPLETION = "transaction.flush_before_completion";
  @NonNls String RELEASE_CONNECTIONS = "connection.release_mode";
  @NonNls String CURRENT_SESSION_CONTEXT_CLASS = "current_session_context_class";
  @NonNls String TRANSACTION_STRATEGY = "transaction.factory_class";
  @NonNls String TRANSACTION_MANAGER_STRATEGY = "transaction.manager_lookup_class";
  @NonNls String USER_TRANSACTION = "jta.UserTransaction";
  @NonNls String CACHE_PROVIDER = "cache.provider_class";
  @NonNls String CACHE_PROVIDER_CONFIG = "cache.provider_configuration_file_resource_path";
  @NonNls String CACHE_NAMESPACE = "cache.jndi";
  @NonNls String USE_QUERY_CACHE = "cache.use_query_cache";
  @NonNls String QUERY_CACHE_FACTORY = "cache.query_cache_factory";
  @NonNls String USE_SECOND_LEVEL_CACHE = "cache.use_second_level_cache";
  @NonNls String USE_MINIMAL_PUTS = "cache.use_minimal_puts";
  @NonNls String CACHE_REGION_PREFIX = "cache.region_prefix";
  @NonNls String USE_STRUCTURED_CACHE = "cache.use_structured_entries";
  @NonNls String GENERATE_STATISTICS = "generate_statistics";
  @NonNls String USE_IDENTIFIER_ROLLBACK = "use_identifier_rollback";
  @NonNls String USE_REFLECTION_OPTIMIZER = "bytecode.use_reflection_optimizer";
  @NonNls String QUERY_TRANSLATOR = "query.factory_class";
  @NonNls String QUERY_SUBSTITUTIONS = "query.substitutions";
  @NonNls String HBM2DDL_AUTO = "hbm2ddl.auto";
  @NonNls String SQL_EXCEPTION_CONVERTER = "jdbc.sql_exception_converter";
  @NonNls String WRAP_RESULT_SETS = "jdbc.wrap_result_sets";
  @NonNls String ORDER_UPDATES = "order_updates";
  @NonNls String DEFAULT_ENTITY_MODE = "default_entity_mode";
  @NonNls String JACC_CONTEXTID = "jacc_context_id";
  @NonNls String BYTECODE_PROVIDER = "bytecode.provider";
  @NonNls String JPAQL_STRICT_COMPLIANCE = "query.jpaql_strict_compliance";
}
