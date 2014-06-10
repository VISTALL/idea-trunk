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

import com.intellij.lang.PsiBuilder;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author ilyas
 */
public class GrailsTag implements GspElementTypes {

  public static void parse(PsiBuilder builder) {
    PsiBuilder.Marker tagMarker = builder.mark();

    ParserUtils.getToken(builder, XML_START_TAG_START);
    if (!XML_TAG_NAME.equals(builder.getTokenType())) {
      builder.error(GroovyBundle.message("identifier.expected"));
      tagMarker.drop();
      return;
    }
    String tagName = builder.getTokenText();
    builder.advanceLexer();
    parseAttrList(builder);
    if (ParserUtils.getToken(builder, XML_EMPTY_ELEMENT_END)) {
      tagMarker.done(GRAILS_TAG);
    } else if (ParserUtils.getToken(builder, XML_TAG_END)) {
      parseBody(builder, tagName);
      parseEndTag(builder, tagName);
      tagMarker.done(GRAILS_TAG);
    } else {
      if (XML_END_TAG_START == builder.getTokenType() || XML_START_TAG_START == builder.getTokenType()) {
        tagMarker.done(GRAILS_TAG);
        return;
      }
      while (!(XML_EMPTY_ELEMENT_END.equals(builder.getTokenType()) ||
          XML_TAG_END.equals(builder.getTokenType()) ||
          builder.eof())) {
        builder.error(GrailsBundle.message("wrong.attributes"));
        builder.advanceLexer();
      }
      if (ParserUtils.getToken(builder, XML_EMPTY_ELEMENT_END)) {
        tagMarker.done(GRAILS_TAG);
      } else if (ParserUtils.getToken(builder, XML_TAG_END)) {
        parseBody(builder, tagName);
        parseEndTag(builder, tagName);
        tagMarker.done(GRAILS_TAG);
      } else {
        tagMarker.done(GRAILS_TAG);
      }
    }
  }

  private static void parseEndTag(PsiBuilder builder, String tagName) {
    if (XML_END_TAG_START.equals(builder.getTokenType())) {
      ParserUtils.getToken(builder, XML_END_TAG_START);
      if (XML_TAG_NAME.equals(builder.getTokenType()) && tagName.equals(builder.getTokenText())) {
        builder.advanceLexer();
      } else {
        ParserUtils.getToken(builder, XML_TAG_NAME, GrailsBundle.message("closing.tag.brace.expected"));
      }
      ParserUtils.getToken(builder, XML_TAG_END, GrailsBundle.message("closing.tag.brace.expected"));
    } else {
      builder.error(GrailsBundle.message("closing.grails.tag.expected", tagName));
    }
  }

  private static void parseAttrList(PsiBuilder builder) {
    while (!builder.eof() &&
        !(XML_EMPTY_ELEMENT_END.equals(builder.getTokenType()) || XML_TAG_END.equals(builder.getTokenType()))) {
      if (XML_END_TAG_START == builder.getTokenType() || XML_START_TAG_START == builder.getTokenType()) {
        return;
      } else {
        parseAttribute(builder);
      }
    }
    if (XML_EMPTY_ELEMENT_END.equals(builder.getTokenType()) || XML_TAG_END.equals(builder.getTokenType())) {
    } else {
      builder.error(GrailsBundle.message("closing.tag.brace.expected1"));
    }
  }

  private static void parseAttribute(PsiBuilder builder) {
    PsiBuilder.Marker attrMarker = builder.mark();
    if (!XML_NAME.equals(builder.getTokenType())) {
      builder.error(GroovyBundle.message("identifier.expected"));
      attrMarker.drop();
      while (!builder.eof() &&
          !(XML_TAG_END.equals(builder.getTokenType()) || XML_EMPTY_ELEMENT_END.equals(builder.getTokenType())) &&
          !(XML_END_TAG_START == builder.getTokenType() || XML_START_TAG_START == builder.getTokenType())
          && !XML_NAME.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
      return;
    }
    builder.advanceLexer();
    if (!XML_EQ.equals(builder.getTokenType())) {
      builder.error(GrailsBundle.message("equal.expected"));
      attrMarker.done(GRAILS_TAG_ATTRIBUTE);
      while (!builder.eof() &&
          !(XML_TAG_END.equals(builder.getTokenType()) || XML_EMPTY_ELEMENT_END.equals(builder.getTokenType())) &&
          !(XML_END_TAG_START == builder.getTokenType() || XML_START_TAG_START == builder.getTokenType())
          && !XML_NAME.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
      return;
    }
    builder.advanceLexer();
    if (!XML_ATTRIBUTE_VALUE_START_DELIMITER.equals(builder.getTokenType())) {
      builder.error(GrailsBundle.message("delim.val.expected"));
      attrMarker.done(GRAILS_TAG_ATTRIBUTE);
      while (!builder.eof() &&
          !(XML_TAG_END.equals(builder.getTokenType()) || XML_EMPTY_ELEMENT_END.equals(builder.getTokenType())) &&
          !(XML_END_TAG_START == builder.getTokenType() || XML_START_TAG_START == builder.getTokenType())
          && !XML_NAME.equals(builder.getTokenType())) {
        builder.advanceLexer();
      }
      return;
    }
    builder.advanceLexer();
    PsiBuilder.Marker valMarker = builder.mark();
    while (!builder.eof() && !XML_ATTRIBUTE_VALUE_END_DELIMITER.equals(builder.getTokenType())) {
      builder.advanceLexer();
    }
    if (XML_ATTRIBUTE_VALUE_END_DELIMITER.equals(builder.getTokenType())) {
      valMarker.done(GRAILS_TAG_ATTRIBUTE_VALUE);
      builder.advanceLexer();
    } else {
      valMarker.done(GRAILS_TAG_ATTRIBUTE_VALUE);
    }

    attrMarker.done(GRAILS_TAG_ATTRIBUTE);

    while (!builder.eof() &&
        !(XML_TAG_END.equals(builder.getTokenType()) || XML_EMPTY_ELEMENT_END.equals(builder.getTokenType())) &&
        !(XML_END_TAG_START == builder.getTokenType() || XML_START_TAG_START == builder.getTokenType())
        && !XML_NAME.equals(builder.getTokenType())) {
      builder.error(GrailsBundle.message("wrong.attributes"));
      builder.advanceLexer();
    }
  }

  private static void parseBody(PsiBuilder builder, String tagName) {
    GspParser.GspParsing gspParsing = new GspParser.GspParsing(builder);
    PsiBuilder.Marker first = null;
    while (true) {
      gspParsing.parseVariousTagContent(true);
      if (XML_END_TAG_START.equals(builder.getTokenType())) {
        if (first == null) {
          first = builder.mark();
        }
        PsiBuilder.Marker rb = builder.mark();
        builder.advanceLexer();
        if (tagName.equals(builder.getTokenText())) {
          rb.rollbackTo();
          first.drop();
          first = null;
          break;
        } else {
          builder.error(GrailsBundle.message("clos.tag.in.wrong.place"));
          ParserUtils.getToken(builder, XML_TAG_NAME);
          ParserUtils.getToken(builder, XML_TAG_END);
          rb.drop();
        }
      }
      if (builder.eof()) {
        builder.error(GrailsBundle.message("closing.grails.tag.expected", tagName));
        break;
      }
    }
    if (first != null) {
      first.rollbackTo();
    }
  }

}
