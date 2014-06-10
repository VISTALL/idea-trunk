/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.completion.handlers;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.HashSet;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.extensions.completion.ContextSpecificInsertHandler;

import java.util.Set;

/**
 * @author ilyas
 */
public class ControllerReferenceInsertHandler implements ContextSpecificInsertHandler {
  private static final Logger LOG =
    Logger.getInstance("#org.jetbrains.plugins.grails.completion.handlers.ControllerReferenceInsertHandler");
  private final static Set<String> CONTROLLER_IMPLICIT_METHODS = new HashSet<String>(4);

  static {
    CONTROLLER_IMPLICIT_METHODS.add("render");
    CONTROLLER_IMPLICIT_METHODS.add("bindData");
    CONTROLLER_IMPLICIT_METHODS.add("bind");
    CONTROLLER_IMPLICIT_METHODS.add("redirect");
  }


  public boolean isAcceptable(InsertionContext context, int startOffset, LookupElement item) {
    PsiFile file = context.getFile();
    VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
    return virtualFile != null &&
        GrailsUtils.isControllerClassFile(virtualFile, file.getProject()) &&
        CONTROLLER_IMPLICIT_METHODS.contains(item.getLookupString());
  }

  public void handleInsert(InsertionContext context, int startOffset, LookupElement item) {
    Object obj = item.getObject();
    if (!(obj instanceof String) || !CONTROLLER_IMPLICIT_METHODS.contains(obj)) {
      LOG.assertTrue(false, "obj = " + obj);
    }

    String name = (String) obj;
    Editor editor = context.getEditor();
    Document document = editor.getDocument();
    CaretModel caretModel = editor.getCaretModel();
    int offset = startOffset + name.length();
    if (offset == document.getTextLength() || document.getCharsSequence().charAt(offset) != '(') {
      document.insertString(offset, "()");
    }
    caretModel.moveToOffset(offset + 1);
  }

}
