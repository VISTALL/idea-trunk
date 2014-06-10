package org.jetbrains.android.dom;

import com.android.sdklib.SdkConstants;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.IdeaTestUtil;
import org.jetbrains.android.sdk.Android15TestProfile;
import org.jetbrains.android.sdk.AndroidSdkTestProfile;

import java.util.List;

/**
 * @author coyote
 */
public class AndroidLayoutDomTest extends AndroidDomTest {
  public AndroidLayoutDomTest() {
    super(false, "dom/layout");
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
    return "res/layout/" + testFileName;
  }

  public void testAttributeNameCompletion1() throws Throwable {
    testCompletionVariants("an1.xml", withNamespace("layout_weight", "layout_width"));
  }

  public void testAttributeNameCompletion2() throws Throwable {
    testCompletion("an2.xml", "an2_after.xml");
  }

  public void testAttributeNameCompletion3() throws Throwable {
    testCompletion("an3.xml", "an3_after.xml");
  }

  public void testAttributeNameCompletion4() throws Throwable {
    testCompletion("an4.xml", "an4_after.xml");
  }

  public void testAttributeNameCompletion5() throws Throwable {
    testCompletion("an5.xml", "an5_after.xml");
  }

  public void testHighlighting() throws Throwable {
    testHighlighting("hl.xml");
  }

  public void testCustomTagCompletion() throws Throwable {
    testCompletion("ctn.xml", "ctn_after.xml");
  }

  public void testCustomAttributeNameCompletion() throws Throwable {
    testCompletionVariants("can.xml", "app:text", "app:textColor", "app:textSize");
  }

  public void testCustomAttributeValueCompletion() throws Throwable {
    testCompletionVariants("cav.xml", "@color/color0", "@color/color1", "@color/color2");
  }

  public void testTagNameCompletion1() throws Throwable {
    testCompletion("tn1.xml", "tn1_after.xml");
  }

  public void testFlagCompletion() throws Throwable {
    testCompletionVariants("av1.xml", "center", "center_horizontal", "center_vertical");
    testCompletionVariants("av2.xml", "fill", "fill_horizontal", "fill_vertical");
  }

  public void testResourceCompletion() throws Throwable {
    testCompletionVariants("av3.xml", "@color/", "@android:", "@drawable/");
    List<String> list = getAllResources();
    list.add("@android:");
    testCompletionVariants("av8.xml", list.toArray(new String[list.size()]));
  }

  public void testLocalResourceCompletion1() throws Throwable {
    testCompletionVariants("av4.xml", "@color/color0", "@color/color1", "@color/color2");
  }

  public void testLocalResourceCompletion2() throws Throwable {
    testCompletionVariants("av5.xml", "@drawable/picture1", "@drawable/picture2", "@drawable/picture3", "@drawable/cdrawable");
  }

  public void testLocalResourceCompletion3() throws Throwable {
    testCompletionVariants("av7.xml", "@android:", "@string/hello", "@string/hello1", "@string/welcome", "@string/welcome1",
                           "@string/itStr");
  }

  public void testLocalResourceCompletion4() throws Throwable {
    testCompletionVariants("av7.xml", "@android:", "@string/hello", "@string/hello1", "@string/welcome", "@string/welcome1",
                           "@string/itStr");
  }

  public void testLocalResourceCompletion5() throws Throwable {
    testCompletionVariants("av12.xml", "@android:", "@anim/anim1", "@anim/anim2");
  }

  public void testForceLocalResourceCompletion() throws Throwable {
    testCompletionVariants("av13.xml", "@string/hello", "@string/hello1");
  }

  public void testSystemResourceCompletion() throws Throwable {
    testCompletionVariants("av6.xml", "@android:color/", "@android:drawable/");
  }

  public void testCompletionSpecialCases() throws Throwable {
    testCompletionVariants("av9.xml", "@string/hello", "@string/hello1");
  }

  public void testLayoutAttributeValuesCompletion() throws Throwable {
    testCompletionVariants("av10.xml", "fill_parent", "wrap_content", "@android:");
    testCompletionVariants("av11.xml", "center", "center_horizontal", "center_vertical");
  }

  public void testTagNameCompletion2() throws Throwable {
    testCompletionVariants("tn2.xml", "EditText", "ExpandableListView", "ExtractEditText");
  }

  public void testTagNameCompletion3() throws Throwable {
    testCompletionVariants("tn3.xml", "View", "ViewAnimator", "ViewFlipper", "ViewStub", "ViewSwitcher");
  }

  public void testTagNameCompletion4() throws Throwable {
    testCompletion("tn4.xml", "tn4_after.xml");
  }

  public void testIdCompletion1() throws Throwable {
    testCompletionVariants("idcompl1.xml", "@android:", "@+id/", "@id/idd1", "@id/idd2");
  }

  public void testIdCompletion2() throws Throwable {
    testCompletionVariants("idcompl2.xml", "@android:id/text", "@android:id/text1", "@android:id/text2");
  }

  public void testIdHighlighting() throws Throwable {
    testHighlighting("idh.xml");
  }

  public void testIdReferenceCompletion() throws Throwable {
    testCompletion("idref1.xml", "idref1_after.xml");
  }

  public void testSystemIdReferenceCompletion() throws Throwable {
    testCompletion("idref2.xml", "idref2_after.xml");
  }

  public void testViewClassCompletion() throws Throwable {
    testCompletion("viewclass.xml", "viewclass_after.xml");
  }

  public void testPrimitiveValues() throws Throwable {
    testHighlighting("primValues.xml");
  }

  public void testTableCellAttributes() throws Throwable {
    testCompletion("tableCell.xml", "tableCell_after.xml");
  }

  public void testCompletionPerformance() throws Throwable {
    myFixture.copyFileToProject("dom/resources/bigfile.xml", "res/values/bigfile.xml");
    myFixture.copyFileToProject("dom/resources/bigattrs.xml", "res/values/bigattrs.xml");
    myFixture.copyFileToProject("dom/resources/bigattrs.xml", "res/values/bigattrs1.xml");
    myFixture.copyFileToProject("dom/resources/bigattrs.xml", "res/values/bigattrs2.xml");
    myFixture.copyFileToProject("dom/resources/bigattrs.xml", "res/values/bigattrs3.xml");
    String path = copyFileToProject("bigfile.xml");
    VirtualFile f = myFixture.findFileInTempDir(path);
    myFixture.configureFromExistingVirtualFile(f);
    IdeaTestUtil.assertTiming("", 800, new Runnable() {
      public void run() {
        try {
          myFixture.completeBasic();
        }
        catch (Exception e) {
        }
      }
    });
  }

  public void testViewClassReference() throws Throwable {
    VirtualFile file = myFixture.copyFileToProject(testFolder + "/vcr.xml", getPathToCopy("vcr.xml"));
    myFixture.configureFromExistingVirtualFile(file);
    PsiFile psiFile = myFixture.getFile();
    String text = psiFile.getText();
    int rootOffset = text.indexOf("ScrollView");
    PsiReference rootReference = psiFile.findReferenceAt(rootOffset);
    assertNotNull(rootReference);
    PsiElement rootViewClass = rootReference.resolve();
    assertTrue("Must be PsiClass reference", rootViewClass instanceof PsiClass);
    int childOffset = text.indexOf("LinearLayout");
    PsiReference childReference = psiFile.findReferenceAt(childOffset);
    assertNotNull(childReference);
    PsiElement childViewClass = childReference.resolve();
    assertTrue("Must be PsiClass reference", childViewClass instanceof PsiClass);
  }
}

