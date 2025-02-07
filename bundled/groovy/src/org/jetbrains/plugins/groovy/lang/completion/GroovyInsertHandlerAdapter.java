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

package org.jetbrains.plugins.groovy.lang.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.plugins.groovy.extensions.completion.ContextSpecificInsertHandler;
import org.jetbrains.plugins.groovy.extensions.completion.InsertHandlerRegistry;

/**
 * @author ilyas
 */
public class GroovyInsertHandlerAdapter implements InsertHandler {

  private final GroovyInsertHandler myGroovyInsertHandler = new GroovyInsertHandler();

  public void handleInsert(InsertionContext context, LookupElement item) {
    for (ContextSpecificInsertHandler handler : InsertHandlerRegistry.getInstance().getSpecificInsertHandlers()) {
      if (handler.isAcceptable(context, context.getStartOffset(), item)) {
        handler.handleInsert(context, context.getStartOffset(), item);
        return;
      }
    }
    myGroovyInsertHandler.handleInsert(context, item);
  }
}
