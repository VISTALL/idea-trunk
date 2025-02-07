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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.modules;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.cache.info.RFileInfo;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualName;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualContainer;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualModule;
import org.jetbrains.plugins.ruby.ruby.cache.psi.impl.RVirtualModuleImpl;
import org.jetbrains.plugins.ruby.ruby.cache.psi.impl.RVirtualNameImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RModuleName;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.RFieldConstantContainerImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import org.jetbrains.plugins.ruby.ruby.presentation.RModulePresentationUtil;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 11.06.2006
 */
public class RModuleImpl extends RFieldConstantContainerImpl implements RModule {

    public RModuleImpl(ASTNode astNode) {
        super(astNode);
    }

    public void accept(@NotNull PsiElementVisitor visitor){
        if (visitor instanceof RubyElementVisitor){
            ((RubyElementVisitor) visitor).visitRModule(this);
            return;
        }
        super.accept(visitor);
    }

    @NotNull
    public ItemPresentation getPresentation() {
        return RModulePresentationUtil.getPresentation(this);
    }

    @Nullable
    public Icon getIcon(final int flags) {
        return RModulePresentationUtil.getIcon(this, flags);
    }

    @Nullable
    public RModuleName getModuleName() {
        return getChildByType(RModuleName.class, 0);
    }
    
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        return null;
    }

    @NotNull
    public RVirtualModule createVirtualCopy(@Nullable final RVirtualContainer virtualParent,
                                               @NotNull final RFileInfo info) {
        final RVirtualName virtualModuleName = new RVirtualNameImpl(getFullPath(), isGlobal());
        assert virtualParent != null;

        final RVirtualModuleImpl vModule = new RVirtualModuleImpl(virtualParent, virtualModuleName,
                                       getAccessModifier(), info);
        addVirtualData(vModule, info);
        return vModule;
    }

    public int getTextOffset() {
        final RModuleName moduleName = getModuleName();
        return moduleName!=null ? moduleName.getTextOffset() : super.getTextOffset();
    }

    public StructureType getType() {
        return StructureType.MODULE;
    }

    protected RPsiElement getNameElement() {
        return getModuleName();
    }
}
