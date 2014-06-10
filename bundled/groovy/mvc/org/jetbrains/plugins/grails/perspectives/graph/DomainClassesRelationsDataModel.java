/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails.perspectives.graph;

import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import org.jetbrains.plugins.grails.perspectives.create.CreateNewRelation;
import org.jetbrains.plugins.groovy.util.GroovyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class DomainClassesRelationsDataModel extends GraphDataModel<DomainClassNode, DomainClassRelationsInfo> {
  private Set<DomainClassNode> myNodes = new HashSet<DomainClassNode>();
  private Set<DomainClassRelationsInfo> myEdges = new HashSet<DomainClassRelationsInfo>();

  private final Project myProject;
  private final VirtualFile myDomainDirectory;

  private Map<DomainClassNode, List<DomainClassRelationsInfo>> myNodesToOutsMap;

  public DomainClassesRelationsDataModel(@NotNull VirtualFile domainDirectory, Project project) {
    myDomainDirectory = domainDirectory;
    myProject = project;
  }

  public Map<DomainClassNode, List<DomainClassRelationsInfo>> getNodesToOutsMap() {
    return myNodesToOutsMap;
  }

  @NotNull
  public Collection<DomainClassNode> getNodes() {
    refreshDataModel();
    return myNodes;
  }

  public void refreshDataModel() {
    clearAll();

    updateDataModel();
  }

  private void updateDataModel() {
    myNodesToOutsMap = DomainClassUtils.buildNodesAndEdges(myProject, myDomainDirectory);
    myNodes = myNodesToOutsMap.keySet();
    myEdges = new HashSet<DomainClassRelationsInfo>(GroovyUtils.flatten(myNodesToOutsMap.values()));
  }

  private void clearAll() {
    myNodes.clear();
    myEdges.clear();
  }

  @NotNull
  public Collection<DomainClassRelationsInfo> getEdges() {
    refreshDataModel();

    return myEdges;
  }

  @NotNull
  public DomainClassNode getSourceNode(DomainClassRelationsInfo domainClassRelationsInfo) {
    return domainClassRelationsInfo.getSource();
  }

  @NotNull
  public DomainClassNode getTargetNode(DomainClassRelationsInfo domainClassRelationsInfo) {
    return domainClassRelationsInfo.getTarget();
  }

  @NotNull
  public String getNodeName(DomainClassNode domainClassNode) {
    return domainClassNode.getUniqueName();
  }

  @NotNull
  public String getEdgeName(DomainClassRelationsInfo domainClassRelationsInfo) {
    return domainClassRelationsInfo.getEdgeLabel();
  }

  public DomainClassRelationsInfo createEdge(@NotNull DomainClassNode source, @NotNull DomainClassNode target) {
    final PsiClass targetClass = target.getTypeDefinition();
    final String targetQualifiedName = targetClass.getQualifiedName();

    final PsiClass psiClass = source.getTypeDefinition();
    final String sourceQualifiedName = psiClass.getQualifiedName();

    if (sourceQualifiedName != null && sourceQualifiedName.contains(".") && targetQualifiedName != null && !targetQualifiedName.contains(".")) {
      final int exitCode = Messages.showDialog(myProject, GrailsBundle.message("destination.class.cannot.be.resolved"), GrailsBundle.message("Warning"), new String[]{"OK", "Cancel"}, 1, Messages.getWarningIcon());

      if (exitCode == DialogWrapper.CANCEL_EXIT_CODE) return null;
    }

    CreateNewRelation dialogWrapper = new CreateNewRelation(source, target, myProject);
    dialogWrapper.show();

    DomainClassRelationsInfo.Relation relationType = dialogWrapper.getEdgeRelationType();
    if (relationType != null && DomainClassRelationsInfo.Relation.UNKNOWN != relationType) {
      myEdges.add(new DomainClassRelationsInfo(source, target, relationType));
    }

    return new DomainClassRelationsInfo(source, target, relationType);
  }

  public void dispose() {
  }

  public Project getProject() {
    return myProject;
  }
}
