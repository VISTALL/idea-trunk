package com.sixrr.xrp.tagtoattribute;

import com.intellij.openapi.help.HelpManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.IdeBorderFactory;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseRefactoringDialog;
import com.sixrr.xrp.ui.ScopePanel;
import com.sixrr.xrp.utils.XMLUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import java.awt.*;

@SuppressWarnings({"OverridableMethodCallInConstructor", "ThisEscapedInObjectConstruction"})
class TagToAttributeDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final JTextField attributeNameField;

    private final XmlTag tag;

    TagToAttributeDialog(XmlTag tag) {
        super(tag.getProject(), true);
        final String tagName = tag.getName();
        this.tag = tag;
        final XmlTag parentTag = tag.getParentTag();
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Replace Tag With Attribute");
        attributeNameLabel.setText("Name for new attribute");
        final XmlAttribute[] attributes = parentTag.getAttributes();
        String attributeName = null;
        for (XmlAttribute attribute : attributes) {
            if (attribute != null) {
                attributeName = attribute.getLocalName();
                break;
            }
        }
        final String newAttributeName = attributeNameFromTag(tagName, attributeName);
        attributeNameField = new JTextField(newAttributeName);
        final Document document = attributeNameField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.TagToAttribute";
    }

    public JComponent getPreferredFocusedComponent(){
        return attributeNameField;
    }
    private static String attributeNameFromTag(String tagName, String attributeName) {
        final int colonIndex = tagName.indexOf((int) ':');
        if (colonIndex >= 0) {
            final String prefix = tagName.substring(0, colonIndex + 1);
            final String suffix = tagName.substring(colonIndex + 1);
            return prefix + matchCase(suffix, attributeName);
        } else {
            return matchCase(tagName, attributeName);
        }
    }

    public static String matchCase(String stringToMatch, String stringMatched) {
        if (stringMatched == null || stringMatched.length() == 0) {
            return stringToMatch.toLowerCase();
        }
        if (isAllLowerCase(stringMatched)) {
            return stringToMatch.toLowerCase();
        }
        if (isAllUpperCase(stringMatched)) {
            return stringToMatch.toUpperCase();
        }
        final char startingChar = stringMatched.charAt(0);
        if (Character.isLowerCase(startingChar)) {
            return Character.toLowerCase(stringToMatch.charAt(0)) + stringToMatch.substring(1);
        }
        if (Character.isUpperCase(startingChar)) {
            return Character.toUpperCase(stringToMatch.charAt(0)) + stringToMatch.substring(1);
        }
        return stringToMatch;
    }

    private static boolean isAllLowerCase(String string) {
        final char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c) && !Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
    private static boolean isAllUpperCase(String string) {
        final char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c) && !Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }


    public String getAttributeName() {
        final String text = attributeNameField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Replace tag " + tagName + " with attribute");
        panel.add(attributeNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        attributeNameField.setEditable(true);
        box.add(attributeNameField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }


    protected boolean isValid() {
        final String attributeName = getAttributeName();
        final boolean attributeNameEntered = XMLUtil.attributeNameIsValid(attributeName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return attributeNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {
        final String parentTagname = tag.getParentTag().getName();
        final String tagName = tag.getName();
        final String attributeName = getAttributeName();
        return " <xsl:template match=\"" + parentTagname + "\">\n" +
                "\t<xsl:copy> \n" +
                "\t\t<xsl:for-each select =\"child::" + tagName + "\">\n" +
                "\t\t\t<xsl:attribute name = \""+ attributeName + "\">\n" +
                "\t\t\t\t<xsl:value-of select=\".\"/>\n" +
                "\t\t\t</xsl:attribute>\n" +
                "\t\t</xsl:for-each>\n" +
                "\t\t<xsl:apply-templates select=\"@*|node()\"/>\n" +
                "\t</xsl:copy>\n" +
                "</xsl:template>\n" +
                "<xsl:template match=\"" + parentTagname + "/" + tagName+"\"/>";
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.TagToAttribute);
    }

}