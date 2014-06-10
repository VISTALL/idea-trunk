// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * http://www.springframework.org/schema/beans:dependency-checkAttrType enumeration.
 */
public enum DependencyCheck implements NamedEnum {
  
    ALL ("all"),
    DEFAULT ("default"),
    NONE ("none"),
    OBJECTS ("objects"),
    SIMPLE ("simple");

    private final String value;
    private DependencyCheck(@NonNls String value) { this.value = value; }
    public String getValue() { return value; }

    public static DependencyCheck fromDefault(@Nullable DefaultDependencyCheck def) {
      if (def == null) {
        return DEFAULT;
      }
      switch (def) {
        case ALL:
          return ALL;
        case NONE:
          return NONE;
        case OBJECTS:
          return OBJECTS;
        case SIMPLE:
          return SIMPLE;
        default:
          return DEFAULT;
      }
    }
}
