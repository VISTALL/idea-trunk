package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.ConvertContext;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Gregory.Shrago
 */
public class GeneratorClassResolvingConverter extends ClassWithShortcutsResolvingConverter {

  private static final @NonNls Map<String, String> GENERATOR_CLASS_MAP = new THashMap<String, String>();
  static {
    GENERATOR_CLASS_MAP.put("increment", HibernateConstants.GEN_INCREMENT);
    GENERATOR_CLASS_MAP.put("identity", HibernateConstants.GEN_IDENTITY);
    GENERATOR_CLASS_MAP.put("sequence", HibernateConstants.GEN_SEQUENCE);
    GENERATOR_CLASS_MAP.put("hilo", HibernateConstants.GEN_HILO);
    GENERATOR_CLASS_MAP.put("seqhilo", HibernateConstants.GEN_SEQHILO);
    GENERATOR_CLASS_MAP.put("uuid", HibernateConstants.GEN_UUID);
    GENERATOR_CLASS_MAP.put("guid", HibernateConstants.GEN_GUID);
    GENERATOR_CLASS_MAP.put("native", "");
    GENERATOR_CLASS_MAP.put("assigned", HibernateConstants.GEN_ASSIGNED);
    GENERATOR_CLASS_MAP.put("select", HibernateConstants.GEN_SELECT);
    GENERATOR_CLASS_MAP.put("foreign", HibernateConstants.GEN_FOREIGN);
    GENERATOR_CLASS_MAP.put("sequence-identity", HibernateConstants.GEN_SEQUENCE_IDENTITY);
  }

  @Nullable
  protected String getBaseClassName(final ConvertContext context) {
    return HibernateConstants.GENERATOR_BASE_CLASS;
  }

  @NotNull
  protected Map<String, String> getShortcutsMap() {
    return GENERATOR_CLASS_MAP;
  }

  public static String getGeneratorClassName(final String classNameOrShortcut) {
    final String name = GENERATOR_CLASS_MAP.get(classNameOrShortcut);
    return StringUtil.isNotEmpty(name)? name : classNameOrShortcut; 
  }
}
