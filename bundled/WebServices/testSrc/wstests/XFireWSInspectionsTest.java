package wstests;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.references.MyReferenceProvider;
import com.advancedtools.webservices.references.BaseRangedReference;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.filters.TrueFilter;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

/**
 * @by maxim
 */
public class XFireWSInspectionsTest extends BaseWSInspectionsTestCase {
  protected String getTestDataPath() {
    return "highlighting/xfire";
  }

  public void testXFireWs() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java","services.xml");
  }

  public void testXFireWs3() throws Throwable {
    final String testName = getTestName();
    EnvironmentFacade.getInstance().registerXmlAttributeValueReferenceProvider(
      myFixture.getProject(),
      new String[] {"class"},
      TrueFilter.INSTANCE, 
      EnvironmentFacade.getInstance().acquireClassReferenceProvider(myFixture.getProject())
    );
    EnvironmentFacade.getInstance().registerXmlAttributeValueReferenceProvider(
      myFixture.getProject(),
      new String[] {"name"},
      TrueFilter.INSTANCE,
      new MyReferenceProvider() {
        public PsiReference[] getReferencesByElement(final PsiElement psiElement) {
          return new PsiReference[] {
            new BaseRangedReference(psiElement, 1, psiElement.getTextLength() - 1) {
              @Nullable
              public PsiElement resolve() {
                XmlTag t = (XmlTag) psiElement.getParent().getParent().getParent();
                if (t.getAttributeValue("class") != null) {
                  final PsiReference[] references = t.getAttribute("class", null).getValueElement().getReferences();
                  final PsiElement element = references[references.length - 1].resolve();
                  if (element instanceof PsiClass) return PropertyUtil.findPropertySetter((PsiClass) element, getCanonicalText(), false, true);
                }
                return null;
              }

              public Object[] getVariants() {
                return new Object[0];
              }

              public boolean isSoft() {
                return false;
              }
            }
          };
        }
      }
    );
    doHighlightingTest(false,false, testName + ".java","services3.xml");
  }

  public void testHighlightingInConfig() throws Throwable {
    doHighlightingTest(true, "bad_services.xml", "XFireWs2.java");
  }

  public void testCxfWs() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java","cxf-servlet.xml");
  }

  public void testHighlightingInCxfConfig() throws Throwable {
    doHighlightingTest(true, "bad_cxf-servlet.xml", "CxfWs2.java");
  }
}
