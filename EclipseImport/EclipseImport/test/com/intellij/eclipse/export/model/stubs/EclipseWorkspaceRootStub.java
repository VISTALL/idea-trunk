package com.intellij.eclipse.export.model.stubs;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class EclipseWorkspaceRootStub implements IWorkspaceRoot {
  private IPath location;
  private Map<IPath, IFile> files = new HashMap<IPath, IFile>();

  public IPath getLocation() {
    return location;
  }

  public void setLocation(IPath l) {
    location = l;
  }

  public boolean exists(IPath path) {
    return files.containsKey(path);
  }

  public IFile getFile(IPath path) {
    return files.get(path);
  }

  public void addFile(IPath path, IFile file) {
    files.put(path, file);
  }

  //
  // Unsupported operations
  //

  public void delete(boolean b, boolean b1, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IContainer[] findContainersForLocation(IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IContainer[] findContainersForLocationURI(URI uri) {
    throw new UnsupportedOperationException();
  }

  public IFile[] findFilesForLocation(IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IFile[] findFilesForLocationURI(URI uri) {
    throw new UnsupportedOperationException();
  }

  public IContainer getContainerForLocation(IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IFile getFileForLocation(IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IProject getProject(String string) {
    throw new UnsupportedOperationException();
  }

  public IProject[] getProjects() {
    throw new UnsupportedOperationException();
  }

  public IResource findMember(String string) {
    throw new UnsupportedOperationException();
  }

  public IResource findMember(String string, boolean b) {
    throw new UnsupportedOperationException();
  }

  public IResource findMember(IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IResource findMember(IPath iPath, boolean b) {
    throw new UnsupportedOperationException();
  }

  public String getDefaultCharset() throws CoreException {
    throw new UnsupportedOperationException();
  }

  public String getDefaultCharset(boolean b) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IFolder getFolder(IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IResource[] members() throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IResource[] members(boolean b) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IResource[] members(int i) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IFile[] findDeletedMembersWithHistory(int i, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setDefaultCharset(String string) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setDefaultCharset(String string, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void accept(IResourceProxyVisitor iResourceProxyVisitor, int i) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void accept(IResourceVisitor iResourceVisitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void accept(IResourceVisitor iResourceVisitor, int i, boolean b)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void accept(IResourceVisitor iResourceVisitor, int i, int i1) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void clearHistory(IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void copy(IPath iPath, boolean b, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void copy(IPath iPath, int i, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void copy(IProjectDescription iProjectDescription,
                   boolean b,
                   IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void copy(IProjectDescription iProjectDescription,
                   int i,
                   IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IMarker createMarker(String string) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IResourceProxy createProxy() {
    throw new UnsupportedOperationException();
  }

  public void delete(boolean b, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void delete(int i, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void deleteMarkers(String string, boolean b, int i) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public boolean exists() {
    throw new UnsupportedOperationException();
  }

  public IMarker findMarker(long l) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IMarker[] findMarkers(String string, boolean b, int i) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public String getFileExtension() {
    throw new UnsupportedOperationException();
  }

  public IPath getFullPath() {
    throw new UnsupportedOperationException();
  }

  public long getLocalTimeStamp() {
    throw new UnsupportedOperationException();
  }

  public URI getLocationURI() {
    throw new UnsupportedOperationException();
  }

  public IMarker getMarker(long l) {
    throw new UnsupportedOperationException();
  }

  public long getModificationStamp() {
    throw new UnsupportedOperationException();
  }

  public String getName() {
    throw new UnsupportedOperationException();
  }

  public IContainer getParent() {
    throw new UnsupportedOperationException();
  }

  public String getPersistentProperty(QualifiedName qualifiedName) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IProject getProject() {
    throw new UnsupportedOperationException();
  }

  public IPath getProjectRelativePath() {
    throw new UnsupportedOperationException();
  }

  public IPath getRawLocation() {
    throw new UnsupportedOperationException();
  }

  public URI getRawLocationURI() {
    throw new UnsupportedOperationException();
  }

  public ResourceAttributes getResourceAttributes() {
    throw new UnsupportedOperationException();
  }

  public Object getSessionProperty(QualifiedName qualifiedName) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public int getType() {
    throw new UnsupportedOperationException();
  }

  public IWorkspace getWorkspace() {
    throw new UnsupportedOperationException();
  }

  public boolean isAccessible() {
    throw new UnsupportedOperationException();
  }

  public boolean isDerived() {
    throw new UnsupportedOperationException();
  }

  public boolean isLocal(int i) {
    throw new UnsupportedOperationException();
  }

  public boolean isLinked() {
    throw new UnsupportedOperationException();
  }

  public boolean isLinked(int i) {
    throw new UnsupportedOperationException();
  }

  public boolean isPhantom() {
    throw new UnsupportedOperationException();
  }

  public boolean isReadOnly() {
    throw new UnsupportedOperationException();
  }

  public boolean isSynchronized(int i) {
    throw new UnsupportedOperationException();
  }

  public boolean isTeamPrivateMember() {
    throw new UnsupportedOperationException();
  }

  public void move(IPath iPath, boolean b, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void move(IPath iPath, int i, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void move(IProjectDescription iProjectDescription,
                   boolean b,
                   boolean b1,
                   IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void move(IProjectDescription iProjectDescription,
                   int i,
                   IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void refreshLocal(int i, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void revertModificationStamp(long l) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setDerived(boolean b) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setLocal(boolean b, int i, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public long setLocalTimeStamp(long l) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setPersistentProperty(QualifiedName qualifiedName, String string)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setReadOnly(boolean b) {
    throw new UnsupportedOperationException();
  }

  public void setResourceAttributes(ResourceAttributes resourceAttributes)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setSessionProperty(QualifiedName qualifiedName, Object object)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setTeamPrivateMember(boolean b) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void touch(IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public Object getAdapter(Class aClass) {
    throw new UnsupportedOperationException();
  }

  public boolean contains(ISchedulingRule iSchedulingRule) {
    throw new UnsupportedOperationException();
  }

  public boolean isConflicting(ISchedulingRule iSchedulingRule) {
    throw new UnsupportedOperationException();
  }
}
