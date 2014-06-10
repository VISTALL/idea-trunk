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

package org.jetbrains.plugins.grails.references.providers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceSet;
import com.intellij.openapi.project.Project;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ilyas
 */
public class GspImportListReferenceProvider extends JavaClassReferenceProvider {
  @NonNls
  private static final String STATIC_PREFIX = "static ";

  public GspImportListReferenceProvider(final Project project) {
    super(project);
    Map<CustomizationKey, Object> options = new THashMap<CustomizationKey, Object>();
    options.put(RESOLVE_QUALIFIED_CLASS_NAME, Boolean.TRUE);
    options.put(ADVANCED_RESOLVE, Boolean.TRUE);
    setOptions(options);
  }

  @NotNull
  public PsiReference[] getReferencesByString(String str, PsiElement position, int offsetInPosition) {
    final List<PsiReference> results = new ArrayList<PsiReference>();
    int lastReferencePosition = 0;

    do {
      int asIndex = str.indexOf(" as ", lastReferencePosition);
      int i = str.indexOf(';', lastReferencePosition);
      final int nextReferenceStart = i >= 0 ? i : str.length();
      String identifier = str.substring(lastReferencePosition, asIndex >= 0 && asIndex + 1 < nextReferenceStart
          ? asIndex + 1 : nextReferenceStart);
      if (identifier.length() == 0) {
        lastReferencePosition = nextReferenceStart + 1;
        continue;
      }
      int whitespaceShift = 0;
      while (whitespaceShift < identifier.length() && Character.isWhitespace(identifier.charAt(whitespaceShift++))) ;
      lastReferencePosition += whitespaceShift - 1;
      identifier = identifier.substring(whitespaceShift - 1);
      boolean isStatic = false;
      if (identifier.startsWith(STATIC_PREFIX)) {
        identifier = identifier.substring(STATIC_PREFIX.length());
        lastReferencePosition += STATIC_PREFIX.length();
        isStatic = true;
      }
      final JavaClassReferenceSet referenceSet = new JavaClassReferenceSet(identifier.trim(),
          position,
          offsetInPosition + lastReferencePosition, isStatic, this);
      final PsiReference[] allReferences = referenceSet.getAllReferences();
//      int index = 0;
      for (PsiReference allReference : allReferences) {
        String text = allReference.getCanonicalText();
        if (!"*".equals(text)) {
/*
          if (!text.trim().equals(text)) {
            int startOffset = allReference.getRangeInElement().getStartOffset();
            int endOffset = allReference.getRangeInElement().getEndOffset();
            while (text.charAt(0) == ' ' || text.charAt(0) == '\n' ||
                text.charAt(0) == 't' || text.charAt(0) == '\r') {
              startOffset++;
              text = text.substring(1);
            }
            while (!text.trim().equals(text)) {
              endOffset--;
              text = text.substring(0, text.length() - 1);
            }
            TextRange range = new TextRange(startOffset, endOffset);
            JavaClassReference newReference = new JavaClassReference(referenceSet, range, index, text, isStatic);
            results.add(newReference);
          } else
*/
          results.add(allReference);
        }
//        index++;
      }
      lastReferencePosition = nextReferenceStart + 1;
    }
    while (lastReferencePosition < str.length());
    return results.toArray(new PsiReference[results.size()]);
  }

  public boolean isSoft() {
    return false;
  }
}
