/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package org.jbez.modelgen;

import java.util.Map;
import java.util.TreeMap;

public class TypeDesc {
  public enum TypeEnum {
    CLASS, ENUM, GROUP_INTERFACE
  }

  public TypeDesc(String xsName, String xsNamespace, String name, TypeEnum type) {
    this.xsName = xsName;
    this.xsNamespace = xsNamespace;
    this.name = name;
    this.type = type;
  }

  TypeEnum type;
  final String xsName;
  final String xsNamespace;
  final String name;
  final Map<String, FieldDesc> fdMap = new TreeMap<String, FieldDesc>();
  boolean duplicates;
  String documentation;
  TypeDesc[] supers;

  public String toString() {
    return (type == TypeEnum.ENUM ? "enum" : "type") + ": " + name + ";" + xsName + ";";
  }
}
