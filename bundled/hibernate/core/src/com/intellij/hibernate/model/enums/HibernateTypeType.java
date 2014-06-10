package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum HibernateTypeType implements NamedEnum {
  INTEGER("integer"), LONG("long"), SHORT("short"), FLOAT("float"), DOUBLE("double"), CHARACTER("character"),
  BYTE("byte"), BOOLEAN("boolean"), YES_NO("yes_no"), TRUE_FALSE("true_false"),

  STRING("string"),
  DATE("date"), TIME("time"), TIMESTAMP("timestamp"),
  CALENDAR("calendar"), CALENDAR_DATE("calendar_date"),
  BIG_DECIMAL("big_decimal"), BIG_INTEGER("big_integer"),
  LOCALE("locale"), TIMEZONE("timezone"), CURRENCY("currency"),

  CLASS("class"),

  BINARY("binary"), TEXT("text"),
  SERIALIZABLE("serializable"), CLOB("clob"), BLOB("blob"),
  IMM_DATE("imm_date"), IMM_TIME("imm_time"), IMM_TIMESTAMP("imm_timestamp"), IMM_CALENDAR("imm_calendar"),
  IMM_CALENDAR_DATE("imm_calendar_date"), IMM_SERIALIZABLE("imm_serializable"), IMM_BINARY("imm_binary");


  private final String myValue;

  private HibernateTypeType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

  @NonNls
  public String getJavaTypeName() {
    switch (this) {
      case BIG_DECIMAL:
        return "java.math.BigDecimal";
      case BIG_INTEGER:
        return "java.math.BigInteger";
      case IMM_BINARY:
      case BINARY:
        return "byte[]";
      case BLOB:
        return "java.sql.Blob";
      case BOOLEAN:
        return "boolean";
      case BYTE:
        return "byte";
      case IMM_CALENDAR:
      case CALENDAR:
        return "java.util.Calendar";
      case IMM_CALENDAR_DATE:      
      case CALENDAR_DATE:
        return "java.util.Calendar";
      case CHARACTER:
        return "char";
      case CLASS:
        return "java.lang.Class";
      case CLOB:
        return "java.sql.Clob";
      case CURRENCY:
        return "java.util.Currency";
      case IMM_DATE:
      case DATE:
        return "java.util.Date";
      case DOUBLE:
        return "double";
      case FLOAT:
        return "float";
      case INTEGER:
        return "int";
      case LOCALE:
        return "java.util.Locale";
      case LONG:
        return "long";
      case IMM_SERIALIZABLE:
      case SERIALIZABLE:
        return "java.io.Serializable";
      case SHORT:
        return "short";
      case STRING:
      case TEXT:
        return "java.lang.String";
      case TIME:
      case IMM_TIME:
      case IMM_TIMESTAMP:
      case TIMESTAMP:
        return "java.util.Date";
      case TIMEZONE:
        return "java.util.TimeZone";
      case TRUE_FALSE:
      case YES_NO:
        return "boolean";
    }
    assert false;
    return null;
  }
}
