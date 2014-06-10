package com.intellij.hibernate.console;

import org.hibernate.pretty.Formatter;
import org.hibernate.pretty.DDLFormatter;

/**
 * @author Gregory.Shrago
 */
public class Formatter32 implements HibernateConsoleMain.Formatter {
  public boolean isReady() {
    try {
      return Class.forName("org.hibernate.pretty.Formatter") != null;
    }
    catch (Exception e) {
      return false;
    }
  }

  public String formatDDL(final String sql) {
    return new DDLFormatter(sql).format();
  }

  public String formatSQL(final String sql) {
    return (new Formatter(sql)).setInitialString("").setIndentString(" ").format();
  }
}
