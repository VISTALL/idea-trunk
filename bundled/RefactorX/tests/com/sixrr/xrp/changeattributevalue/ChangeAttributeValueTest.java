package com.sixrr.xrp.changeattributevalue;

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

public class ChangeAttributeValueTest extends LightIdeaTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void test() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo>\n" +
                "    <BAR x = \"4\"></BAR>\n" +
                "    <BAR x = '3'></BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlTag childTag = rootTag.getSubTags()[0];
        final XmlAttribute attr = childTag.getAttribute("x");
        final ChangeAttributeValueProcessor processor = new ChangeAttributeValueProcessor(attr, "5", context, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = \"5\"></BAR>\n" +
                "    <BAR x = '3'></BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>", file.getText());
    }
}