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
 * Time: 8:37:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidXmlResourcesDomTest extends AndroidDomTest {
  public AndroidXmlResourcesDomTest() {
    super(false, "dom/xml");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.copyFileToProject(SdkConstants.FN_ANDROID_MANIFEST_XML, SdkConstants.FN_ANDROID_MANIFEST_XML);
  }

  protected String getPathToCopy(String testFileName) {
    return "res/xml/" + testFileName;
  }

  public AndroidSdkTestProfile getTestProfile() {
    return new Android15TestProfile();
  }


  public void testPreferenceRootCompletion() throws Throwable {
    testCompletion("pref1.xml", "pref1_after.xml");
  }

  public void testPreferenceChildrenCompletion() throws Throwable {
    testCompletion("pref2.xml", "pref2_after.xml");
  }

  public void testPreferenceAttributeNamesCompletion1() throws Throwable {
    testCompletionVariants("pref3.xml", withNamespace("summary", "summaryOn", "summaryOff"));
  }

  public void testPreferenceAttributeNamesCompletion2() throws Throwable {
    testCompletion("pref4.xml", "pref4_after.xml");
  }

  public void testPreferenceAttributeValueCompletion() throws Throwable {
    testCompletionVariants("pref5.xml", "@string/welcome", "@string/welcome1");
  }

  public void testSearchableRoot() throws Throwable {
    testCompletion("searchable_r.xml", "searchable_r_after.xml");
  }

  public void testSearchableAttributeName() throws Throwable {
    testCompletion("searchable_an.xml", "searchable_an_after.xml");
  }

  public void testSearchableAttributeValue() throws Throwable {
    testCompletionVariants("searchable_av.xml", "@string/welcome", "@string/welcome1");
  }

  public void testSearchableTagNameCompletion() throws Throwable {
    testCompletion("searchable_tn.xml", "searchable_tn_after.xml");
  }

  public void testPreferenceIntent() throws Throwable {
    testHighlighting("pref_intent.xml");
  }

  public void testPreferenceIntent1() throws Throwable {
    testCompletion("pref_intent1.xml", "pref_intent1_after.xml");
  }
}
