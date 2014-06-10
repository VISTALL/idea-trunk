package com.intellij.hibernate.model.xml.mapping;

import com.intellij.persistence.model.TableInfoProvider;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.javaee.model.JavaeePersistenceORMResolveConverters;
import com.intellij.javaee.model.xml.CommonDomModelElement;

/**
 * @author Gregory.Shrago
 */
public interface HbmTableInfoProvider extends TableInfoProvider, CommonDomModelElement {
  @Convert(JavaeePersistenceORMResolveConverters.TableResolver.class)
  GenericAttributeValue<String> getTableName();

  @Convert(JavaeePersistenceORMResolveConverters.CatalogResolver.class)
  GenericAttributeValue<String> getCatalog();

  @Convert(JavaeePersistenceORMResolveConverters.SchemaResolver.class)
  GenericAttributeValue<String> getSchema();
}
