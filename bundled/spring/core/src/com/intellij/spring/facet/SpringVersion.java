/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.facet.ui.libraries.LibraryInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createMavenJarInfo;
import static com.intellij.facet.ui.libraries.MavenLibraryUtil.createSubMavenJarInfo;

/**
 * @author Dmitry Avdeev
 */
public enum SpringVersion {

  Spring1_2("1.2.9", new LibraryInfo[] {
    createSubMavenJarInfo("/org/springframework/", "spring", "1.2.9", "org.springframework.core.SpringVersion"),
    createMavenJarInfo("commons-logging", "1.0.4", "org.apache.commons.logging.Log"),
  }, null),

  Spring2_0("2.0.8", new LibraryInfo[] {
    createSubMavenJarInfo("/org/springframework/", "spring", "2.0.8", "org.springframework.core.SpringVersion"),
    createMavenJarInfo("commons-logging", "1.1", "org.apache.commons.logging.Log"),
  }, null),

  Spring2_5("2.5.5", new LibraryInfo[] {
    createSubMavenJarInfo("/org/springframework/", "spring", "2.5.5", "org.springframework.core.SpringVersion"),
    createMavenJarInfo("commons-logging", "1.1.1", "org.apache.commons.logging.Log"),
  },
  createSubMavenJarInfo("/org/springframework/", "spring-webmvc", "2.5.5", "org.springframework.web.servlet.DispatcherServlet")
  );

  private final String myName;
  private final LibraryInfo[] myJars;
  private final LibraryInfo myMvcJars;

  private SpringVersion(String name, LibraryInfo[] jars, final LibraryInfo mvcJars) {

    myName = name;
    myJars = jars;
    myMvcJars = mvcJars;
  }

  public String getName() {
    return myName;
  }

  public LibraryInfo[] getJars() {
    return myJars;
  }

  public LibraryInfo getMvcJars() {
    return myMvcJars;
  }

  public String toString() {
    return myName;
  }
}
