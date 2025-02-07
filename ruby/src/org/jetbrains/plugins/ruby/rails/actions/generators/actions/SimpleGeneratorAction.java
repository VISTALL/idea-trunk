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

package org.jetbrains.plugins.ruby.rails.actions.generators.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.rails.RailsUtil;
import org.jetbrains.plugins.ruby.rails.actions.generators.GenerateDialogs;
import org.jetbrains.plugins.ruby.rails.actions.generators.GeneratorsUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.RubyScriptRunnerArgumentsProvider;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 02.12.2006
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class SimpleGeneratorAction extends AbstractScriptAction {
    private String myGeneratorName;

    public SimpleGeneratorAction(@NotNull final String scriptName,
                                 @NotNull final String actionName,
                                 @Nullable final String description,
                                 @Nullable final Icon icon) {
        super(actionName, description, icon);
        myGeneratorName = scriptName;
    }

    public SimpleGeneratorAction(@NotNull final String name) {
        this(name,
                format(name),
                RBundle.message("new.generate.common.action.title", name),
                RailsIcons.GENERATOR_ICON);
    }

    private static String format(final String str) {
        final String result = Character.toUpperCase(str.charAt(0)) +
                (str.length() > 1 ? str.substring(1) : TextUtil.EMPTY_STRING);
        return result.replace('_', ' ');
    }


    public String getGeneratorName() {
        return myGeneratorName;
    }

    private String getGeneratingProcessTitle() {
        return RBundle.message("new.generate.common.generating.title");
    }

    /**
     * Delegates invokeAction to corresponding <code>SimpleGeneratorAction</code>
     *
     * @param scriptArguments arguments for script
     * @param mainArgument    main argument
     * @param module          module
     */
    @SuppressWarnings({"UnusedDeclaration", "UnusedParameters"})
    public void invokeAction(final String scriptArguments,
                             final String mainArgument,
                             final Module module) {

        //Save all opened documents
        FileDocumentManager.getInstance().saveAllDocuments();

        /**
        * Run script in console
        */
        final String railsAppHomePath =  RailsFacetUtil.getRailsAppHomeDirPath(module);
        assert railsAppHomePath != null;
        GeneratorsUtil.invokeGenerator(module,
                                        getGeneratingProcessTitle(),
                                        getErrorTitle(),
                                        createScriptParameters(scriptArguments, railsAppHomePath),
                                        null, null, RModuleUtil.getModuleOrJRubyFacetSdk(module));
    }

    public void update(@NotNull final AnActionEvent e) {
        super.update(e);

        final Presentation presentation = e.getPresentation();
        if (!presentation.isVisible()) {
            return;
        }

        final DataContext dataContext = e.getDataContext();
        final Module module = DataKeys.MODULE.getData(dataContext);

        final boolean isVisible = module != null
                && RailsFacetUtil.hasRailsSupport(module);
        
        final boolean isEnabled = isVisible
                && RubySdkUtil.isKindOfRubySDK(RModuleUtil.getModuleOrJRubyFacetSdk(module));

        AnActionUtil.updatePresentation(presentation, isVisible, isEnabled);
    }

    protected SimpleGeneratorInputValidator createValidator(@NotNull final Module module,
                                                            @Nullable final PsiDirectory directory) {
        return new SimpleGeneratorInputValidator(this, module, directory);
    }

    protected void checkBeforeCreate(@NotNull final String newName,
                                     @Nullable final PsiDirectory directory)
            throws IncorrectOperationException {
        // Do nothing
    }

    protected String[] createScriptParameters(final String inputString,
                                              @NotNull final String railsAppHomePath) {
        final List<String> parameters = new ArrayList<String>();
        parameters.add(VirtualFileUtil.buildSystemIndependentPath(railsAppHomePath, GeneratorsUtil.GENERATE_SCRIPT));
        parameters.add(myGeneratorName);

        RubyScriptRunnerArgumentsProvider.collectArguments(inputString, parameters);

        return parameters.toArray(new String[parameters.size()]);
    }

    protected String getGenerateDialogTitle() {
        return RBundle.message("new.generate.common.action.prompt.title",
                myGeneratorName);
    }

    protected String getErrorTitle() {
        return RBundle.message("new.generate.common.error.title");
    }

    protected PsiElement[] invokeDialog(@NotNull final Module module,
                                        @Nullable final PsiDirectory directory) {
        final SimpleGeneratorInputValidator validator = createValidator(module, directory);
        GenerateDialogs.showGenerateDialog(module, getGenerateDialogTitle(),
                validator);
        return validator.getCreatedElements();
    }

    protected boolean validateBeforeInvokeDialog(final Module module) {
        if (!RailsUtil.hasRailsSupportInSDK(RModuleUtil.getModuleOrJRubyFacetSdk(module))) {
            Messages.showErrorDialog(module.getProject(),
                                     RBundle.message("new.generate.common.error.no.rails"),
                                     RBundle.message("action.registered.shortcut.execute.disabled.title"));
            return false;
        }
        return true;
    }
}
