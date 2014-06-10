package com.intellij.eclipse.export.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Resource {
  private IPath path;
  private boolean isVariable;

  public Resource(IPath path) {
    this(path, false);
  }

  public Resource(IPath path, boolean isVariable) {
    this.path = path;
    this.isVariable = isVariable;
  }

  public boolean isVariable() {
    return isVariable || isEclipseLibrary();
  }

  public boolean isEclipseLibrary() {
    // todo factor out this method
    IPath eclipseLocation = EclipseServices.getEclipseInstallationLocation();

    if (eclipseLocation == null) return false;

    if (!(path.getDevice() == null
          ? eclipseLocation.getDevice() == null
          : path.getDevice().equals(eclipseLocation.getDevice()))) return false;

    return path.matchingFirstSegments(eclipseLocation) == eclipseLocation.segmentCount();
  }

  public String getAbsolutePath() {
    IPath result = path;
    if (isVariable) {
      IPath resolvedPath = EclipseServices.getResolvedVariablePath(path);
      if (resolvedPath != null) result = resolvedPath;
    }
    return result.toString();
  }

  public String getVariablePath() {
    if (isEclipseLibrary()) return withEclipsePathVariable();
    if (!isVariable || path.segmentCount() == 0) return path.toString();

    IPath head = new Path("$" + getVariableName() + "$");
    IPath tail = path.removeFirstSegments(1);

    return head.append(tail).toString();
  }

  private String withEclipsePathVariable() {
    if (!isEclipseLibrary()) return getAbsolutePath();

    int segmentsToRemove = EclipseServices.getEclipseInstallationLocation().segmentCount();

    IPath head = new Path("$" + getVariableName() + "$");
    IPath tail = path.setDevice(null).removeFirstSegments(segmentsToRemove);

    return head.append(tail).toString();
  }

  public String getVariableName() {
    if (isEclipseLibrary()) return "ECLIPSE_HOME";
    return path.segment(0);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o.getClass() != getClass()) return false;

    return ((Resource)o).path.equals(path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
