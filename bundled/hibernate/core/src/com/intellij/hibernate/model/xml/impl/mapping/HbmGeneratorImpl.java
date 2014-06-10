package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.xml.mapping.HbmGenerator;
import com.intellij.hibernate.model.xml.mapping.HbmParam;
import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import com.intellij.util.ArrayUtil;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmGeneratorImpl extends BaseImpl implements HbmGenerator {
  public GenericValue<String> getTableName() {
    return findParameter("table", "tables", "target_tables");
  }

  public GenericValue<String> getCatalog() {
    return findParameter("catalog");
  }

  public GenericValue<String> getSchema() {
    return findParameter("schema");
  }

  private GenericValue<String> findParameter(final String... names) {
    for (HbmParam param : getParams()) {
      final String name = param.getName().getStringValue();
      if (ArrayUtil.find(names, name) > -1) {
        final String value = param.getStringValue();
        return ReadOnlyGenericValue.getInstance(getFirstInList(value));
      }
    }
    return ReadOnlyGenericValue.getInstance(null);
  }

  private static String getFirstInList(final String value) {
    if (StringUtil.isEmpty(value)) return value;
    final int idx = value.indexOf(',');
    if (idx == -1) return value;
    return value.substring(0, idx).trim();
  }

}
