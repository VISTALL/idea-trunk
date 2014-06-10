/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.hibernate.console;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.database.console.ConsoleBase;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.query.HQLQueryPlan;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory.Shrago
 */
public class HibernateConsoleMain extends ConsoleBase {
  @NonNls private static final String NULL = "<null>";

  public static void main(String[] args) throws Exception {
    runConsole(new HibernateConsoleMain(), System.out);
  }

  private static final Formatter ourFormatter = chooseFormatter();

  private String myConfigurationClass;
  private Configuration myConfiguration;
  private SessionFactory mySessionFactory;
  private Session mySession;
  private Query myQuery;

  public HibernateConsoleMain() {
    myConfigurationClass = "org.hibernate.cfg.Configuration";
  }

  @Override
  protected String getErrorText(final String errorMsg, final Throwable throwable) {
    if (StringUtil.isEmpty(errorMsg) &&
        (throwable instanceof HibernateException || throwable instanceof IllegalStateException)) {
      final Throwable cause = getDeepestCause(throwable);
      final String topMessage = throwable != cause ? throwable.getLocalizedMessage() : null;
      return super.getErrorText((StringUtil.isNotEmpty(topMessage)? topMessage +" : ": "") + cause.getLocalizedMessage(), null);
    }
    return super.getErrorText(errorMsg, throwable);
  }

  public void initUserConfigurator(final String userDriverClass) throws Exception {
    initUserConfiguratorInner(userDriverClass);
  }

  public void setConfigurationClass(final String configurationClass) {
    myConfigurationClass = configurationClass;
  }

  public void initConfiguration(final String filePath) throws Exception {
    myConfiguration = (Configuration)Class.forName(myConfigurationClass).newInstance();
    myConfiguration.configure(new File(filePath));
    tryInvokeMethod(getUserConfigurator(), "configure", Object.class, myConfiguration, null);
  }

  public void initSessionFactory(final String nothing) {
    if (myConfiguration == null) throw new IllegalStateException("Hibernate configuration is not created");
    if (mySession != null) mySession.close();
    if (mySessionFactory != null) mySessionFactory.close();
    mySessionFactory = myConfiguration.buildSessionFactory();
    tryInvokeMethod(getUserConfigurator(), "configure", Object.class, mySessionFactory, null);
    mySession = mySessionFactory.openSession();
    tryInvokeMethod(getUserConfigurator(), "configure", Object.class, mySession, null);
  }

  public void prepareHql(final String query) {
    if (mySession == null) throw new IllegalStateException("Session is not created");
    myQuery = null;
    myQuery = mySession.createQuery(query);
    tryInvokeMethod(getUserConfigurator(), "configure", Object.class, myQuery, null);
  }

  public void prepareSql(final String query) {
    if (mySession == null) throw new IllegalStateException("Session is not created");
    myQuery = null;
    myQuery = mySession.createSQLQuery(query);
    tryInvokeMethod(getUserConfigurator(), "configure", Object.class, myQuery, null);
  }

  public void executeQuery(final String queryParameters) {
    if (myQuery == null) throw new IllegalStateException("Query is not prepared");
    setQueryParameters(myQuery, queryParameters);
    final long start = System.currentTimeMillis();
    final List list = myQuery.list();
    final long executionTime = System.currentTimeMillis() - start;
    @NonNls final StringBuilder sb = new StringBuilder();
    sb.append("<query>").append(escape(myQuery.getQueryString())).append("</query>");
    sb.append("<time>").append(executionTime).append("</time>");

    final Type[] returnTypes = myQuery.getReturnTypes();
    final String[] aliases = myQuery.getReturnAliases();
    for (int i = 0; i < returnTypes.length; i++) {
      final Type type = returnTypes[i];
      final String alias = aliases != null && StringUtil.parseInt(aliases[i], -1) == -1? aliases[i] : "";
      printColumnDefinition(sb, type, i, alias);
    }
    for (Object o : list) {
      sb.append("<r>");
      if (o instanceof Object[]) {
        final Object[] array = (Object[])o;
        for (int i = 0; i < array.length; i++) {
          objectToXml(array[i], returnTypes[i], sb);
        }
      }
      else {
        objectToXml(o, returnTypes[0], sb);
      }
      sb.append("</r>");
    }
    getOutputHandler().handleOutput(sb.toString());
  }

  public void translateQuery(final String hql) throws Exception {
    if (mySessionFactory == null) throw new IllegalStateException("SessionFactory is not created");
    final HQLQueryPlan plan = new HQLQueryPlan(hql, false, Collections.EMPTY_MAP, (SessionFactoryImplementor)mySessionFactory);
    @NonNls final StringBuilder sb = new StringBuilder();
    int i = 0;
    for (QueryTranslator translator : plan.getTranslators()) {
      i++;
      if (translator.isManipulationStatement()) {
        sb.append("DML #").append(i).append(" tables: ");
        boolean first = true;
        for (Object qspace : translator.getQuerySpaces()) {
          sb.append(qspace);
          if (!first) {
            sb.append(", ");
          }
          else {
            first = false;
          }
        }
      }
      else {
        final Type[] returnTypes = translator.getReturnTypes();
        sb.append("SQL #").append(i).append(" types: ");
        for (int j = 0; j < returnTypes.length; j++) {
          final Type returnType = returnTypes[j];
          sb.append(returnType.getName());
          if (j < returnTypes.length - 1) {
            sb.append(", ");

          }
        }
      }
      sb.append("\n-----------------\n");
      for (Object o : translator.collectSqlStrings()) {
        final String sql = (String)o;
        final String formattedSql = ourFormatter.formatSQL(sql);
        sb.append(formattedSql);
      }
    }
    getOutputHandler().handleOutput(escape(sb.toString()));
  }

  public void generateDDL(final String nothing) {
    if (myConfiguration == null) throw new IllegalStateException("Hibernate configuration is not created");
    final Dialect dialect = Dialect.getDialect(myConfiguration.getProperties());
    final String[] createSQL = myConfiguration.generateSchemaCreationScript(dialect);
    final String[] dropSQL = myConfiguration.generateDropSchemaScript(dialect);
    final StringBuilder sb = new StringBuilder();
    for (String sql : createSQL) {
      sb.append(ourFormatter.formatDDL(sql)).append(";\n");
    }
    for (String sql : dropSQL) {
      sb.append(ourFormatter.formatDDL(sql)).append(";\n");
    }
    getOutputHandler().handleOutput(escape(sb.toString()));
  }

  private static void setQueryParameters(final Query query, final String queryParameters) {
    final Map<String, String> map = stringToMap(queryParameters);
    for (String name : map.keySet()) {
      final String value = map.get(name);
      if (name.startsWith("?")) {
        query.setParameter(Integer.parseInt(name.substring(1)), value, new StringType());
      }
      else {
        query.setParameter(name, value, new StringType());
      }
    }
  }

  private void printColumnDefinition(@NonNls final StringBuilder sb, final Type type, final int index, final String prefix) {
    final String nextPrefix = (prefix.length() == 0 ? StringUtil.getShortName(type.getName()) : prefix) + ".";
    if (type.isEntityType()) {
      final ClassMetadata metaData = mySessionFactory.getClassMetadata(type.getName());
      if (metaData != null) {
        if (metaData.hasIdentifierProperty()) {
          final String name = metaData.getIdentifierPropertyName();
          printColumnDefinition(sb, metaData.getIdentifierType(), 0, nextPrefix + name);
        }
        final String[] names = metaData.getPropertyNames();
        final Type[] types = metaData.getPropertyTypes();
        for (int i = 0; i < names.length; i++) {
          if (types[i].isAssociationType()) continue;
          printColumnDefinition(sb, types[i], i + 1, nextPrefix + names[i]);
        }
      }
      else {
        printColumnDefinitionDefault(sb, type, index, prefix);
      }
    }
    else if (type instanceof AbstractComponentType) {
      final AbstractComponentType componentType = (AbstractComponentType)type;
      final String[] names = componentType.getPropertyNames();
      final Type[] types = componentType.getSubtypes();
      for (int i = 0; i < names.length; i++) {
        if (types[i].isAssociationType()) continue;
        printColumnDefinition(sb, types[i], i, nextPrefix + names[i]);
      }
    }
    else {
      printColumnDefinitionDefault(sb, type, index, prefix);
    }
  }

  private static void printColumnDefinitionDefault(final StringBuilder sb, final Type type, final int index, final String prefix) {
    sb.append("<column name=\"").append(prefix.length() > 0 ? prefix : ("#" + index));
    sb.append("\" type=\"").append(escape(type.getName()));
    sb.append("\" class=\"").append(escape(type.getReturnedClass().getName()));
    sb.append("\"/>");
  }

  private void objectToXml(@Nullable final Object o, @Nullable final Type type, @NonNls final StringBuilder sb) {
    if (type != null && type.isEntityType()) {
      final ClassMetadata metaData = mySessionFactory.getClassMetadata(type.getName());
      if (metaData != null) {
        if (metaData.hasIdentifierProperty()) {
          objectToXml(o == null? null : metaData.getIdentifier(o, EntityMode.POJO), metaData.getIdentifierType(), sb);
        }
        final Type[] types = metaData.getPropertyTypes();
        if (types != null) {
          final Object[] values = o == null? null : metaData.getPropertyValues(o, EntityMode.POJO);
          for (int i = 0; i < types.length; i++) {
            if (types[i] != null && types[i].isAssociationType()) continue;
            objectToXml(values == null? null : values[i], types[i], sb);
          }
          return;
        }
      }
    }
    else if (type instanceof AbstractComponentType) {
      final AbstractComponentType componentType = (AbstractComponentType)type;
      final Type[] types = componentType.getSubtypes();
      if (types != null) {
        final Object[] values = o == null? null : componentType.getPropertyValues(o, EntityMode.POJO);
        for (int i = 0; i < types.length; i++) {
          if (types[i] != null && types[i].isAssociationType()) continue;
          objectToXml(values == null? null : values[i], types[i], sb);
        }
        return;
      }
    }
    objectToXmlDefault(o, type, sb);
  }

  private void objectToXmlDefault(final Object o, final Type type, final StringBuilder sb) {
    sb.append("<c>");
    sb.append(escape(o == null? NULL :type.toLoggableString(o, (SessionFactoryImplementor)mySessionFactory)));
    sb.append("</c>");
  }

  private static Formatter chooseFormatter() {
    if (new Formatter32().isReady()) {
      return new Formatter32();
    }
    else if (new Formatter33().isReady()) {
      return new Formatter33();
    }
    else {
      return new Formatter() {

        public boolean isReady() {
          return true;
        }

        public String formatDDL(final String sql) {
          return sql;
        }

        public String formatSQL(final String ddl) {
          return ddl;
        }
      };
    }
  }

  public interface Formatter {
    boolean isReady();
    String formatDDL(final String sql);
    String formatSQL(final String ddl);
  }
}
