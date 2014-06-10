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

package org.jetbrains.plugins.grails.lang.gsp.parsing.gsp;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.impl.source.parsing.xml.XmlParsing;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author ilyas
 */
public class GspParser implements PsiParser, GspElementTypes, XmlTokenType {

  @NotNull
  public ASTNode parse(final IElementType root, final PsiBuilder builder) {

    builder.enforceCommentTokens(TokenSet.EMPTY);
    final PsiBuilder.Marker file = builder.mark();
    GspParsing gspParsing = new GspParsing(builder);
    final PsiBuilder.Marker docMarker = builder.mark();
    builder.mark().done(XmlElementType.XML_PROLOG);

    final PsiBuilder.Marker rootTag = builder.mark();
    while (true) {
      gspParsing.parseTagContent();
      if (builder.eof()) break;
    }
    rootTag.done(GspElementTypes.GSP_ROOT_TAG);
    docMarker.done(GSP_XML_DOCUMENT);
    file.done(GSP_FILE);
    return builder.getTreeBuilt();
  }

  public static class GspParsing extends XmlParsing {
    private final PsiBuilder builder;


    public GspParsing(final PsiBuilder builder) {
      super(builder);
      this.builder = builder;
    }

    protected void parseComment() {
      final PsiBuilder.Marker comment = mark();
      advance();
      comment.done(XML_COMMENT);
    }

    protected boolean isCommentToken(final IElementType elementType) {
      return GSP_COMMENTS.contains(elementType);
    }

    public void parseTagContent() {
      parseVariousTagContent(false);
    }


    public void parseVariousTagContent(boolean isInGrailsTag) {
      IElementType tokenType = builder.getTokenType();
      if (JSCRIPT_BEGIN.equals(tokenType) ||
              GSCRIPT_BEGIN.equals(tokenType)) {
        parseScriptlet();
        return;
      }
      if (JEXPR_BEGIN.equals(tokenType) ||
              GEXPR_BEGIN.equals(tokenType)) {
        parseExprInjection();
        return;
      }
      if (JDECLAR_BEGIN.equals(tokenType) ||
              GDECLAR_BEGIN.equals(tokenType)) {
        parseDeclaration();
        return;
      }
      if (XML_START_TAG_START.equals(tokenType)) {
        GrailsTag.parse(builder);
        return;
      }
      if (XML_END_TAG_START.equals(tokenType)) {
        if (isInGrailsTag) return;
        else {
          ParserUtils.getToken(builder, XML_END_TAG_START);
          builder.error(GrailsBundle.message("clos.tag.in.wrong.place"));
          ParserUtils.getToken(builder, XML_TAG_NAME);
          ParserUtils.getToken(builder, XML_TAG_END);
          return;
        }
      }
      if (!builder.eof()) {
        builder.advanceLexer();
      }
    }

    private void parseScriptlet() {
      PsiBuilder.Marker marker = builder.mark();
      builder.advanceLexer();
      if (GSP_GROOVY_CODE.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
      if (!(GSCRIPT_END.equals(builder.getTokenType()) ||
              JSCRIPT_END.equals(builder.getTokenType()))) {
        builder.error(GrailsBundle.message("script.end.tag.expected"));
      } else {
        builder.advanceLexer();
      }
      marker.done(GSP_SCRIPTLET_TAG);
    }

    private void parseExprInjection() {
      PsiBuilder.Marker marker = builder.mark();
      builder.advanceLexer();
      if (GSP_GROOVY_CODE.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
      if (!(GEXPR_END.equals(builder.getTokenType()) ||
              JEXPR_END.equals(builder.getTokenType()))) {
        builder.error(GrailsBundle.message("script.end.tag.expected"));
      } else {
        builder.advanceLexer();
      }
      marker.done(GSP_EXPR_TAG);
    }

    private void parseDeclaration() {
      PsiBuilder.Marker marker = builder.mark();
      builder.advanceLexer();
      if (GSP_GROOVY_CODE.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
      if (!(GDECLAR_END.equals(builder.getTokenType()) ||
              JDECLAR_END.equals(builder.getTokenType()))) {
        builder.error(GrailsBundle.message("script.end.tag.expected"));
      } else {
        builder.advanceLexer();
      }
      marker.done(GSP_DECLARATION_TAG);
    }

    public void parseDocument() {
    }


  }
}
