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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualStructuralElement;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualUtil;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RVirtualConstant;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RVirtualField;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.ConstantDefinitions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.FieldDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldConstantContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RConstantHolderUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RFieldHolderUtil;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Mar 13, 2007
 */
public abstract class RFieldConstantContainerImpl extends RContainerBase implements RFieldConstantContainer {

    // Cached information for ConstantUsages
    private List<ConstantDefinitions> myConstantDefinitions;

    // Cached information for FieldUsages
    private List<FieldDefinition> myFieldDefinitions;

    public RFieldConstantContainerImpl(ASTNode astNode) {
        super(astNode);
    }


    @NotNull
    public List<ConstantDefinitions> getConstantDefinitions() {
        if (myConstantDefinitions == null) {
            myConstantDefinitions = RConstantHolderUtil.gatherConstantDefinitions(this);
        }
        return myConstantDefinitions;
    }

    @Nullable
    public ConstantDefinitions getDefinition(@NotNull final RVirtualConstant constant) {
        return RConstantHolderUtil.getDefinition(this, constant);
    }

    @NotNull
    public List<FieldDefinition> getFieldsDefinitions() {
        if (myFieldDefinitions == null) {
            myFieldDefinitions = RFieldHolderUtil.gatherFieldDescriptions(this);
        }
        return myFieldDefinitions;
    }

    @Nullable
    public FieldDefinition getDefinition(@NotNull final RVirtualField field) {
        return RFieldHolderUtil.getDefinition(this, field);
    }

    public synchronized void subtreeChanged() {
        clearMyCaches();
        super.subtreeChanged();
    }

    private void clearMyCaches() {
        myFieldDefinitions = null;
        myConstantDefinitions = null;
    }

    @NotNull
    public List<RVirtualConstant> getVirtualConstants() {
        return RVirtualUtil.getVirtualConstants(this, this);
    }

    @NotNull
    public List<RVirtualField> getVirtualFields() {
        return RVirtualUtil.getVirtualFields(this, this);
    }

    public boolean equalsToVirtual(@NotNull RVirtualStructuralElement element) {
        // TODO: to be honest, we must add another 2 check!
        // RVPsiUtuils.areConstantHoldersEqual and RVPsiUtuils.areFieldHoldersEqual
        return super.equalsToVirtual(element);
    }
}
