package com.intellij.eclipse.export.model.stubs;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

public class EclipseWorkspaceStub implements IWorkspace {
  private EclipseWorkspaceRootStub root = new EclipseWorkspaceRootStub();

  public EclipseWorkspaceRootStub getRoot() {
    return root;
  }

  //
  // Unsupported operations
  //

  public void addResourceChangeListener(IResourceChangeListener iResourceChangeListener) {
    throw new UnsupportedOperationException();
  }

  public void addResourceChangeListener(IResourceChangeListener iResourceChangeListener,
                                        int i) {
    throw new UnsupportedOperationException();
  }

  public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant iSaveParticipant)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void build(int i, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void checkpoint(boolean b) {
    throw new UnsupportedOperationException();
  }

  public IProject[][] computePrerequisiteOrder(IProject[] iProjects) {
    throw new UnsupportedOperationException();
  }

  public ProjectOrder computeProjectOrder(IProject[] iProjects) {
    throw new UnsupportedOperationException();
  }

  public IStatus copy(IResource[] iResources,
                      IPath iPath,
                      boolean b,
                      IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IStatus copy(IResource[] iResources,
                      IPath iPath,
                      int i,
                      IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IStatus delete(IResource[] iResources, boolean b, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IStatus delete(IResource[] iResources, int i, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void deleteMarkers(IMarker[] iMarkers) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void forgetSavedTree(String string) {
    throw new UnsupportedOperationException();
  }

  public IProjectNatureDescriptor[] getNatureDescriptors() {
    throw new UnsupportedOperationException();
  }

  public IProjectNatureDescriptor getNatureDescriptor(String string) {
    throw new UnsupportedOperationException();
  }

  public Map getDanglingReferences() {
    throw new UnsupportedOperationException();
  }

  public IWorkspaceDescription getDescription() {
    throw new UnsupportedOperationException();
  }

  public IResourceRuleFactory getRuleFactory() {
    throw new UnsupportedOperationException();
  }

  public ISynchronizer getSynchronizer() {
    throw new UnsupportedOperationException();
  }

  public boolean isAutoBuilding() {
    throw new UnsupportedOperationException();
  }

  public boolean isTreeLocked() {
    throw new UnsupportedOperationException();
  }

  public IProjectDescription loadProjectDescription(IPath iPath) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IProjectDescription loadProjectDescription(InputStream inputStream)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IStatus move(IResource[] iResources,
                      IPath iPath,
                      boolean b,
                      IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IStatus move(IResource[] iResources,
                      IPath iPath,
                      int i,
                      IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IProjectDescription newProjectDescription(String string) {
    throw new UnsupportedOperationException();
  }

  public void removeResourceChangeListener(IResourceChangeListener iResourceChangeListener) {
    throw new UnsupportedOperationException();
  }

  public void removeSaveParticipant(Plugin plugin) {
    throw new UnsupportedOperationException();
  }

  public void run(IWorkspaceRunnable iWorkspaceRunnable,
                  ISchedulingRule iSchedulingRule,
                  int i,
                  IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void run(IWorkspaceRunnable iWorkspaceRunnable, IProgressMonitor iProgressMonitor)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public IStatus save(boolean b, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setDescription(IWorkspaceDescription iWorkspaceDescription)
    throws CoreException {
    throw new UnsupportedOperationException();
  }

  public void setWorkspaceLock(WorkspaceLock workspaceLock) {
    throw new UnsupportedOperationException();
  }

  public String[] sortNatureSet(String[] strings) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateEdit(IFile[] iFiles, Object object) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateLinkLocation(IResource iResource, IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateLinkLocationURI(IResource iResource, URI uri) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateName(String string, int i) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateNatureSet(String[] strings) {
    throw new UnsupportedOperationException();
  }

  public IStatus validatePath(String string, int i) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateProjectLocation(IProject iProject, IPath iPath) {
    throw new UnsupportedOperationException();
  }

  public IStatus validateProjectLocationURI(IProject iProject, URI uri) {
    throw new UnsupportedOperationException();
  }

  public IPathVariableManager getPathVariableManager() {
    throw new UnsupportedOperationException();
  }

  public Object getAdapter(Class aClass) {
    throw new UnsupportedOperationException();
  }
}
