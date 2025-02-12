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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Aug 27, 2007
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class ShowFileSymbolAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(DataKeys.PROJECT);
        if (project == null){
            return;
        }
        final FileSymbol fileSymbol = LastSymbolStorage.getInstance(project).getSymbol();
        if (fileSymbol != null) {
            final String string = fileSymbol.dumpHTML();
            try {
                final File file = File.createTempFile("dump", ".html");
                final FileWriter writer = new FileWriter(file);
                writer.append(string);
                writer.close();
                BrowserUtil.launchBrowser(VirtualFileUtil.constructLocalUrl(file.getPath()));
            } catch (IOException e1) {
                // ignore
            }

        }
    }
}
