package com.sixrr.xrp.attributetotag;

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
class AttributeToTagDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final JTextField tagNameField;

    private final XmlAttribute attribute;
    private final XmlTag tag;

    AttributeToTagDialog(XmlAttribute attribute) {
        super(attribute.getProject(), true);
        this.attribute = attribute;
        final String attributeName = attribute.getName();
        tag = attribute.getParent();
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Replace Attribute With Tag");
        attributeNameLabel.setText("Name for new tag");
        final String newTagName = tagNameFromAttribute(attributeName, tag.getLocalName());
        tagNameField = new JTextField(newTagName);
        final Document document = tagNameField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    private static String tagNameFromAttribute(String attributeName, String tagName) {
        final int colonIndex = attributeName.indexOf((int) ':');
        if (colonIndex >= 0) {
            final String prefix = attributeName.substring(0, colonIndex + 1);
            final String suffix = attributeName.substring(colonIndex + 1);
            return prefix + matchCase(suffix, tagName);
        } else {
            return matchCase(attributeName, tagName);
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
    protected String getDimensionServiceKey() {
        return "RefactorX.AttributeToTag";
    }

    public JComponent getPreferredFocusedComponent(){
        return tagNameField;
    }
    public String getTagName() {
        final String text = tagNameField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();

        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Replace attribute " + attributeName + " of tag " + tagName);
        panel.add(attributeNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        tagNameField.setEditable(true);
        box.add(tagNameField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }

    protected boolean isValid() {
        final String tagName = getTagName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(tagName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();
        final String newTagName = getTagName();
        return "<xsl:template match=\"@" + attributeName+ "[parent::" + tagName +"]\">\n" +
                "<" + newTagName +"><xsl:value-of select=\".\"/></" + newTagName +">\n" +
                "</xsl:template>";
    }


    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.AttributeToTag);
    }
}