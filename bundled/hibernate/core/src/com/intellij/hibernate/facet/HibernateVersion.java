package com.intellij.hibernate.facet;

import com.intellij.facet.ui.libraries.LibraryInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createMavenJarInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createSubMavenJarInfo;
import org.jetbrains.annotations.NonNls;

/**
 * @author Gregory.Shrago
 */
public enum HibernateVersion {

  Hibernate_3_2_0("3.2", new LibraryInfo[] {
    createSubMavenJarInfo("/org/hibernate", "hibernate", "3.2.6.ga", "org.hibernate.Session"),
    createSubMavenJarInfo("/org/hibernate", "hibernate-annotations", "3.2.1.ga", "org.hibernate.cfg.AnnotationConfiguration"),
    createMavenJarInfo("commons-collections", "2.1.1", "org.apache.commons.collections.Factory"),
    createMavenJarInfo("commons-logging", "1.0.4", "org.apache.commons.logging.Log"),
    createMavenJarInfo("asm", "1.5.3", "org.objectweb.asm.ClassWriter"),
    createMavenJarInfo("cglib", "2.1_3", "net.sf.cglib.proxy.Enhancer"),
    createMavenJarInfo("dom4j", "1.6.1", "org.dom4j.DocumentException"),
    createMavenJarInfo("antlr", "2.7.6", "antlr.ANTLRException"),
  }, "hibernate-main-3.0.java");



  private final String myName;
  private final LibraryInfo[] myJars;
  private final String myMainTemplateName;

  private HibernateVersion(String name, LibraryInfo[] jars, final @NonNls String mainTemplateName) {
    myName = name;
    myJars = jars;
    myMainTemplateName = mainTemplateName;
  }

  public String getName() {
    return myName;
  }

  public LibraryInfo[] getJars() {
    return myJars;
  }

  public String toString() {
    return myName;
  }

  public String getMainTemplateName() {
    return myMainTemplateName;
  }

}
