package com.advancedtools.webservices.wsengine;

import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
* User: Maxim
* Date: Oct 23, 2006
* Time: 10:10:04 AM
* To change this template use File | Settings | File Templates.
*/
public class LibraryInfo implements ExternalEngine.LibraryDescriptor {
  private String name;
  private String[] libJars;
  private boolean toIncludeInJavaEEContainerDeployment;

  public String getName() {
    return name;
  }

  public String[] getLibJars() {
    return libJars;
  }

  public boolean isToIncludeInJavaEEContainerDeployment() {
    return toIncludeInJavaEEContainerDeployment;
  }

  public LibraryInfo(String _name, @NonNls String[] _libJars) {
    this(_name, _libJars, true);
  }

  public LibraryInfo(String _name, @NonNls String[] _libJars, boolean includeInDeployment) {
    libJars = _libJars;
    name = _name;
    toIncludeInJavaEEContainerDeployment = includeInDeployment;
  }

  public LibraryInfo(String _name, @NonNls String _libJar) {
    this( _name, new String[] { _libJar }, true );
  }

  public void appendJars(@NonNls String[] jars) {
    libJars = ArrayUtil.mergeArrays(libJars, jars,String.class);
  }
}
