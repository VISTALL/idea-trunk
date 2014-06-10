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

package org.jetbrains.plugins.grails.lang.gsp.highlighter;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;

/**
 * @author ilyas
 */
public class GspPairedBraceMatcher implements PairedBraceMatcher, GspElementTypes {

  private static final BracePair[] PAIRS = new BracePair[]{
          new BracePair(JEXPR_BEGIN, JEXPR_END, false),
          new BracePair(JSCRIPT_BEGIN, JSCRIPT_END, false),
          new BracePair(JDECLAR_BEGIN, JDECLAR_END, false),
          new BracePair(JDIRECT_BEGIN, JDIRECT_END, false),

          new BracePair(GEXPR_BEGIN, GEXPR_END, false),
          new BracePair(GSCRIPT_BEGIN, GSCRIPT_END, false),
          new BracePair(GDECLAR_BEGIN, GDECLAR_END, false),
          new BracePair(GDIRECT_BEGIN, GDIRECT_END, false),
  };

  public BracePair[] getPairs() {
    return PAIRS;
  }

  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType iElementType, @Nullable IElementType iElementType1) {
    return true;
  }

  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}