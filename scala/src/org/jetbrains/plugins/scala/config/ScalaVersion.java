package org.jetbrains.plugins.scala.config;

import com.intellij.facet.ui.libraries.LibraryInfo;
import org.jetbrains.annotations.NonNls;
import static org.jetbrains.plugins.scala.config.util.ScalaMavenLibraryUtil.createJarDownloadInfo;


/**
 * @author ilyas
 */
public enum ScalaVersion {

  Scala2_7_1("2.7.1", new LibraryInfo[]{
      createJarDownloadInfo("scala-compiler", "2.7.1", "org/scala-lang", "scala.tools.nsc.CompilerRun"),
      createJarDownloadInfo("scala-library", "2.7.1", "org/scala-lang", "scala.Predef"),
  }),

  Scala2_7_2("2.7.2", new LibraryInfo[]{
      createJarDownloadInfo("scala-compiler", "2.7.2", "org/scala-lang", "scala.tools.nsc.CompilerRun"),
      createJarDownloadInfo("scala-library", "2.7.2", "org/scala-lang", "scala.Predef"),
  }),

  Scala2_7_3("2.7.3", new LibraryInfo[]{
      createJarDownloadInfo("scala-compiler", "2.7.3", "org/scala-lang", "scala.tools.nsc.CompilerRun"),
      createJarDownloadInfo("scala-library", "2.7.3", "org/scala-lang", "scala.Predef"),
  })
  ,

  Scala2_7_4("2.7.4", new LibraryInfo[]{
      createJarDownloadInfo("scala-compiler", "2.7.4", "org/scala-lang", "scala.tools.nsc.CompilerRun"),
      createJarDownloadInfo("scala-library", "2.7.4", "org/scala-lang", "scala.Predef"),
  })
  ,

  Scala2_7_5("2.7.5", new LibraryInfo[]{
      createJarDownloadInfo("scala-compiler", "2.7.5", "org/scala-lang", "scala.tools.nsc.CompilerRun"),
      createJarDownloadInfo("scala-library", "2.7.5", "org/scala-lang", "scala.Predef"),
  })
  ,

  Scala2_7_6("2.7.6", new LibraryInfo[]{
      createJarDownloadInfo("scala-compiler", "2.7.6", "org/scala-lang", "scala.tools.nsc.CompilerRun"),
      createJarDownloadInfo("scala-library", "2.7.6", "org/scala-lang", "scala.Predef"),
  })
  ;

  private final String myName;
  private final LibraryInfo[] myJars;

  private ScalaVersion(@NonNls String name, LibraryInfo[] infos) {
    myName = name;
    myJars = infos;
  }

  public LibraryInfo[] getJars() {
    return myJars;
  }

  public String toString() {
    return myName;
  }

}