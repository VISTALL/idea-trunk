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

import org.jetbrains.android.sdk.AndroidSdkTestProfile;
import org.jetbrains.android.sdk.Android15TestProfile;
import com.android.sdklib.SdkConstants;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 11, 2009
 * Time: 9:26:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidAnimationDomTest extends AndroidDomTest {
  public AndroidAnimationDomTest() {
    super(false, "dom/anim");
  }

  protected String getPathToCopy(String testFileName) {
    return "res/anim/" + testFileName;
  }

  public AndroidSdkTestProfile getTestProfile() {
    return new Android15TestProfile();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML, SdkConstants.FN_ANDROID_MANIFEST_XML);
  }

  public void testRootCompletion() throws Throwable {
    testCompletion("root.xml", "root_after.xml");
  }

  public void testHighlighting() throws Throwable {
    testHighlighting("hl.xml");
  }

  public void testChildren() throws Throwable {
    testCompletion("tn.xml", "tn_after.xml");
  }
}
