package com.sixrr.xrp.addattribute;

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

public class AddAttributeTest extends LightIdeaTestCase {

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
        final AddAttributeProcessor processor = new AddAttributeProcessor(childTag, "foo", "bar", true, context, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = \"4\" foo=\"bar\"></BAR>\n" +
                "    <BAR x = '3' foo=\"bar\"></BAR>\n" +
                "    <BAR foo=\"bar\"></BAR>\n" +
                "</Foo>", file.getText());
    }


    public void testAddIfAbsent() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo>\n" +
                "    <BAR x = \"4\"></BAR>\n" +
                "    <BAR x = '3'></BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlTag childTag = rootTag.getSubTags()[0];
        final XmlAttribute attr = childTag.getAttribute("x");
        final AddAttributeProcessor processor = new AddAttributeProcessor(childTag, "x", "bar", true, context, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = \"4\"></BAR>\n" +
                "    <BAR x = '3'></BAR>\n" +
                "    <BAR x=\"bar\"></BAR>\n" +
                "</Foo>", file.getText());
    }

    public void testAddChanging() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo>\n" +
                "    <BAR x = \"4\"></BAR>\n" +
                "    <BAR x = '3'></BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlTag childTag = rootTag.getSubTags()[0];
        final XmlAttribute attr = childTag.getAttribute("x");
        final AddAttributeProcessor processor = new AddAttributeProcessor(childTag, "x", "bar", false, context, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR x = \"bar\"></BAR>\n" +
                "    <BAR x = \"bar\"></BAR>\n" +
                "    <BAR x=\"bar\"></BAR>\n" +
                "</Foo>", file.getText());
    }
}
