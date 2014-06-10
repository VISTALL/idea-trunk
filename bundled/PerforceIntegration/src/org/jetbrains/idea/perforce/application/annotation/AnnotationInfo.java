/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.application.annotation;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

public class AnnotationInfo {
  private final String myContent;
  private final int[] myRevisions;
  private final boolean myUseChangelistNumbers;

  public AnnotationInfo(String output, boolean useChangelistNumbers) throws IOException {
    myUseChangelistNumbers = useChangelistNumbers;
    final LineNumberReader reader = new LineNumberReader(new StringReader(output));
    String line;
    final StringBuffer content = new StringBuffer();
    final TIntArrayList revisions = new TIntArrayList();
    while ((line = reader.readLine()) != null) {
      final int endOfRevisionIndex = line.indexOf(":");
      if (endOfRevisionIndex > 0) {
        String revision = line.substring(0, endOfRevisionIndex);
        String contentLine = line.substring(endOfRevisionIndex + 2);
        content.append(contentLine);
        content.append("\n");
        revisions.add(Integer.parseInt(revision));
      }
    }

    myContent = content.toString();
    myRevisions = revisions.toNativeArray();
  }

  public String getContent() {
    return myContent;
  }

  public int getRevision(int lineNumber) {
    if (lineNumber < 0 || lineNumber >= myRevisions.length) return -1;
    return myRevisions[lineNumber];
  }

  public boolean isUseChangelistNumbers() {
    return myUseChangelistNumbers;
  }
}
