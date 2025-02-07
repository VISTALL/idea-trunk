/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
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

package org.jetbrains.plugins.ruby.ruby.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.rails.nameConventions.ModelsConventions;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RClassPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RPresentationConstants;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Oct 30, 2007
 */
public class RClassNode extends ProjectViewNode<RVirtualClass> {
    private final Icon myIcon;
    private final VirtualFile myVirtualFile;

    public RClassNode(@NotNull final Project project,
                      @Nullable final Module module,
                      @NotNull final RVirtualFile file,
                      @NotNull final RVirtualClass value,
                      @NotNull final List<RVirtualClass> classes,
                      final ViewSettings viewSettings) {
        super(project, value, viewSettings);
        myVirtualFile = file.getVirtualFile();
        // Rails checks
        if (module != null && RailsFacetUtil.hasRailsSupport(module)) {
            if (ControllersConventions.isControllerFile(file, module, classes)){
                myIcon = RailsIcons.RAILS_CONTROLLER_NODE;
                return;
            }
            if (ModelsConventions.isModelFile(file, module, classes)){
                myIcon = RailsIcons.RAILS_MODEL_NODE;
                return;
            }
        }
        myIcon = RClassPresentationUtil.getIcon();
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return myVirtualFile;
    }

    public boolean contains(@NotNull VirtualFile file) {
        return false;
    }

    @NotNull
    public Collection<AbstractTreeNode> getChildren() {
        return Collections.emptyList();
    }

    protected void update(PresentationData data) {
        final RVirtualClass aClass = getValue();
        data.setIcons(myIcon);
        data.setLocationString(RClassPresentationUtil.getLocation(aClass));
        data.setPresentableText(RClassPresentationUtil.formatName(aClass, RPresentationConstants.SHOW_NAME));
    }

    public void navigate(boolean requestFocus) {
        ((Navigatable)getPsiElement()).navigate(requestFocus);
    }

    public boolean canNavigate() {
        return canNavigateToSource();
    }

    public boolean canNavigateToSource() {
        return getPsiElement() instanceof Navigatable;
    }

    public RStructuralElement getPsiElement(){
        final RVirtualClass element = getValue();
        return RVirtualPsiUtil.findInPsi(myProject, element);
    }

    public boolean canRepresent(Object element) {
        final RStructuralElement psiElement = getPsiElement();
        return psiElement!=null && RubyPsiUtil.getRFile(psiElement)  == element;
    }
}
