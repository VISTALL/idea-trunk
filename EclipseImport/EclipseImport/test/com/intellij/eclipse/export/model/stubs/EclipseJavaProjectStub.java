package com.intellij.eclipse.export.model.stubs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.eval.IEvaluationContext;

import java.util.Map;

public class EclipseJavaProjectStub implements IJavaProject {
  private IClasspathEntry[] classpaths = new IClasspathEntry[0];

  public IClasspathEntry[] getRawClasspath() throws JavaModelException {
    return classpaths;
  }

  public void setRawClasspath(IClasspathEntry[] c) {
    classpaths = c;
  }

  //
  // Unsupported operations
  //

  public IClasspathEntry decodeClasspathEntry(String string) {
    throw new UnsupportedOperationException();
  }

  public String encodeClasspathEntry(IClasspathEntry entry) {
    throw new UnsupportedOperationException();
  }

  public IJavaElement findElement(IPath iPath) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IJavaElement findElement(IPath iPath, WorkingCopyOwner workingCopyOwner)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPackageFragment findPackageFragment(IPath iPath) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot findPackageFragmentRoot(IPath iPath) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry) {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string, IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string, WorkingCopyOwner workingCopyOwner)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string,
                        WorkingCopyOwner workingCopyOwner,
                        IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string, String string1) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string, String string1, IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string, String string1, WorkingCopyOwner workingCopyOwner)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IType findType(String string,
                        String string1,
                        WorkingCopyOwner workingCopyOwner,
                        IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot[] getAllPackageFragmentRoots() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public Object[] getNonJavaResources() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public String getOption(String string, boolean b) {
    throw new UnsupportedOperationException();
  }

  public Map getOptions(boolean b) {
    throw new UnsupportedOperationException();
  }

  public IPath getOutputLocation() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot getPackageFragmentRoot(String string) {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot getPackageFragmentRoot(IResource iResource) {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot[] getPackageFragmentRoots() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
    throw new UnsupportedOperationException();
  }

  public IPackageFragment[] getPackageFragments() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IProject getProject() {
    throw new UnsupportedOperationException();
  }

  public String[] getRequiredProjectNames() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IClasspathEntry[] getResolvedClasspath(boolean b) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean hasBuildState() {
    throw new UnsupportedOperationException();
  }

  public boolean hasClasspathCycle(IClasspathEntry[] iClasspathEntries) {
    throw new UnsupportedOperationException();
  }

  public boolean isOnClasspath(IJavaElement iJavaElement) {
    throw new UnsupportedOperationException();
  }

  public boolean isOnClasspath(IResource iResource) {
    throw new UnsupportedOperationException();
  }

  public IEvaluationContext newEvaluationContext() {
    throw new UnsupportedOperationException();
  }

  public ITypeHierarchy newTypeHierarchy(IRegion iRegion, IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public ITypeHierarchy newTypeHierarchy(IRegion iRegion,
                                         WorkingCopyOwner workingCopyOwner,
                                         IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public ITypeHierarchy newTypeHierarchy(IType iType,
                                         IRegion iRegion,
                                         IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public ITypeHierarchy newTypeHierarchy(IType iType,
                                         IRegion iRegion,
                                         WorkingCopyOwner workingCopyOwner,
                                         IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IPath readOutputLocation() {
    throw new UnsupportedOperationException();
  }

  public IClasspathEntry[] readRawClasspath() {
    throw new UnsupportedOperationException();
  }

  public void setOption(String string, String string1) {
    throw new UnsupportedOperationException();
  }

  public void setOptions(Map map) {
    throw new UnsupportedOperationException();
  }

  public void setOutputLocation(IPath iPath, IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public void setRawClasspath(IClasspathEntry[] iClasspathEntries,
                              IPath iPath,
                              boolean b,
                              IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public void setRawClasspath(IClasspathEntry[] iClasspathEntries,
                              boolean b,
                              IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public void setRawClasspath(IClasspathEntry[] iClasspathEntries,
                              IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public void setRawClasspath(IClasspathEntry[] iClasspathEntries,
                              IPath iPath,
                              IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IJavaElement[] getChildren() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean hasChildren() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean exists() {
    throw new UnsupportedOperationException();
  }

  public IJavaElement getAncestor(int i) {
    throw new UnsupportedOperationException();
  }

  public String getAttachedJavadoc(IProgressMonitor iProgressMonitor)
    throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IResource getCorrespondingResource() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public String getElementName() {
    throw new UnsupportedOperationException();
  }

  public int getElementType() {
    throw new UnsupportedOperationException();
  }

  public String getHandleIdentifier() {
    throw new UnsupportedOperationException();
  }

  public IJavaModel getJavaModel() {
    throw new UnsupportedOperationException();
  }

  public IJavaProject getJavaProject() {
    throw new UnsupportedOperationException();
  }

  public IOpenable getOpenable() {
    throw new UnsupportedOperationException();
  }

  public IJavaElement getParent() {
    throw new UnsupportedOperationException();
  }

  public IPath getPath() {
    throw new UnsupportedOperationException();
  }

  public IJavaElement getPrimaryElement() {
    throw new UnsupportedOperationException();
  }

  public IResource getResource() {
    throw new UnsupportedOperationException();
  }

  public ISchedulingRule getSchedulingRule() {
    throw new UnsupportedOperationException();
  }

  public IResource getUnderlyingResource() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean isReadOnly() {
    throw new UnsupportedOperationException();
  }

  public boolean isStructureKnown() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public Object getAdapter(Class aClass) {
    throw new UnsupportedOperationException();
  }

  public void close() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public String findRecommendedLineSeparator() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public IBuffer getBuffer() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean hasUnsavedChanges() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean isConsistent() throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public boolean isOpen() {
    throw new UnsupportedOperationException();
  }

  public void makeConsistent(IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public void open(IProgressMonitor iProgressMonitor) throws JavaModelException {
    throw new UnsupportedOperationException();
  }

  public void save(IProgressMonitor iProgressMonitor, boolean b) throws JavaModelException {
    throw new UnsupportedOperationException();
  }
}
