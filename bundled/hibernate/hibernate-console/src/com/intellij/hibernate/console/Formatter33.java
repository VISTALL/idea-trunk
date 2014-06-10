package com.intellij.hibernate.console;

import org.hibernate.jdbc.util.FormatStyle;

/**
 * @author Gregory.Shrago
 */
public class Formatter33 implements HibernateConsoleMain.Formatter {
  public boolean isReady() {
    try {
      return Class.forName("org.hibernate.jdbc.util.FormatStyle") != null;
    }
    catch (Exception e) {
      return false;
    }
  }

  public String formatDDL(final String sql) {
    return FormatStyle.DDL.getFormatter().format(sql);
  }

  public String formatSQL(final String sql) {
    return FormatStyle.BASIC.getFormatter().format(sql);
  }
}