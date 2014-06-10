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

package org.jetbrains.plugins.groovy.grails.parser;

import org.jetbrains.plugins.groovy.GroovyFileType;

import java.io.IOException;

/**
 * @author ilyas
 */
public class GspGroovyParserTest extends GspParsingTestCase {

  public void testComments$com() throws Throwable { doTest(); }
  public void testComments$comm1() throws Throwable { doTest(); }
  public void testComments$commm2() throws Throwable { doTest(); }
  public void testControl$common() throws Throwable { doTest(); }
  public void testControl$for1() throws Throwable { doTest(); }
  public void testControl$for2() throws Throwable { doTest(); }
  public void testControl$if1() throws Throwable { doTest(); }
  public void testControl$if2() throws Throwable { doTest(); }
  public void testControl$swit1() throws Throwable { doTest(); }
  public void testControl$swit2() throws Throwable { doTest(); }
  public void testCustom$tag1() throws Throwable { doTest(); }
  public void testCustom$tag2() throws Throwable { doTest(); }
  public void testCustom$tag3() throws Throwable { doTest(); }
  public void testDeclarations$dec1() throws Throwable { doTest(); }
  public void testDeclarations$dec2() throws Throwable { doTest(); }
  public void testDeclarations$dec3() throws Throwable { doTest(); }
  public void testDirect$dir1() throws Throwable { doTest(); }
  public void testErrors$err1() throws Throwable { doTest(); }
  public void testErrors$err2() throws Throwable { doTest(); }
  public void testErrors$err3() throws Throwable { doTest(); }
  public void testErrors$err4() throws Throwable { doTest(); }
  public void testSimple$clos1() throws Throwable { doTest(); }
  public void testSimple$inj1() throws Throwable { doTest(); }
  public void testSimple$inj2() throws Throwable { doTest(); }
  public void testSimple$nl() throws Throwable { doTest(); }
  public void testSimple$stat1() throws Throwable { doTest(); }

  private void doTest() throws IOException {
    doTest(GroovyFileType.GROOVY_FILE_TYPE.getLanguage());
  }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/parser/gspGroovy/";
  }
}
