package com.sixrr.xrp.convertcontentstoattribute;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.context.SingleFileContext;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import java.io.IOException;

public class ConvertContentsToAttributeTest extends LightIdeaTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void test() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo>\n" +
                "    <BAR>4 </BAR>\n" +
                "    <BAR> 3 </BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlTag childTag = rootTag.getSubTags()[0];
        final ConvertContentsToAttributeProcessor processor = new ConvertContentsToAttributeProcessor(childTag, "x", context, true, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = '4'></BAR>\n" +
                "    <BAR x = '3' ></BAR>\n" +
                "    <BAR x = ''></BAR>\n" +
                "</Foo>", file.getText());
    }

    public void testNoTrim() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo>\n" +
                "    <BAR>4 </BAR>\n" +
                "    <BAR> 3 </BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlTag childTag = rootTag.getSubTags()[0];
        final ConvertContentsToAttributeProcessor processor = new ConvertContentsToAttributeProcessor(childTag, "x", context, false, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = '4 '></BAR>\n" +
                "    <BAR x = ' 3 ' ></BAR>\n" +
                "    <BAR x = ''></BAR>\n" +
                "</Foo>", file.getText());
    }
}