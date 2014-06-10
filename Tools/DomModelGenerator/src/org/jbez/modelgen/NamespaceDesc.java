/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package org.jbez.modelgen;

import java.util.Map;
import java.util.HashMap;

public class NamespaceDesc {
  public NamespaceDesc(String name,
                       String pkgName,
                       String superClass,
                       String prefix,
                       String factoryClass,
                       String helperClass,
                       String imports,
                       String intfs) {
    this.name = name;
    this.pkgName = pkgName;
    this.superClass = superClass;
    this.prefix = prefix;
    this.factoryClass = factoryClass;
    this.helperClass = helperClass;
    this.imports = imports;
    this.intfs = intfs;
  }

  public NamespaceDesc(String name, NamespaceDesc def) {
    this.name = name;
    this.pkgName = def.pkgName;
    this.superClass = def.superClass;
    this.prefix = def.prefix;
    this.factoryClass = def.factoryClass;
    this.helperClass = def.helperClass;
    this.imports = def.imports;
    this.intfs = def.intfs;
  }

  final Map<String, String> props = new HashMap<String, String>();
  final String name;
  String pkgName;
  String superClass;
  String prefix;
  String factoryClass;
  String helperClass;
  String imports;
  String intfs;
  boolean skip;
  String[] pkgNames;
  String enumPkg;


  public String toString() {
    return "NS:"+name+" "+(skip?"skip":"")+pkgName;
  }
}
