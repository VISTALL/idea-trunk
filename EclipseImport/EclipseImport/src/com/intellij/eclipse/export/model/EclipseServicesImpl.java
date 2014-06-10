package com.intellij.eclipse.export.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.service.datalocation.Location;

public class EclipseServicesImpl {
  public IPath getEclipseInstallationPath() {
    Location location = Platform.getInstallLocation();
    if (location == null) return null;

    return new Path(location.getURL().getPath());
  }

  public IPath getResolvedVariablePath(IPath path) {
    return JavaCore.getResolvedVariablePath(path);
  }
}
