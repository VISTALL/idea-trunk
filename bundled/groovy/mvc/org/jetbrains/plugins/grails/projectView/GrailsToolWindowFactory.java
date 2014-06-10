/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.plugins.grails.projectView;

import org.jetbrains.plugins.groovy.mvc.projectView.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.GroovyIcons;
import org.jetbrains.plugins.grails.config.GrailsFramework;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.groovy.mvc.projectView.DomainClassNode;
import org.jetbrains.plugins.grails.projectView.v2.nodes.leafs.ControllerClassNode;
import org.jetbrains.plugins.grails.projectView.v2.nodes.leafs.ViewNode;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.mvc.projectView.TestsTopLevelDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.Icons;

import javax.swing.*;
import java.util.List;

/**
 * @author peter
 */
public class GrailsToolWindowFactory extends MvcToolWindowDescriptor {
  public GrailsToolWindowFactory() {
    super(GrailsFramework.INSTANCE);
  }

  @Override
  public void fillModuleChildren(List<AbstractTreeNode> result, Module module, ViewSettings viewSettings, VirtualFile root) {
    final Project project = module.getProject();
    final PsiDirectory domains = findDirectory(project, root, "grails-app/domain");
    if (domains != null) {
      result.add(new TopLevelDirectoryNode(module, domains, viewSettings, "Domain classes", GrailsIcons.GRAILS_DOMAIN_CLASSES_FOLDER_NODE, MvcNodeDescriptor.SortInfo.DOMAIN_CLASSES_FOLDER) {
        @Override
        protected AbstractTreeNode createClassNode(final GrTypeDefinition typeDefinition) {
            if (GrailsUtils.isDomainClass(typeDefinition.getContainingFile().getVirtualFile(), getModule())) {
            return new DomainClassNode(getModule(), typeDefinition, getSettings());
          }
          return super.createClassNode(typeDefinition);
        }
      });
    }

    final PsiDirectory conf = findDirectory(project, root, "grails-app/conf");
    if (conf != null) {
      result.add(new TopLevelDirectoryNode(module, conf, viewSettings, "Configuration", GrailsIcons.GRAILS_CONFIG_FOLDER_NODE, MvcNodeDescriptor.SortInfo.CONFIG_FOLDER));
    }

    // Domain classes
    //VirtualFile root;

    final PsiDirectory controllers = findDirectory(project, root, "grails-app/controllers");
    if (controllers != null) {
      result.add(new TopLevelDirectoryNode(module, controllers, viewSettings, "Controllers", GrailsIcons.GRAILS_CONTROLERS_FOLDER_NODES, MvcNodeDescriptor.SortInfo.CONTROLLERS_FOLDER) {
        @Override
        protected AbstractTreeNode createClassNode(final GrTypeDefinition typeDefinition) {
          final Module module = getModule();

          if (GrailsController.fromClass(typeDefinition) != null) {
            return new ControllerClassNode(module, typeDefinition, getSettings());
          }
          return new ClassNode(module, typeDefinition, getValue().getLocationRootMark(), getSettings());
        }

      });
    }

    final PsiDirectory taglib = findDirectory(project, root, "grails-app/taglib");
    if (taglib != null) {
      result.add(new TopLevelDirectoryNode(module, taglib, viewSettings, "Taglibs", GrailsIcons.TAG_LIB, MvcNodeDescriptor.SortInfo.TAGLIB_FOLDER));
    }

    final PsiDirectory services = findDirectory(project, root, "grails-app/services");
    if (services != null) {
      result.add(new TopLevelDirectoryNode(module, services, viewSettings, "Services", GrailsIcons.SERVICE, MvcNodeDescriptor.SortInfo.SERVICES_FOLDER));
    }

    final PsiDirectory views = findDirectory(project, root, "views");
    if (views != null) {
      result.add(new TopLevelDirectoryNode(module, views, viewSettings, "Views", GrailsIcons.GSP_FILE_TYPE, MvcNodeDescriptor.SortInfo.VIEWS_FOLDER) {
        @Override
        protected void processNotDirectoryFile(List<AbstractTreeNode> nodes, PsiFile file) {
          if (GspFileType.GSP_FILE_TYPE.equals(file.getFileType())) {
            nodes.add(new ViewNode(getModule(), file, getSettings(), NodeId.VIEWS_SUBTREE));
          }
        }

      });
    }
    final PsiDirectory webApp = findDirectory(project, root, "web-app");
    if (webApp != null) {
      result.add(new TopLevelDirectoryNode(module, webApp, viewSettings, "web-app", Icons.WEB_FOLDER_OPEN, MvcNodeDescriptor.SortInfo.WEB_APP_FOLDER));
    }

    final PsiDirectory srcGroovy = findDirectory(project, root, "src/groovy");
    if (srcGroovy != null) {
      result.add(new TopLevelDirectoryNode(module, srcGroovy, viewSettings, "Groovy Sources", GroovyIcons.GROOVY_ICON_16x16, MvcNodeDescriptor.SortInfo.SRC_FOLDERS));
    }

    final PsiDirectory srcJava = findDirectory(project, root, "src/java");
    if (srcJava != null) {
      result.add(new TopLevelDirectoryNode(module, srcJava, viewSettings, "Java Sources", StdFileTypes.JAVA.getIcon(), MvcNodeDescriptor.SortInfo.SRC_FOLDERS));
    }

    final PsiDirectory testsUnit = findDirectory(project, root, "test/unit");
    if (testsUnit != null) {
      result.add(new TestsTopLevelDirectoryNode(module, testsUnit, viewSettings, "Unit Tests", Icons.TEST_SOURCE_FOLDER));
    }

    final PsiDirectory testsIntegration = findDirectory(project, root, "test/integration");
    if (testsIntegration != null) {
      result.add(new TestsTopLevelDirectoryNode(module, testsIntegration, viewSettings, "Integration Tests", GrailsIcons.GRAILS_TEST_RUN_CONFIGURATION));
    }
  }

  @Override
  public Icon getModuleNodeIcon() {
    return GrailsIcons.GRAILS_APP;
  }

}
