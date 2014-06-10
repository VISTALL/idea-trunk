/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.editor;

import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.javaee.CommonModelManager;
import com.intellij.javaee.model.xml.ejb.EjbBase;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import com.intellij.javaee.module.view.ejb.editor.EjbAsVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import com.intellij.util.xml.ui.PerspectiveFileEditorProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JavaeeBeanProvider extends PerspectiveFileEditorProvider {

    private final double weight;

    protected JavaeeBeanProvider(double weight) {
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @SuppressWarnings({"ChainOfInstanceofChecks", "InstanceofIncompatibleInterface", "CastToIncompatibleInterface"})
    public final boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        boolean accept = false;
        if (file instanceof EjbAsVirtualFile) {
            EjbBase bean = CommonModelManager.getInstance().getDomElement(((EjbAsVirtualFile) file).findElement(project));
            if (bean instanceof SessionBean) {
                accept = acceptSessionBean((SessionBean) bean);
            } else if (bean instanceof EntityBean) {
                accept = acceptEntityBean((EntityBean) bean);
            } else if (bean instanceof MessageDrivenBean) {
                accept = acceptMessageBean((MessageDrivenBean) bean);
            }
        }
        return accept;
    }

    @Override
    @NotNull
    @SuppressWarnings({"ChainOfInstanceofChecks", "InstanceofIncompatibleInterface", "CastToIncompatibleInterface"})
    public final PerspectiveFileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        PerspectiveFileEditor editor = null;
        if (file instanceof EjbAsVirtualFile) {
            EjbBase bean = CommonModelManager.getInstance().getDomElement(((EjbAsVirtualFile) file).findElement(project));
            if (bean instanceof SessionBean) {
                editor = createSessionBeanEditor(project, file, (SessionBean) bean);
            } else if (bean instanceof EntityBean) {
                editor = createEntityBeanEditor(project, file, (EntityBean) bean);
            } else if (bean instanceof MessageDrivenBean) {
                editor = createMessageBeanEditor(project, file, (MessageDrivenBean) bean);
            }
        }
        if (editor == null) {
            editor = empty(project, file);
        }
        return editor;
    }

    protected boolean acceptEntityBean(@NotNull EntityBean bean) {
        return false;
    }

    protected boolean acceptSessionBean(@NotNull SessionBean bean) {
        return false;
    }

    protected boolean acceptMessageBean(@NotNull MessageDrivenBean bean) {
        return false;
    }

    @Nullable
    protected PerspectiveFileEditor createEntityBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull EntityBean bean) {
        return empty(project, file);
    }

    @Nullable
    protected PerspectiveFileEditor createSessionBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull SessionBean bean) {
        return empty(project, file);
    }

    @Nullable
    protected PerspectiveFileEditor createMessageBeanEditor(@NotNull Project project, @NotNull VirtualFile file, @NotNull MessageDrivenBean bean) {
        return empty(project, file);
    }

    @NotNull
    private PerspectiveFileEditor empty(@NotNull Project project, @NotNull VirtualFile file) {
        JavaeeIntegration integration = JavaeeIntegration.getInstance();
        return new JavaeeEmptyEditor(project, file, integration.getPresentableName(), integration.getBigIcon());
    }
}
