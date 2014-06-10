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

package org.jetbrains.android.dom;

import com.android.sdklib.SdkConstants;
import org.jetbrains.android.sdk.Android15TestProfile;
import org.jetbrains.android.sdk.AndroidSdkTestProfile;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 25, 2009
 * Time: 6:45:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidValueResourcesTest extends AndroidDomTest {
  public AndroidValueResourcesTest() {
    super(false, "dom/resources");
  }

  public AndroidSdkTestProfile getTestProfile() {
    return new Android15TestProfile();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML, SdkConstants.FN_ANDROID_MANIFEST_XML);
  }

  protected String getPathToCopy(String testFileName) {
    return "res/values/" + testFileName;
  }

  public void testHtmlTags() throws Throwable {
    testCompletionVariants("htmlTags.xml", "b", "i", "u");
  }

  public void testStyles1() throws Throwable {
    testCompletionVariants("styles1.xml", "@drawable/picture1", "@drawable/picture2", "@drawable/picture3");
  }

  public void testStyles2() throws Throwable {
    testCompletion("styles2.xml", "styles2_after.xml");
  }

  public void testStyles3() throws Throwable {
    testCompletionVariants("styles3.xml", "normal", "bold", "italic");
  }

  public void testStylesHighlighting() throws Throwable {
    testHighlighting("styles4.xml");
  }

  public void testAttrFormatCompletion() throws Throwable {
    testCompletion("attrs1.xml", "attrs1_after.xml");
  }
}
