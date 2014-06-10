package com.intellij.eclipse.export.model.stubs;

import com.intellij.eclipse.export.model.EclipseServicesImpl;
import org.eclipse.core.runtime.IPath;

import java.util.HashMap;
import java.util.Map;

public class EclipseServicesImplStub extends EclipseServicesImpl {
  private IPath eclipseInstallationDirectory;
  private Map<IPath, IPath> resolvedVariablePaths = new HashMap<IPath, IPath>();

  @Override
  public IPath getEclipseInstallationPath() {
    return eclipseInstallationDirectory;
  }

  public void setEclipseInstallationLocation(IPath l) {
    eclipseInstallationDirectory = l;
  }

  @Override
  public IPath getResolvedVariablePath(IPath path) {
    return resolvedVariablePaths.get(path);
  }

  public void addResolvedVariablePath(IPath variablePath, IPath resolvedPath) {
    resolvedVariablePaths.put(variablePath, resolvedPath);
  }
}
