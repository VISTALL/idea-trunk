/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jetbrains.plugins.grails.lang.gsp.highlighter;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.tree.IElementType;
import com.intellij.xml.impl.XmlBraceMatcher;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.IGspElementType;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;

/**
 * @author peter
 */
public class GspFileBraceMatcher extends XmlBraceMatcher {
  private static final int GSP_TOKEN_GROUP = 3;

  public int getBraceTokenGroupId(final IElementType tokenType) {
    if (tokenType instanceof IGspElementType) {
      return GSP_TOKEN_GROUP;
    }
    return super.getBraceTokenGroupId(tokenType);
  }

  public boolean areTagsCaseSensitive(final FileType fileType, final int braceGroupId) {
    if (braceGroupId == GSP_TOKEN_GROUP) return true;
    return super.areTagsCaseSensitive(fileType, braceGroupId);
  }

  public boolean isStrictTagMatching(final FileType fileType, final int braceGroupId) {
    if (braceGroupId == GSP_TOKEN_GROUP) return true;
    return super.isStrictTagMatching(fileType, braceGroupId);
  }

  protected boolean isWhitespace(final IElementType tokenType1) {
    return tokenType1 == GspTokenTypes.GSP_WHITE_SPACE || super.isWhitespace(tokenType1);
  }

  protected boolean isFileTypeWithSingleHtmlTags(final FileType fileType) {
    return fileType == GspFileType.GSP_FILE_TYPE || super.isFileTypeWithSingleHtmlTags(fileType);
  }
}
