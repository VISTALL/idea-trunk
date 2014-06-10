package org.jetbrains.android.dom;

import com.android.sdklib.SdkConstants;
import org.jetbrains.android.sdk.Android15TestProfile;
import org.jetbrains.android.sdk.AndroidSdkTestProfile;

import java.util.List;

/**
 * @author coyote
 */
public class AndroidManifestDomTest extends AndroidDomTest {
  public AndroidManifestDomTest() {
    super(false, "dom/manifest");
  }

  public AndroidSdkTestProfile getTestProfile() {
    return new Android15TestProfile();
  }

  protected String getPathToCopy(String testFileName) {
    return SdkConstants.FN_ANDROID_MANIFEST_XML;
  }

  public void testAttributeNameCompletion1() throws Throwable {
    testCompletionVariants("an1.xml", "android:icon", "android:label", "android:priority");
  }

  public void testAttributeNameCompletion2() throws Throwable {
    testCompletionVariants("an2.xml", withNamespace("debuggable", "description"));
  }

  public void testAttributeNameCompletion3() throws Throwable {
    testCompletion("an3.xml", "an3_after.xml");
  }

  public void testAttributeNameCompletion4() throws Throwable {
    testCompletion("an4.xml", "an4_after.xml");
  }

  public void testTagNameCompletion2() throws Throwable {
    testCompletionVariants("tn2.xml", "manifest");
  }

  public void testHighlighting() throws Throwable {
    testHighlighting("hl.xml");
  }

  public void testTagNameCompletion3() throws Throwable {
    testCompletion("tn3.xml", "tn3_after.xml");
  }

  public void testTagNameCompletion4() throws Throwable {
    testCompletion("tn4.xml", "tn4_after.xml");
  }

  public void testAttributeValueCompletion1() throws Throwable {
    testCompletionVariants("av1.xml", "behind", "landscape", "nosensor", "portrait", "sensor", "unspecified", "user");
  }

  public void testResourceCompletion1() throws Throwable {
    testCompletionVariants("av2.xml", "@android:", "@style/style1");
  }

  public void testResourceCompletion2() throws Throwable {
    testCompletionVariants("av3.xml", "@android:", "@string/hello", "@string/hello1", "@string/welcome", "@string/welcome1",
                           "@string/itStr");
  }

  public void testResourceCompletion3() throws Throwable {
    List<String> list = getAllResources();
    list.add("@android:");
    testCompletionVariants("av4.xml", list.toArray(new String[list.size()]));
  }

  public void testTagNameCompletion1() throws Throwable {
    testCompletionVariants("tn1.xml", "uses-permission", "uses-sdk", "uses-configuration");
  }

  public void testSoftTagsAndAttrs() throws Throwable {
    testHighlighting("soft.xml");
  }

  public void testNamespaceCompletion() throws Throwable {
    testCompletion("ns.xml", "ns_after.xml");
  }
}
