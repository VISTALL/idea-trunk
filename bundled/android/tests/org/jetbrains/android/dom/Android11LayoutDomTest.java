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
import org.jetbrains.android.sdk.Android11TestProfile;
import org.jetbrains.android.sdk.AndroidSdkTestProfile;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 3, 2009
 * Time: 3:28:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Android11LayoutDomTest extends AndroidDomTest {
  public Android11LayoutDomTest() {
    super(false, "dom/layout");
  }

  public AndroidSdkTestProfile getTestProfile() {
    return new Android11TestProfile();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML, SdkConstants.FN_ANDROID_MANIFEST_XML);
  }

  protected String getPathToCopy(String testFileName) {
    return "res/layout/" + testFileName;
  }

  public void testAttributeNameCompletion() throws Throwable {
      testCompletionVariants("an1.xml", withNamespace("layout_weight", "layout_width"));
  }

  public void testHighlighting() throws Throwable {
    testHighlighting("hl.xml");
  }

  public void testResourceCompletion() throws Throwable {
    testCompletionVariants("av3.xml", "@color/", "@android:", "@drawable/");
    List<String> list = getAllResources();
    list.add("@android:");
    testCompletionVariants("av8.xml", list.toArray(new String[list.size()]));
  }
}
