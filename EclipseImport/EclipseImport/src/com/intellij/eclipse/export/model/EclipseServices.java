package com.intellij.eclipse.export.model;

import org.eclipse.core.runtime.IPath;

// todo try to remove this class
public class EclipseServices {
  private static EclipseServicesImpl impl = new EclipseServicesImpl();

  public static void setImpl(EclipseServicesImpl i) {
    impl = i;
  }

  public static IPath getEclipseInstallationLocation() {
    return impl.getEclipseInstallationPath();
  }

  public static IPath getResolvedVariablePath(IPath path) {
    return impl.getResolvedVariablePath(path);
  }
}
