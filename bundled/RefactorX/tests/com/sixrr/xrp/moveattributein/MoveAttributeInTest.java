package com.sixrr.xrp.moveattributein;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.context.SingleFileContext;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import java.io.IOException;

public class MoveAttributeInTest extends LightIdeaTestCase {


    protected void setUp() throws Exception {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void test() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo x=\"4\">\n" +
                "    <BAR >barangus</BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlAttribute attr = rootTag.getAttribute("x");
        final MoveAttributeInProcessor processor = new MoveAttributeInProcessor(attr, "BAR", context, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = \"4\">barangus</BAR>\n" +
                "    <BAR x = '4'/>\n" +
                "</Foo>", file.getText());
    }
}