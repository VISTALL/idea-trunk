package com.sixrr.xrp.wraptagcontents;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.context.SingleFileContext;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import java.io.IOException;

public class WrapTagContentsTest extends LightIdeaTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void test() throws IncorrectOperationException, IOException, SAXException {
        final XmlFile file = (XmlFile) LightIdeaTestCase.createLightFile("foo.xml", "<Foo>\n" +
                "    <BAR>4</BAR>\n" +
                "    <BAR>3</BAR>\n" +
                "    <BAR></BAR>\n" +
                "</Foo>");
        final SingleFileContext context = new SingleFileContext(file);
        final XmlTag rootTag = file.getDocument().getRootTag();
        final XmlTag childTag = rootTag.getSubTags()[0];
        final WrapTagContentsProcessor processor = new WrapTagContentsProcessor(childTag, "BAZ", context, false);
        processor.run();
        assertXMLEqual("<Foo>\n" +
                "    <BAR><BAZ>4</BAZ></BAR>\n" +
                "    <BAR><BAZ>3</BAZ></BAR>\n" +
                "    <BAR><BAZ/></BAR>\n" +
                "</Foo>", file.getText());
    }
}